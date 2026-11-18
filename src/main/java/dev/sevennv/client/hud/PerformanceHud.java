package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/** Top-left readout of FPS, ping and CPS. */
public final class PerformanceHud extends HudElement {
    public PerformanceHud() {
        super("performance", "Performance", 2, 2);
    }

    @Override
    public int getWidth() {
        return 90;
    }

    @Override
    public int getHeight() {
        return 30;
    }

    @Override
    public void render(GuiGraphics graphics) {
        int cps = dev.sevennv.client.utils.CpsTracker.getCps();
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        RenderUtils.text(graphics, "FPS " + HudData.mc().getFps(), x + 2, y + 1, 0xFFE0E0E0);
        RenderUtils.text(graphics, "Ping " + HudData.pingText(), x + 2, y + 11, 0xFFE0E0E0);
        RenderUtils.text(graphics, "CPS " + cps, x + 2, y + 21, 0xFFE0E0E0);
    }
}
