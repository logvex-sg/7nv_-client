package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.food.FoodData;

/** Food and saturation readout for the local player. */
public final class FoodHud extends HudElement {
    public FoodHud() {
        super("food", "Food", 110, 46);
    }

    @Override
    public int getWidth() {
        return 78;
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    public void render(GuiGraphics graphics) {
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        if (HudData.mc().player == null) {
            RenderUtils.text(graphics, "Food --", x + 2, y + 1, 0xFFE0E0E0);
            return;
        }
        FoodData food = HudData.mc().player.getFoodData();
        int level = food.getFoodLevel();
        RenderUtils.text(graphics, "Food " + level, x + 2, y + 1, 0xFFE0E0E0);
        RenderUtils.text(graphics, "Sat " + Math.round(food.getSaturationLevel()), x + 2, y + 11, 0xFFB0B0B0);
    }
}
