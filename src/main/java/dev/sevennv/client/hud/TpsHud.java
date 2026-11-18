package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import dev.sevennv.client.utils.TpsTracker;
import net.minecraft.client.gui.GuiGraphics;

/** Server tick-rate estimate derived from world-time packets. */
public final class TpsHud extends HudElement {
    public TpsHud() {
        super("tps", "TPS", 2, 35);
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
        RenderUtils.text(graphics, line(), x + 2, y + 1, color());
    }

    private String line() {
        return "TPS " + TpsTracker.format();
    }

    private int color() {
        double tps = TpsTracker.getTps();
        if (tps >= 19.0) {
            return 0xFF55FF55;
        }
        if (tps >= 15.0) {
            return 0xFFFFFF55;
        }
        return 0xFFFF5555;
    }
}
