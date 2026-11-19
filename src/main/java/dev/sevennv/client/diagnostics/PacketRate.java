package dev.sevennv.client.diagnostics;

import dev.sevennv.client.config.Theme;
import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.utils.PacketLog;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Reports how many packets per second the connection is carrying in each
 * direction, plus a running average. The counters are the same read-only totals
 * the packet inspector uses; nothing is measured by sending traffic.
 */
public final class PacketRate extends DiagModule {
    public PacketRate() {
        super("Packet Rate", "Shows packets per second in each direction");
        window = integer("Window", "Milliseconds between rate samples", 1000, 250, 5000);
        showAverage = bool("Average", "Also show the session average", true);
        warnPerSecond = integer("Warn/s", "Total packets per second that counts as a spike", 400, 50, 5000);
        nameCause = bool("Name cause", "List the busiest packet type during a spike", true);
    }

    private final dev.sevennv.client.config.IntSetting window;
    private final BooleanSetting showAverage;
    private final dev.sevennv.client.config.IntSetting warnPerSecond;
    private final BooleanSetting nameCause;

    private long lastSample = System.currentTimeMillis();
    private long lastInbound;
    private long lastOutbound;
    private double inboundRate;
    private double outboundRate;
    private double inboundAverage;
    private double outboundAverage;
    private double peakRate;
    private int spikeCount;
    private String lastCause = "";
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
        peakRate = 0;
        spikeCount = 0;
        lastCause = "";
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

            double total = inboundRate + outboundRate;
            peakRate = Math.max(peakRate, total);
            if (total > warnPerSecond.get()) {
                spikeCount++;
                if (nameCause.get()) {
                    // Naming the busiest type is what makes a spike actionable:
                    // a chunk burst and an entity burst need different fixes.
                    lastCause = busiest();
                }
            }
        }

        Theme theme = theme();
        DiagPanel panel = new DiagPanel().title("Packet Rate");
        panel.row("Inbound/s", String.format(java.util.Locale.ROOT, "%.1f", inboundRate));
        panel.row("Outbound/s", String.format(java.util.Locale.ROOT, "%.1f", outboundRate));
        if (showAverage.get()) {
            panel.row("Avg in", String.format(java.util.Locale.ROOT, "%.1f", inboundAverage));
            panel.row("Avg out", String.format(java.util.Locale.ROOT, "%.1f", outboundAverage));
        }
        panel.row("Peak/s", String.format(java.util.Locale.ROOT, "%.0f", peakRate));
        if (spikeCount > 0) {
            panel.row("Spikes", String.valueOf(spikeCount));
            if (nameCause.get() && !lastCause.isEmpty()) {
                panel.row("  cause", lastCause);
            }
        }
        panel.row("Totals", PacketLog.inboundCount() + " / " + PacketLog.outboundCount());
        drawPanel(graphics, panel);
    }

    /** Busiest inbound packet type this session, with its count. */
    private static String busiest() {
        java.util.List<java.util.Map.Entry<String, Long>> top = PacketLog.topInbound(1);
        if (top.isEmpty()) {
            return "";
        }
        java.util.Map.Entry<String, Long> entry = top.get(0);
        return entry.getKey() + " x" + entry.getValue();
    }
}
