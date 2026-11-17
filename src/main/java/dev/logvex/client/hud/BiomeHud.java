package dev.logvex.client.hud;

import dev.logvex.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/** Biome and dimension readout. */
public final class BiomeHud extends HudElement {
    public BiomeHud() {
        super("biome", "Biome", 2, 58);
    }

    @Override
    public int getWidth() {
        return RenderUtils.font().width(HudData.biomeText()) + 4;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        drawLine(graphics, HudData.biomeText(), 0xFFB0D0B0);
    }
}
