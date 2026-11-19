package dev.sevennv.client;

import dev.sevennv.client.events.ChatReceiveEvent;
import dev.sevennv.client.events.ChatSendEvent;
import dev.sevennv.client.events.HudRenderEvent;
import dev.sevennv.client.events.PacketEvent;
import dev.sevennv.client.events.TickEvent;
import dev.sevennv.client.events.WorldChangeEvent;
import dev.sevennv.client.hud.HudManager;
import dev.sevennv.client.misc.AutoEz;
import dev.sevennv.client.misc.AutoGg;
import dev.sevennv.client.misc.AutoReconnect;
import dev.sevennv.client.misc.ChatFilter;
import dev.sevennv.client.misc.ChatMacros;
import dev.sevennv.client.misc.ChatSuffix;
import dev.sevennv.client.misc.ChatTimestamps;
import dev.sevennv.client.misc.LastServer;
import dev.sevennv.client.module.ModuleManager;
import dev.sevennv.client.render.RenderUtils;
import dev.sevennv.client.utils.CombatTracker;
import dev.sevennv.client.utils.CpsTracker;
import dev.sevennv.client.utils.NotificationManager;
import dev.sevennv.client.utils.PacketLog;
import dev.sevennv.client.utils.ServerFingerprint;
import dev.sevennv.client.world.DeathCoordinates;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

/**
 * Wires Fabric API callbacks to the client's own managers and event bus. This
 * is the only place that touches Fabric lifecycle and rendering events.
 */
final class ClientEvents {
    private static Level lastLevel;

    // Reused across ticks so a steady packet stream does not allocate a list
    // every tick.
    private static final java.util.List<PacketEvent> packetBatch = new java.util.ArrayList<>();

    private ClientEvents() {
    }

    static void register(SevennvClient client) {
        ClientTickEvents.START_CLIENT_TICK.register(ClientEvents::onTick);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, mc) -> onJoin(mc));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, mc) -> onDisconnect());

        // System/game messages are decorated (timestamps) before they reach the
        // chat history; player chat is dispatched as-is once it is displayed.
        ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> {
            Component decorated = message;
            ChatTimestamps timestamps = modules().get(ChatTimestamps.class);
            if (timestamps != null && timestamps.isEnabled()) {
                decorated = timestamps.decorate(message);
            }
            dispatchChat(decorated, overlay);
            return decorated;
        });
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, timestamp) ->
                dispatchChat(message, false));

        // Chat messages are dropped before they reach the history, so a
        // filtered message never appears and cannot be scrolled back to.
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            ChatFilter filter = modules().get(ChatFilter.class);
            return filter == null || !filter.isEnabled() || filter.allow(message.getString());
        });

        // Player chat arrives on its own channel; the same filter runs there.
        // Your own messages are always kept so you can see what you sent.
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signed, sender, params, timestamp) -> {
            ChatFilter filter = modules().get(ChatFilter.class);
            if (filter == null || !filter.isEnabled()) {
                return true;
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (sender != null && minecraft.player != null
                    && sender.id().equals(minecraft.player.getUUID())) {
                return true;
            }
            return filter.allow(message.getString());
        });

        ClientSendMessageEvents.ALLOW_CHAT.register(ClientEvents::onSendChat);
        ClientSendMessageEvents.ALLOW_COMMAND.register(ClientEvents::onSendCommand);
        ClientSendMessageEvents.MODIFY_CHAT.register(ClientEvents::modifyChat);

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(SevennvClient.MOD_ID, "hud"),
                ClientEvents::renderHud);

        // Cache the camera position on the render thread before terrain is drawn,
        // for the culling and pixel-block hooks, which may run on section-compile
        // workers.
        WorldRenderEvents.START_MAIN.register(context -> {
            net.minecraft.client.Camera camera = context.gameRenderer().getMainCamera();
            if (camera != null) {
                net.minecraft.world.phys.Vec3 pos = camera.position();
                dev.sevennv.client.render.PixelBlocks.updateCamera(pos.x, pos.y, pos.z);
                dev.sevennv.client.render.ParticleLimiter.updateCamera(pos.x, pos.y, pos.z);
                dev.sevennv.client.render.EntityCulling.onFrameStart();
                dev.sevennv.client.render.ParticleLimiter.onFrameStart();
            }
        });

        // Gizmo-based world rendering: this callback runs inside the level
        // renderer's per-frame gizmo collection, so module emissions are picked
        // up by the same frame that is currently being drawn.
        WorldRenderEvents.END_MAIN.register(context -> SevennvClient.getModuleManager().renderWorld());
    }

    private static ModuleManager modules() {
        return SevennvClient.getModuleManager();
    }

    private static void dispatchChat(Component message, boolean overlay) {
        ChatReceiveEvent event = new ChatReceiveEvent(message, overlay);
        SevennvClient.getEventManager().post(event);
        modules().onChat(event);
    }

    private static void onTick(Minecraft mc) {
        SevennvClient.getKeybindManager().poll();
        SevennvClient.getModuleManager().tick();
        SevennvClient.getEventManager().post(new TickEvent(mc, mc.getDeltaTracker()));
        SevennvClient.getConfigManager().saveIfDirty();

        // Packets are recorded on the netty thread and drained here, on the
        // client thread, so modules never touch game state off-thread. The
        // combat tracker consumes the same stream to attribute kills.
        ModuleManager manager = SevennvClient.getModuleManager();
        int drained = PacketLog.drainInto(packetBatch);
        if (drained > 0) {
            boolean wantPackets = manager.hasPacketHandlers();
            for (int i = 0; i < drained; i++) {
                PacketEvent event = packetBatch.get(i);
                trackCombat(event, mc);
                if (wantPackets) {
                    manager.onPacket(event);
                }
            }
        }

        drainDeaths();
        pollAutoReconnect();

        Level level = mc.level;
        if (level != lastLevel) {
            lastLevel = level;
            if (level != null) {
                SevennvClient.getModuleManager().onWorldChange();
                SevennvClient.getEventManager().post(new WorldChangeEvent(true));
            }
        }
    }

    private static void trackCombat(PacketEvent event, Minecraft mc) {
        if (!(mc.level instanceof net.minecraft.client.multiplayer.ClientLevel level)) {
            return;
        }
        if (event.getPacket() instanceof ServerboundInteractPacket interact
                && event.getDirection() == PacketEvent.Direction.OUTBOUND) {
            CombatTracker.onAttackPacket(interact);
        } else if (event.getPacket() instanceof ClientboundEntityEventPacket entityEvent) {
            CombatTracker.onEntityEvent(entityEvent, level);
        } else if (event.getPacket() instanceof ClientboundPlayerCombatKillPacket kill) {
            CombatTracker.onCombatKill(kill, level);
        }
    }

    private static long deathCursor;

    /** Feeds recorded deaths to the reply and death-marker modules. */
    private static void drainDeaths() {
        for (CombatTracker.Death death : CombatTracker.deathsSince(deathCursor)) {
            deathCursor = Math.max(deathCursor, death.time());
            AutoEz ez = modules().get(AutoEz.class);
            if (ez != null && ez.isEnabled()) {
                if (death.credited()) {
                    ez.onKillName(death.name());
                } else if (death.player()) {
                    ez.onObservedDeathName(death.name());
                }
            }
        }

        CombatTracker.LocalDeath local = CombatTracker.lastLocalDeath();
        if (local != null && local.time() > localDeathCursor) {
            localDeathCursor = local.time();
            DeathCoordinates deaths = modules().get(DeathCoordinates.class);
            if (deaths != null && deaths.isEnabled()) {
                deaths.onDeath(local.position(), local.dimension());
            }
            AutoGg gg = modules().get(AutoGg.class);
            if (gg != null && gg.isEnabled()) {
                gg.onDeath();
            }
        }
    }

    private static long localDeathCursor;

    private static void pollAutoReconnect() {
        if (Minecraft.getInstance().getConnection() == null) {
            return;
        }
        AutoReconnect reconnect = modules().get(AutoReconnect.class);
        if (reconnect != null && reconnect.isEnabled() && reconnect.isPending()) {
            reconnect.cancelPending();
        }
    }

    private static void onJoin(Minecraft mc) {
        NotificationManager.clear();
        CpsTracker.reset();
        PacketLog.reset();
        CombatTracker.reset();
        ServerFingerprint.reset();
        deathCursor = System.currentTimeMillis();
        localDeathCursor = 0;
        LastServer.capture(mc);
        AutoReconnect reconnect = modules().get(AutoReconnect.class);
        if (reconnect != null) {
            reconnect.resetAttempts();
        }
        NotificationManager.info("Connected", "7nv_ Client " + SevennvClient.VERSION);
    }

    private static void onDisconnect() {
        lastLevel = null;
        SevennvClient.getModuleManager().disableAll();
        SevennvClient.getModuleManager().onWorldChange();
        SevennvClient.getEventManager().post(new WorldChangeEvent(false));
        NotificationManager.clear();
        // Friends, waypoints and notes are persistent config and survive a
        // disconnect; only transient session state is dropped here.
        PacketLog.reset();
        CpsTracker.reset();
        CombatTracker.reset();
        ServerFingerprint.reset();

        AutoReconnect reconnect = modules().get(AutoReconnect.class);
        if (reconnect != null && reconnect.isEnabled()) {
            reconnect.onDisconnected();
        }
    }

    private static boolean onSendChat(String message) {
        // A chat line starting with the client prefix is a client command and
        // is handled locally, so it is never sent to the server as chat.
        dev.sevennv.client.command.CommandManager commands = SevennvClient.getCommandManager();
        if (commands != null && message.startsWith(dev.sevennv.client.command.CommandManager.PREFIX)) {
            commands.handle(message);
            return false;
        }
        ChatMacros macros = modules().get(ChatMacros.class);
        if (macros != null && macros.isEnabled() && macros.handle(message)) {
            return false;
        }
        ChatSendEvent event = new ChatSendEvent(message);
        return !SevennvClient.getEventManager().postCancellable(event);
    }

    /**
     * Rewrites an outgoing chat line before vanilla sends it. This is the only
     * correct place to append a suffix: sending from the ALLOW event would
     * re-enter the same event and loop.
     */
    private static String modifyChat(String message) {
        ChatSuffix suffix = modules().get(ChatSuffix.class);
        if (suffix == null || !suffix.isEnabled()) {
            return message;
        }
        return suffix.decorate(message);
    }

    private static boolean onSendCommand(String command) {
        // A slash command that also carries the client prefix (e.g. /!toggle)
        // is handled locally. Everything else is a real server command and is
        // passed straight through, untouched.
        dev.sevennv.client.command.CommandManager commands = SevennvClient.getCommandManager();
        if (commands == null || !command.startsWith(dev.sevennv.client.command.CommandManager.PREFIX)) {
            return true;
        }
        return !commands.handle(command);
    }

    private static void renderHud(GuiGraphics graphics, net.minecraft.client.DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) {
            return;
        }
        float partialTick = delta.getGameTimeDeltaPartialTick(false);
        HudManager hud = SevennvClient.getHudManager();
        if (hud != null) {
            hud.render(graphics);
        }
        SevennvClient.getModuleManager().render2D(graphics, partialTick);
        NotificationManager.render(graphics);
        SevennvClient.getEventManager().post(new HudRenderEvent(graphics, partialTick));
        RenderUtils.font();
    }
}
