package dev.logvex.client.diagnostics;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.IntSetting;
import dev.logvex.client.config.Theme;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.render.RenderUtils;
import dev.logvex.client.utils.PacketLog;
import dev.logvex.client.events.PacketEvent;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Observational packet inspector: lists the most recent packets with their
 * direction. Nothing is dropped, delayed or modified, so this is a pure
 * read-only window on traffic the client already sends and receives.
 */
public final class PacketInspector extends Module {
    public PacketInspector() {
        super("Packet Inspector", "Lists recent packets in both directions", Category.DIAGNOSTICS);
        maxLines = integer("Max lines", "How many packets to list", 12, 1, 32);
        showOutbound = bool("Outbound", "Include packets the client sent", true);
        showInbound = bool("Inbound", "Include packets the client received", true);
        showCounters = bool("Counters", "Show the running totals", true);
    }

    private final IntSetting maxLines;
    private final BooleanSetting showOutbound;
    private final BooleanSetting showInbound;
    private final BooleanSetting showCounters;

    private final List<String> lines = new ArrayList<>();

    @Override
    public void onPacket(PacketEvent event) {
        boolean outbound = event.getDirection() == PacketEvent.Direction.OUTBOUND;
        if ((outbound && !showOutbound.get()) || (!outbound && !showInbound.get())) {
            return;
        }
        lines.add((outbound ? "\u2191 " : "\u2193 ") + event.getPacketName());
        while (lines.size() > maxLines.get()) {
            lines.remove(0);
        }
    }

    @Override
    public void onWorldChange() {
        lines.clear();
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Theme theme = theme();
        int width = 178;
        int rows = lines.size();
        int height = 14 + Math.max(1, rows) * 9 + (showCounters.get() ? 10 : 0);
        int x = 4;
        int y = 4;

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);
        RenderUtils.text(graphics, "Packet Inspector", x + 3, y + 3, theme.accent);

        int rowY = y + 14;
        if (lines.isEmpty()) {
            RenderUtils.text(graphics, "(no packets)", x + 3, rowY, theme.textDim);
        } else {
            for (String line : lines) {
                int color = line.startsWith("\u2191") ? 0xFFC0A050 : 0xFF70A0D0;
                RenderUtils.text(graphics, trim(line, width - 6), x + 3, rowY, color);
                rowY += 9;
            }
        }
        if (showCounters.get()) {
            RenderUtils.text(graphics, "\u2193 " + PacketLog.inboundCount() + "  \u2191 " + PacketLog.outboundCount(),
                    x + 3, y + height - 10, theme.textDim);
        }
    }

    private static String trim(String value, int maxWidth) {
        while (RenderUtils.font().width(value) > maxWidth && value.length() > 3) {
            value = value.substring(0, value.length() - 2) + "\u2026";
        }
        return value;
    }

    private Theme theme() {
        var config = dev.logvex.client.LogvexClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
