package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/** Horizontal speed with a small bar so the magnitude is visible at a glance. */
public final class SpeedBarHud extends HudElement {
    private static final int BAR_WIDTH = 40;

    public SpeedBarHud() {
        super("speedbar", "Speed Bar", 2, 200);
    }

    @Override
    public int getWidth() {
        return BAR_WIDTH + 40;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        double bps = HudData.speedBps();
        String label = String.format(java.util.Locale.ROOT, "%.1f b/s", bps);
        RenderUtils.rect(graphics, x, y, BAR_WIDTH, 8, background());
        int filled = (int) Math.min(BAR_WIDTH, bps * 4.0);
        RenderUtils.rect(graphics, x, y, filled, 8, 0xFF40A0FF);
        RenderUtils.text(graphics, label, x + BAR_WIDTH + 2, y, 0xFFE0E0E0);
    }
}
