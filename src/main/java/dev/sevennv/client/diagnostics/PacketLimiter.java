package dev.sevennv.client.diagnostics;

import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.PacketLog;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Watches the packet rate and reports spikes, which are the usual cause of a
 * sudden frame drop on a busy server. This is purely observational: it counts
 * what already flows through the connection and never delays, drops or throttles
 * a packet.
 */
public final class PacketLimiter extends Module {
    public PacketLimiter() {
        super("Packet Limiter", "Warns when the packet rate spikes", Category.DIAGNOSTICS);
        warnPerSecond = integer("Warn/s", "Packets per second that counts as a spike", 400, 50, 5000);
        windowTicks = integer("Window", "Ticks averaged for the rate", 20, 5, 100);
    }

    private final IntSetting warnPerSecond;
    private final IntSetting windowTicks;

    private long lastInbound;
    private long lastOutbound;
    private int tickCounter;
    private double inboundPerSecond;
    private double outboundPerSecond;
    private double peakPerSecond;
    private int spikeCount;

    @Override
    public void onWorldChange() {
        lastInbound = 0;
        lastOutbound = 0;
        tickCounter = 0;
        inboundPerSecond = 0;
        outboundPerSecond = 0;
        peakPerSecond = 0;
        spikeCount = 0;
    }

    @Override
    public void onTick() {
        if (++tickCounter < windowTicks.get()) {
            return;
        }
        tickCounter = 0;
        long inbound = PacketLog.inboundCount();
        long outbound = PacketLog.outboundCount();
        double elapsed = windowTicks.get() / 20.0;
        inboundPerSecond = (inbound - lastInbound) / elapsed;
        outboundPerSecond = (outbound - lastOutbound) / elapsed;
        lastInbound = inbound;
        lastOutbound = outbound;
        double total = inboundPerSecond + outboundPerSecond;
        peakPerSecond = Math.max(peakPerSecond, total);
        if (total > warnPerSecond.get()) {
            spikeCount++;
        }
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Theme theme = theme();
        double total = inboundPerSecond + outboundPerSecond;
        DiagPanel panel = new DiagPanel().title("Packet Rate");
        panel.row("Inbound", String.format(java.util.Locale.ROOT, "%.0f/s", inboundPerSecond));
        panel.row("Outbound", String.format(java.util.Locale.ROOT, "%.0f/s", outboundPerSecond));
        panel.row("Total", String.format(java.util.Locale.ROOT, "%.0f/s", total));
        panel.row("Peak", String.format(java.util.Locale.ROOT, "%.0f/s", peakPerSecond));
        panel.row("Spikes", String.valueOf(spikeCount));
        panel.row("Status", total > warnPerSecond.get() ? "spike" : "ok");
        panel.render(graphics, theme, 4, 4);
    }

    private Theme theme() {
        var config = dev.sevennv.client.SevennvClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
