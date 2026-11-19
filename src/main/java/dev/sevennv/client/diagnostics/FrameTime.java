package dev.sevennv.client.diagnostics;

import dev.sevennv.client.config.Theme;
import dev.sevennv.client.config.IntSetting;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Measures the time between rendered frames so a stutter is easy to spot. The
 * sample is taken from the client's own frame callback; nothing is sent and no
 * server state is touched.
 */
public final class FrameTime extends DiagModule {
    public FrameTime() {
        super("Frame Time", "Reports per-frame render time");
        window = integer("Window", "Frames averaged for the display", 60, 10, 600);
        warnMs = integer("Warn ms", "Highlight frame times above this", 33, 5, 500);
    }

    private final IntSetting window;
    private final IntSetting warnMs;

    private long lastNanos;
    private double current;
    private double min = Double.MAX_VALUE;
    private double max;
    private double average;
    private int samples;

    @Override
    public void onWorldChange() {
        lastNanos = 0;
        current = 0;
        min = Double.MAX_VALUE;
        max = 0;
        average = 0;
        samples = 0;
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        long now = System.nanoTime();
        if (lastNanos != 0) {
            current = (now - lastNanos) / 1_000_000.0;
            min = Math.min(min, current);
            max = Math.max(max, current);
            samples++;
            average += (current - average) / Math.min(samples, window.get());
        }
        lastNanos = now;

        Theme theme = theme();
        DiagPanel panel = new DiagPanel().title("Frame Time");
        panel.row("Current", String.format(java.util.Locale.ROOT, "%.1f ms", current));
        panel.row("Average", String.format(java.util.Locale.ROOT, "%.1f ms", average));
        panel.row("Min", String.format(java.util.Locale.ROOT, "%.1f ms", min == Double.MAX_VALUE ? 0 : min));
        panel.row("Max", String.format(java.util.Locale.ROOT, "%.1f ms", max));
        panel.row("Est. FPS", current > 0 ? String.format(java.util.Locale.ROOT, "%.0f", 1000.0 / current) : "-");
        panel.row("Status", current > warnMs.get() ? "slow" : "ok");
        drawPanel(graphics, panel);
    }
}
