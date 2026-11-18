package dev.sevennv.client.diagnostics;

import dev.sevennv.client.config.Theme;
import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Small flat panel builder shared by the diagnostics overlays. Rows are added
 * first and the panel is measured once, so a changing value never makes the
 * background flicker between frames.
 */
public final class DiagPanel {
    private final List<String[]> rows = new ArrayList<>();
    private String title;

    public DiagPanel title(String title) {
        this.title = title;
        return this;
    }

    public DiagPanel row(String label, String value) {
        rows.add(new String[]{label, value});
        return this;
    }

    public int width() {
        int width = 120;
        for (String[] row : rows) {
            width = Math.max(width, RenderUtils.font().width(row[0]) + RenderUtils.font().width(row[1]) + 16);
        }
        if (title != null) {
            width = Math.max(width, RenderUtils.font().width(title) + 8);
        }
        return width;
    }

    public int height() {
        return 14 + Math.max(1, rows.size()) * 9 + 2;
    }

    /** Draws the panel at the given position with the default text colours. */
    public void render(GuiGraphics graphics, Theme theme, int x, int y) {
        int width = width();
        int height = height();
        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);
        if (title != null) {
            RenderUtils.text(graphics, title, x + 3, y + 3, theme.accent);
        }
        int rowY = y + 14;
        if (rows.isEmpty()) {
            RenderUtils.text(graphics, "(no data)", x + 3, rowY, theme.textDim);
            return;
        }
        for (String[] row : rows) {
            RenderUtils.text(graphics, row[0], x + 3, rowY, theme.textDim);
            RenderUtils.textRight(graphics, row[1], x + width - 3, rowY, theme.text);
            rowY += 9;
        }
    }
}
