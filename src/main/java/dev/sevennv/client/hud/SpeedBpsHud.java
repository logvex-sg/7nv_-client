package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/** Horizontal speed in blocks per second, with a per-axis breakdown. */
public final class SpeedBpsHud extends HudElement {
    public SpeedBpsHud() {
        super("speed_bps", "Speed (bps)", 110, 177);
    }

    @Override
    public int getWidth() {
        return Math.max(RenderUtils.font().width(line()) + 4, 80);
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        drawLine(graphics, line(), 0xFFE0E0E0);
    }

    private String line() {
        return String.format(java.util.Locale.ROOT, "%.2f b/s", HudData.speedBps());
    }
}
