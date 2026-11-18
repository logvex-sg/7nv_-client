package dev.sevennv.client.diagnostics;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.PacketLog;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Reports how many packets per second the connection is carrying in each
 * direction, plus a running average. The counters are the same read-only totals
 * the packet inspector uses; nothing is measured by sending traffic.
 */
public final class PacketRate extends Module {
    public PacketRate() {
        super("Packet Rate", "Shows packets per second in each direction", Category.DIAGNOSTICS);
        window = integer("Window", "Milliseconds between rate samples", 1000, 250, 5000);
        showAverage = bool("Average", "Also show the session average", true);
    }

    private final dev.sevennv.client.config.IntSetting window;
    private final BooleanSetting showAverage;

    private long lastSample = System.currentTimeMillis();
    private long lastInbound;
    private long lastOutbound;
    private double inboundRate;
    private double outboundRate;
    private double inboundAverage;
    private double outboundAverage;
    private int samples;

    @Override
    public void onWorldChange() {
        reset();
    }

    private void reset() {
        lastSample = System.currentTimeMillis();
        lastInbound = PacketLog.inboundCount();
        lastOutbound = PacketLog.outboundCount();
        inboundRate = 0;
        outboundRate = 0;
        inboundAverage = 0;
        outboundAverage = 0;
        samples = 0;
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        long now = System.currentTimeMillis();
        long elapsed = now - lastSample;
        if (elapsed >= window.get()) {
            long inbound = PacketLog.inboundCount();
            long outbound = PacketLog.outboundCount();
            double seconds = elapsed / 1000.0;
            inboundRate = (inbound - lastInbound) / seconds;
            outboundRate = (outbound - lastOutbound) / seconds;
            samples++;
            inboundAverage += (inboundRate - inboundAverage) / samples;
            outboundAverage += (outboundRate - outboundAverage) / samples;
            lastInbound = inbound;
            lastOutbound = outbound;
            lastSample = now;
        }

        Theme theme = theme();
        DiagPanel panel = new DiagPanel().title("Packet Rate");
        panel.row("Inbound/s", String.format(java.util.Locale.ROOT, "%.1f", inboundRate));
        panel.row("Outbound/s", String.format(java.util.Locale.ROOT, "%.1f", outboundRate));
        if (showAverage.get()) {
            panel.row("Avg in", String.format(java.util.Locale.ROOT, "%.1f", inboundAverage));
            panel.row("Avg out", String.format(java.util.Locale.ROOT, "%.1f", outboundAverage));
        }
        panel.row("Totals", PacketLog.inboundCount() + " / " + PacketLog.outboundCount());
        panel.render(graphics, theme, 4, 4);
    }

    private Theme theme() {
        var config = dev.sevennv.client.SevennvClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
