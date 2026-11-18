package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/** World time with a sun/moon indicator, distinct from the local wall clock. */
public final class WorldClockHud extends HudElement {
    public WorldClockHud() {
        super("worldclock", "World Clock", 110, 226);
    }

    @Override
    public int getWidth() {
        return Math.max(RenderUtils.font().width(line()) + 4, 84);
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        drawLine(graphics, line(), HudData.isNight() ? 0xFFA0A0FF : 0xFFFFE080);
    }

    private String line() {
        return (HudData.isNight() ? "Moon " : "Sun ") + HudData.worldTimeText();
    }
}
