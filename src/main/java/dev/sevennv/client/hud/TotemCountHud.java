package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import dev.sevennv.client.utils.CombatStats;
import net.minecraft.client.gui.GuiGraphics;

/** Total totems of undying carried. */
public final class TotemCountHud extends HudElement {
    public TotemCountHud() {
        super("totemcount", "Totem Count", 2, 112);
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
        return "Totems " + CombatStats.inventoryTotems();
    }
}
