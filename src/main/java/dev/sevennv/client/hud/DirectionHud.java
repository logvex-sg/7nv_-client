package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

/** Compass direction and axis readout. */
public final class DirectionHud extends HudElement {
    public DirectionHud() {
        super("direction", "Direction", 110, 24);
    }

    @Override
    public int getWidth() {
        return RenderUtils.font().width("Dir " + HudData.directionText()) + 4;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        String axis = "";
        Player player = HudData.mc().player;
        if (player != null) {
            axis = " [" + dev.sevennv.client.utils.WorldUtils.axisLabel(player) + "]";
        }
        drawLine(graphics, "Dir " + HudData.directionText() + axis, 0xFFE0E0E0);
    }
}
