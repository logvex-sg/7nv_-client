package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/** Biome and coordinates on one line, for players who run a single readout. */
public final class BiomeCoordsHud extends HudElement {
    public BiomeCoordsHud() {
        super("biomecoords", "Biome + Coords", 110, 215);
    }

    @Override
    public int getWidth() {
        return Math.max(RenderUtils.font().width(line()) + 4, 120);
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
        return HudData.biomeText() + "  " + HudData.coordsText();
    }
}
