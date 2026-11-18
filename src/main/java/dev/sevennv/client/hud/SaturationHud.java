package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Food level together with saturation, which vanilla does not show. */
public final class SaturationHud extends HudElement {
    public SaturationHud() {
        super("saturation", "Saturation", 2, 166);
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
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return "Sat 0.0";
        }
        return String.format("Food %d Sat %.1f", player.getFoodData().getFoodLevel(),
                player.getFoodData().getSaturationLevel());
    }
}
