package dev.logvex.client.hud;

import dev.logvex.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/** In-game world clock and day counter. */
public final class TimeHud extends HudElement {
    public TimeHud() {
        super("time", "World Time", 2, 182);
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
        String phase = HudData.isNight() ? "Night" : "Day";
        long day = HudData.mc().level == null ? 0 : HudData.mc().level.getDayTime() / 24000L;
        return HudData.worldTimeText() + " (" + phase + " " + day + ")";
    }

    @Override
    public void render(GuiGraphics graphics) {
        drawLine(graphics, text(), 0xFFE0D090);
    }
}
