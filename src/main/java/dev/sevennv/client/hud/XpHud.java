package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

/** Experience level, progress toward the next level and total points. */
public final class XpHud extends HudElement {
    public XpHud() {
        super("xp", "Experience", 110, 144);
    }

    @Override
    public int getWidth() {
        return Math.max(RenderUtils.font().width(line()) + 4, 80);
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        drawLine(graphics, line(), 0xFF80FF80);
    }

    private String line() {
        Player player = HudData.mc().player;
        if (player == null) {
            return "XP -";
        }
        return "XP " + player.experienceLevel + " (" + Math.round(player.experienceProgress * 100f) + "%)";
    }
}
