package dev.logvex.client.hud;

import dev.logvex.client.render.RenderUtils;
import dev.logvex.client.utils.MathUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

/** Coordinate readout with optional chunk offset. */
public final class CoordinatesHud extends HudElement {
    public CoordinatesHud() {
        super("coordinates", "Coordinates", 2, 34);
    }

    @Override
    public int getWidth() {
        return RenderUtils.font().width(HudData.coordsText()) + 4;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        drawLine(graphics, HudData.coordsText(), 0xFFE0E0E0);
    }
}
