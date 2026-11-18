package dev.sevennv.client.diagnostics;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.render.RenderUtils;
import dev.sevennv.client.utils.PacketLog;
import dev.sevennv.client.utils.Timer;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.Map;

/**
 * Packet statistics: totals, per-second rates and the most common packet types
 * in each direction. Rates are sampled once a second from the counters the
 * packet observer maintains, so the module adds no per-packet cost of its own.
 */
public final class PacketStatistics extends Module {
    public PacketStatistics() {
        super("Packet Statistics", "Shows packet totals and rates", Category.DIAGNOSTICS);
        topCount = integer("Top types", "How many packet types to list per direction", 4, 0, 10);
        showRates = bool("Rates", "Show per-second rates", true);
        showInbound = bool("Inbound", "Include inbound statistics", true);
        showOutbound = bool("Outbound", "Include outbound statistics", true);
    }

    private final IntSetting topCount;
    private final BooleanSetting showRates;
    private final BooleanSetting showInbound;
    private final BooleanSetting showOutbound;

    private final Timer sampleTimer = new Timer();
    private long lastInbound;
    private long lastOutbound;
    private double inboundRate;
    private double outboundRate;

    @Override
    public void onTick() {
        if (!sampleTimer.tick(1000)) {
            return;
        }
        long inbound = PacketLog.inboundCount();
        long outbound = PacketLog.outboundCount();
        inboundRate = inbound - lastInbound;
        outboundRate = outbound - lastOutbound;
        lastInbound = inbound;
        lastOutbound = outbound;
    }

    @Override
    public void onWorldChange() {
        lastInbound = 0;
        lastOutbound = 0;
        inboundRate = 0;
        outboundRate = 0;
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Theme theme = theme();
        int width = 168;
        int rows = 2 + (showRates.get() ? 2 : 0) + topCount.get() * 2;
        int height = 14 + rows * 9;
        int x = graphics.guiWidth() - width - 4;
        int y = 4;

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);
        RenderUtils.text(graphics, "Packet Statistics", x + 3, y + 3, theme.accent);

        int rowY = y + 14;
        if (showInbound.get()) {
            rowY = line(graphics, theme, x, rowY, width, "Inbound", String.valueOf(PacketLog.inboundCount()));
            if (showRates.get()) {
                rowY = line(graphics, theme, x, rowY, width, "  /s", trim1(inboundRate));
            }
            rowY = list(graphics, theme, x, rowY, width, PacketLog.topInbound(topCount.get()));
        }
        if (showOutbound.get()) {
            rowY = line(graphics, theme, x, rowY, width, "Outbound", String.valueOf(PacketLog.outboundCount()));
            if (showRates.get()) {
                rowY = line(graphics, theme, x, rowY, width, "  /s", trim1(outboundRate));
            }
            list(graphics, theme, x, rowY, width, PacketLog.topOutbound(topCount.get()));
        }
    }

    private int line(GuiGraphics graphics, Theme theme, int x, int rowY, int width, String label, String value) {
        RenderUtils.text(graphics, label, x + 3, rowY, theme.textDim);
        RenderUtils.textRight(graphics, value, x + width - 3, rowY, theme.text);
        return rowY + 9;
    }

    private int list(GuiGraphics graphics, Theme theme, int x, int rowY, int width, List<Map.Entry<String, Long>> entries) {
        for (Map.Entry<String, Long> entry : entries) {
            RenderUtils.text(graphics, "  " + entry.getKey(), x + 3, rowY, theme.text);
            RenderUtils.textRight(graphics, String.valueOf(entry.getValue()), x + width - 3, rowY, theme.textDim);
            rowY += 9;
        }
        return rowY;
    }

    private static String trim1(double value) {
        return String.valueOf(Math.round(value));
    }

    private Theme theme() {
        var config = dev.sevennv.client.SevennvClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
