package dev.sevennv.client.module;

import dev.sevennv.client.SevennvClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Owns every registered module and drives their tick lifecycle.
 *
 * <p>Every dispatch path (tick, 2D render, world render, packet, chat, world
 * change) needs only the <em>enabled</em> modules, yet those paths run once per
 * tick, once per frame and once per network packet. Walking the full registry
 * and testing {@link Module#isEnabled()} on each entry every time would touch
 * ~190 modules per packet, most of them disabled. Instead the enabled subset is
 * kept in pre-built lists that are rebuilt only when a module is toggled, so a
 * steady state costs one array read per dispatch.
 */
public final class ModuleManager {
    private final Map<Class<? extends Module>, Module> modules = new LinkedHashMap<>();
    private final List<Module> ordered = new ArrayList<>();

    // Enabled-only snapshots, rebuilt lazily after any enable/disable.
    private List<Module> enabled = List.of();
    private List<Module> tickers = List.of();
    private List<Module> renderers2D = List.of();
    private List<Module> worldRenderers = List.of();
    private List<Module> packetHandlers = List.of();
    private List<Module> chatHandlers = List.of();
    private List<Module> worldChangeHandlers = List.of();
    private List<Module> bound = List.of();
    private boolean bucketsDirty = true;
    private int enabledVersion;

    public void register(Module module) {
        modules.put(module.getClass(), module);
        ordered.add(module);
        ordered.sort(Comparator.comparing(Module::getName));
        // Performance modules are always-on: force them enabled before any
        // config load runs, so they are active even on a fresh install.
        if (module.isAlwaysOn()) {
            module.setEnabled(true);
        }
        invalidate();
    }

    /**
     * Marks the cached enabled lists stale. Called by {@link Module#setEnabled}
     * so a toggle costs one boolean write, not a full rebuild.
     */
    public void invalidate() {
        bucketsDirty = true;
        enabledVersion++;
    }

    /**
     * Monotonic counter bumped whenever any module is enabled or disabled.
     * Cheap-to-compare signal for caches that depend on the enabled set.
     */
    public int getEnabledVersion() {
        return enabledVersion;
    }

    private void ensureBuckets() {
        if (!bucketsDirty) {
            return;
        }
        bucketsDirty = false;
        List<Module> on = new ArrayList<>();
        List<Module> tick = new ArrayList<>();
        List<Module> r2d = new ArrayList<>();
        List<Module> world = new ArrayList<>();
        List<Module> packets = new ArrayList<>();
        List<Module> chats = new ArrayList<>();
        List<Module> changes = new ArrayList<>();
        List<Module> keys = new ArrayList<>();
        for (Module module : ordered) {
            if (module.isBound()) {
                keys.add(module);
            }
            if (!module.isEnabled()) {
                continue;
            }
            on.add(module);
            if (module.wantsTick()) {
                tick.add(module);
            }
            if (module.wantsRender2D()) {
                r2d.add(module);
            }
            if (module.wantsWorldRender()) {
                world.add(module);
            }
            if (module.wantsPackets()) {
                packets.add(module);
            }
            if (module.wantsChat()) {
                chats.add(module);
            }
            if (module.wantsWorldChange()) {
                changes.add(module);
            }
        }
        enabled = List.copyOf(on);
        tickers = List.copyOf(tick);
        renderers2D = List.copyOf(r2d);
        worldRenderers = List.copyOf(world);
        packetHandlers = List.copyOf(packets);
        chatHandlers = List.copyOf(chats);
        worldChangeHandlers = List.copyOf(changes);
        bound = List.copyOf(keys);
    }

    /** True when at least one enabled module wants per-packet callbacks. */
    public boolean hasPacketHandlers() {
        ensureBuckets();
        return !packetHandlers.isEmpty();
    }

    /** True when at least one enabled module wants per-chat callbacks. */
    public boolean hasChatHandlers() {
        ensureBuckets();
        return !chatHandlers.isEmpty();
    }

    // ------------------------------------------------------------------ profiling
    // Optional dispatch profiling for the Frame Profiler module. Off by default
    // so a normal session pays only one volatile read per dispatch.
    public static final int PROFILE_TICK = 0;
    public static final int PROFILE_RENDER2D = 1;
    public static final int PROFILE_WORLD = 2;
    public static final int PROFILE_PACKET = 3;
    public static final int PROFILE_CHAT = 4;
    private static final int PROFILE_SLOTS = 5;

    private static volatile boolean profiling;
    private static final long[] PROFILE_NANOS = new long[PROFILE_SLOTS];
    private static final long[] PROFILE_CALLS = new long[PROFILE_SLOTS];

    public static void setProfiling(boolean enabled) {
        profiling = enabled;
        if (!enabled) {
            java.util.Arrays.fill(PROFILE_NANOS, 0L);
            java.util.Arrays.fill(PROFILE_CALLS, 0L);
        }
    }

    public static boolean isProfiling() {
        return profiling;
    }

    /** Returns {nanos, calls} pairs flattened per slot; caller reads defensively. */
    public static long[] profileSnapshot() {
        long[] copy = new long[PROFILE_SLOTS * 2];
        for (int i = 0; i < PROFILE_SLOTS; i++) {
            copy[i * 2] = PROFILE_NANOS[i];
            copy[i * 2 + 1] = PROFILE_CALLS[i];
        }
        return copy;
    }

    public static void resetProfile() {
        java.util.Arrays.fill(PROFILE_NANOS, 0L);
        java.util.Arrays.fill(PROFILE_CALLS, 0L);
    }

    private static void recordProfile(int slot, long nanos) {
        PROFILE_NANOS[slot] += nanos;
        PROFILE_CALLS[slot]++;
    }

    public void registerAll(Module... entries) {
        for (Module module : entries) {
            register(module);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> type) {
        return (T) modules.get(type);
    }

    public Module getByName(String name) {
        for (Module module : ordered) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public List<Module> getAll() {
        return ordered;
    }

    public List<Module> getByCategory(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module module : ordered) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }
        return result;
    }

    public List<Module> getEnabled() {
        ensureBuckets();
        return enabled;
    }

    public int getEnabledCount() {
        ensureBuckets();
        return enabled.size();
    }

    /** Ticked once per client tick from the tick event. */
    public void tick() {
        ensureBuckets();
        long start = profiling ? System.nanoTime() : 0L;
        for (Module module : tickers) {
            try {
                module.onTick();
            } catch (Exception exception) {
                SevennvClient.LOGGER.error("Module {} threw during tick, disabling", module.getName(), exception);
                module.setEnabled(false);
            }
        }
        if (profiling) {
            recordProfile(PROFILE_TICK, System.nanoTime() - start);
        }
    }

    /** Polls bound module keybinds. Called once per tick before {@link #tick()}. */
    public void pollKeybinds() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) {
            return;
        }
        ensureBuckets();
        for (Module module : bound) {
            InputConstants.Key key = module.getKeybind();
            while (dev.sevennv.client.utils.InputUtils.consumePress(key)) {
                module.toggle();
            }
        }
    }

    public void onWorldChange() {
        ensureBuckets();
        for (Module module : worldChangeHandlers) {
            try {
                module.onWorldChange();
            } catch (Exception exception) {
                SevennvClient.LOGGER.error("Module {} threw on world change", module.getName(), exception);
            }
        }
    }

    /** Dispatches a 2D render pass to every enabled module. */
    public void render2D(net.minecraft.client.gui.GuiGraphics graphics, float partialTick) {
        ensureBuckets();
        long start = profiling ? System.nanoTime() : 0L;
        for (Module module : renderers2D) {
            try {
                module.onRender2D(graphics, partialTick);
            } catch (Exception exception) {
                SevennvClient.LOGGER.error("Module {} threw during 2D render", module.getName(), exception);
            }
        }
        if (profiling) {
            recordProfile(PROFILE_RENDER2D, System.nanoTime() - start);
        }
    }

    /** Dispatches the world render pass to every enabled module. */
    public void renderWorld() {
        ensureBuckets();
        long start = profiling ? System.nanoTime() : 0L;
        for (Module module : worldRenderers) {
            try {
                module.onWorldRender();
            } catch (Exception exception) {
                SevennvClient.LOGGER.error("Module {} threw during world render", module.getName(), exception);
            }
        }
        if (profiling) {
            recordProfile(PROFILE_WORLD, System.nanoTime() - start);
        }
    }

    /** Dispatches a packet to every enabled module. Called from the packet observer. */
    public void onPacket(dev.sevennv.client.events.PacketEvent event) {
        ensureBuckets();
        for (Module module : packetHandlers) {
            try {
                module.onPacket(event);
            } catch (Exception exception) {
                SevennvClient.LOGGER.error("Module {} threw handling a packet", module.getName(), exception);
            }
        }
    }

    /** Dispatches an incoming chat message to every enabled module. */
    public void onChat(dev.sevennv.client.events.ChatReceiveEvent event) {
        ensureBuckets();
        for (Module module : chatHandlers) {
            try {
                module.onChat(event);
            } catch (Exception exception) {
                SevennvClient.LOGGER.error("Module {} threw handling a chat message", module.getName(), exception);
            }
        }
    }

    /** Disables everything except modules that opt out of the disconnect sweep. */
    public void disableAll() {
        for (Module module : ordered) {
            if (module.isEnabled() && !module.survivesDisconnect()) {
                module.setEnabled(false);
            }
        }
    }

    public void resetKeybinds() {
        for (Module module : ordered) {
            module.setKeybind(InputConstants.UNKNOWN);
        }
    }
}
