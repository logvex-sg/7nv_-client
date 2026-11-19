package dev.sevennv.client.diagnostics;

import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.module.ModuleManager;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Locale;

/**
 * Breaks the client's own per-frame cost down by dispatch phase: module ticks,
 * 2D rendering and world rendering. This measures only the time spent inside
 * Logvex's module loops, which is what tells you whether the client or the game
 * is responsible for a frame drop.
 *
 * <p>Profiling is enabled only while this module is on, so an ordinary session
 * never pays the {@link System#nanoTime()} calls.
 */
public final class FrameProfiler extends Module {
    public FrameProfiler() {
        super("Frame Profiler", "Attributes frame time to the client's own loops", Category.DIAGNOSTICS);
        refresh = integer("Refresh", "Milliseconds between resets", 1000, 250, 10000);
        showAverages = bool("Averages", "Show per-call averages", true);
    }

    private final IntSetting refresh;
    private final dev.sevennv.client.config.BooleanSetting showAverages;

    private long windowStart;
    private long lastTickNanos;
    private long lastRenderNanos;
    private long lastWorldNanos;
    private long lastTickCalls;
    private long lastRenderCalls;
    private long lastWorldCalls;
    private double windowMs;

    @Override
    public void onEnable() {
        ModuleManager.setProfiling(true);
        ModuleManager.resetProfile();
        windowStart = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
        ModuleManager.setProfiling(false);
    }

    @Override
    public void onWorldChange() {
        windowStart = System.currentTimeMillis();
        lastTickNanos = 0;
        lastRenderNanos = 0;
        lastWorldNanos = 0;
        lastTickCalls = 0;
        lastRenderCalls = 0;
        lastWorldCalls = 0;
    }

    @Override
    public void onTick() {
        long now = System.currentTimeMillis();
        long elapsed = now - windowStart;
        if (elapsed < refresh.get()) {
            return;
        }
        windowStart = now;
        windowMs = elapsed;
        long[] snapshot = ModuleManager.profileSnapshot();
        lastTickNanos = snapshot[ModuleManager.PROFILE_TICK * 2];
        lastTickCalls = snapshot[ModuleManager.PROFILE_TICK * 2 + 1];
        lastRenderNanos = snapshot[ModuleManager.PROFILE_RENDER2D * 2];
        lastRenderCalls = snapshot[ModuleManager.PROFILE_RENDER2D * 2 + 1];
        lastWorldNanos = snapshot[ModuleManager.PROFILE_WORLD * 2];
        lastWorldCalls = snapshot[ModuleManager.PROFILE_WORLD * 2 + 1];
        ModuleManager.resetProfile();
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Theme theme = theme();
        DiagPanel panel = new DiagPanel().title("Frame Profiler");
        panel.row("Window", String.format(Locale.ROOT, "%d ms", (long) windowMs));
        panel.row("Tick", phase(lastTickNanos, lastTickCalls));
        panel.row("Render 2D", phase(lastRenderNanos, lastRenderCalls));
        panel.row("World", phase(lastWorldNanos, lastWorldCalls));
        panel.row("Modules", String.valueOf(dev.sevennv.client.SevennvClient.getModuleManager().getEnabledCount()));
        panel.render(graphics, theme, 4, 4);
    }

    private String phase(long nanos, long calls) {
        double totalMs = nanos / 1_000_000.0;
        if (!showAverages.get() || calls == 0) {
            return String.format(Locale.ROOT, "%.2f ms", totalMs);
        }
        double avgMicros = nanos / 1000.0 / calls;
        return String.format(Locale.ROOT, "%.2f ms (%d, %.0f \u00b5s)", totalMs, calls, avgMicros);
    }

    private Theme theme() {
        var config = dev.sevennv.client.SevennvClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
