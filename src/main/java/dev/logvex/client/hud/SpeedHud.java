package dev.logvex.client.hud;

import dev.logvex.client.render.RenderUtils;
import dev.logvex.client.utils.MathUtils;
import net.minecraft.client.gui.GuiGraphics;

/** Horizontal speed in blocks per second. */
public final class SpeedHud extends HudElement {
    public SpeedHud() {
        super("speed", "Speed", 2, 92);
    }

    @Override
    public int getWidth() {
        return RenderUtils.font().width(text()) + 4;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    private String text() {
        return "Speed " + MathUtils.round(HudData.speedBps(), 2) + " b/s";
    }

    @Override
    public void render(GuiGraphics graphics) {
        drawLine(graphics, text(), 0xFFE0E0E0);
    }
}
