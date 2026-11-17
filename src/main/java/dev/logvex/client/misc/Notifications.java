package dev.logvex.client.misc;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.IntSetting;
import dev.logvex.client.config.Theme;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.render.RenderUtils;
import dev.logvex.client.utils.NotificationManager;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders a compact history of the client notification stack. The live toasts
 * are drawn by {@link NotificationManager} itself; this module adds an optional
 * scrollback list so a missed alert can still be read.
 */
public final class Notifications extends Module {
    public Notifications() {
        super("Notifications", "Shows a notification history panel", Category.MISC);
        maxRows = integer("Max rows", "How many past notifications to list", 6, 1, 20);
        showTime = bool("Timestamps", "Show when each notification arrived", true);
        onlyErrors = bool("Errors only", "Only list warning and error notifications", false);
    }

    private final IntSetting maxRows;
    private final BooleanSetting showTime;
    private final BooleanSetting onlyErrors;

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Theme theme = theme();
        List<NotificationManager.Entry> entries = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (NotificationManager.Entry entry : NotificationManager.history()) {
            if (onlyErrors.get() && entry.color() != NotificationManager.WARN && entry.color() != NotificationManager.ERROR) {
                continue;
            }
            entries.add(entry);
            if (entries.size() >= maxRows.get()) {
                break;
            }
        }

        int width = 168;
        int height = 14 + Math.max(1, entries.size()) * 9 + 2;
        int x = graphics.guiWidth() - width - 4;
        int y = graphics.guiHeight() - height - 4;

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);
        RenderUtils.text(graphics, "Notifications", x + 3, y + 3, theme.accent);

        int rowY = y + 14;
        if (entries.isEmpty()) {
            RenderUtils.text(graphics, "(none)", x + 3, rowY, theme.textDim);
            return;
        }
        for (NotificationManager.Entry entry : entries) {
            String label = trim(entry.title(), width - (showTime.get() ? 42 : 8));
            RenderUtils.text(graphics, label, x + 3, rowY, RenderUtils.withAlpha(entry.color(), 0xFF));
            if (showTime.get()) {
                long age = (now - entry.created()) / 1000L;
                RenderUtils.textRight(graphics, age + "s", x + width - 3, rowY, theme.textDim);
            }
            rowY += 9;
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
