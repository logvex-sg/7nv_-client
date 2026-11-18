package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

/** One-line combat summary: health, absorption and armour. */
public final class CombatStatsHud extends HudElement {
    public CombatStatsHud() {
        super("combat_stats", "Combat Stats", 2, 210);
    }

    @Override
    public int getWidth() {
        return Math.max(RenderUtils.font().width(line()) + 4, 110);
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        drawLine(graphics, line(), 0xFFFFA0A0);
    }

    private String line() {
        Player player = HudData.mc().player;
        if (player == null) {
            return "HP -  AP -";
        }
        return String.format(java.util.Locale.ROOT, "HP %.1f (+%.1f)  AP %d",
                player.getHealth(), player.getAbsorptionAmount(), player.getArmorValue());
    }
}
