package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import dev.sevennv.client.utils.CombatStats;
import net.minecraft.client.gui.GuiGraphics;

/** Nearby end-crystal count, useful for crystal PvP awareness. */
public final class CrystalHud extends HudElement {
    public CrystalHud() {
        super("crystals", "Crystals", 2, 101);
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
        RenderUtils.text(graphics, line(), x + 2, y + 1, 0xFFE0E0E0);
    }

    private String line() {
        return "Crystals " + CombatStats.crystalCount(12.0);
    }
}
