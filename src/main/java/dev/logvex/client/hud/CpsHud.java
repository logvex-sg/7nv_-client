package dev.logvex.client.hud;

import dev.logvex.client.render.RenderUtils;
import dev.logvex.client.utils.CpsTracker;
import net.minecraft.client.gui.GuiGraphics;

/** Clicks per second readout. */
public final class CpsHud extends HudElement {
    public CpsHud() {
        super("cps", "CPS", 2, 24);
    }

    @Override
    public int getWidth() {
        return RenderUtils.font().width(line()) + 4;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        RenderUtils.text(graphics, line(), x + 2, y + 1, 0xFFE0E0E0);
    }

    private String line() {
        return "CPS " + CpsTracker.getCps();
    }
}
