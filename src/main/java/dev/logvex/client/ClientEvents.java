package dev.logvex.client;

import dev.logvex.client.events.ChatReceiveEvent;
import dev.logvex.client.events.ChatSendEvent;
import dev.logvex.client.events.HudRenderEvent;
import dev.logvex.client.events.PacketEvent;
import dev.logvex.client.events.TickEvent;
import dev.logvex.client.events.WorldChangeEvent;
import dev.logvex.client.hud.HudManager;
import dev.logvex.client.misc.AutoEz;
import dev.logvex.client.misc.AutoGg;
import dev.logvex.client.misc.AutoReconnect;
import dev.logvex.client.misc.ChatMacros;
import dev.logvex.client.misc.ChatTimestamps;
import dev.logvex.client.misc.LastServer;
import dev.logvex.client.module.ModuleManager;
import dev.logvex.client.render.RenderUtils;
import dev.logvex.client.utils.CombatTracker;
import dev.logvex.client.utils.CpsTracker;
import dev.logvex.client.utils.NotificationManager;
import dev.logvex.client.utils.PacketLog;
import dev.logvex.client.world.DeathCoordinates;
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

    private ClientEvents() {
    }

    static void register(LogvexClient client) {
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

        ClientSendMessageEvents.ALLOW_CHAT.register(ClientEvents::onSendChat);
        ClientSendMessageEvents.ALLOW_COMMAND.register(ClientEvents::onSendCommand);

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(LogvexClient.MOD_ID, "hud"),
                ClientEvents::renderHud);

        // Gizmo-based world rendering: this callback runs inside the level
        // renderer's per-frame gizmo collection, so module emissions are picked
        // up by the same frame that is currently being drawn.
        WorldRenderEvents.END_MAIN.register(context -> LogvexClient.getModuleManager().renderWorld());
    }

    private static ModuleManager modules() {
        return LogvexClient.getModuleManager();
    }

    private static void dispatchChat(Component message, boolean overlay) {
        ChatReceiveEvent event = new ChatReceiveEvent(message, overlay);
        LogvexClient.getEventManager().post(event);
        modules().onChat(event);
    }

    private static void onTick(Minecraft mc) {
        LogvexClient.getKeybindManager().poll();
        LogvexClient.getModuleManager().tick();
        LogvexClient.getEventManager().post(new TickEvent(mc, mc.getDeltaTracker()));
        LogvexClient.getConfigManager().saveIfDirty();

        // Packets are recorded on the netty thread and drained here, on the
        // client thread, so modules never touch game state off-thread. The
        // combat tracker consumes the same stream to attribute kills.
        for (PacketEvent event : PacketLog.drain()) {
            trackCombat(event, mc);
            LogvexClient.getModuleManager().onPacket(event);
        }

        drainDeaths();
        pollAutoReconnect();

        Level level = mc.level;
        if (level != lastLevel) {
            lastLevel = level;
            if (level != null) {
                LogvexClient.getModuleManager().onWorldChange();
                LogvexClient.getEventManager().post(new WorldChangeEvent(true));
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
        deathCursor = System.currentTimeMillis();
        localDeathCursor = 0;
        LastServer.capture(mc);
        AutoReconnect reconnect = modules().get(AutoReconnect.class);
        if (reconnect != null) {
            reconnect.resetAttempts();
        }
        NotificationManager.info("Connected", "Logvex Client " + LogvexClient.VERSION);
    }

    private static void onDisconnect() {
        lastLevel = null;
        LogvexClient.getModuleManager().disableAll();
        LogvexClient.getModuleManager().onWorldChange();
        LogvexClient.getEventManager().post(new WorldChangeEvent(false));
        NotificationManager.clear();
        // Friends, waypoints and notes are persistent config and survive a
        // disconnect; only transient session state is dropped here.
        PacketLog.reset();
        CpsTracker.reset();
        CombatTracker.reset();

        AutoReconnect reconnect = modules().get(AutoReconnect.class);
        if (reconnect != null && reconnect.isEnabled()) {
            reconnect.onDisconnected();
        }
    }

    private static boolean onSendChat(String message) {
        // Macros and dot commands are client-local and must not reach the server.
        ChatMacros macros = modules().get(ChatMacros.class);
        if (macros != null && macros.isEnabled() && macros.handle(message)) {
            return false;
        }
        ChatSendEvent event = new ChatSendEvent(message);
        return !LogvexClient.getEventManager().postCancellable(event);
    }

    private static boolean onSendCommand(String command) {
        // Commands starting with '.' are client-local and never reach the server.
        return LogvexClient.getCommandManager() == null
                || !LogvexClient.getCommandManager().handle("." + command);
    }

    private static void renderHud(GuiGraphics graphics, net.minecraft.client.DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) {
            return;
        }
        float partialTick = delta.getGameTimeDeltaPartialTick(false);
        HudManager hud = LogvexClient.getHudManager();
        if (hud != null) {
            hud.render(graphics);
        }
        LogvexClient.getModuleManager().render2D(graphics, partialTick);
        NotificationManager.render(graphics);
        LogvexClient.getEventManager().post(new HudRenderEvent(graphics, partialTick));
        RenderUtils.font();
    }
}
