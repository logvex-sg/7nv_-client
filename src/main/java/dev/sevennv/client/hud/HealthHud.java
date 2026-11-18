package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

/** Health, absorption and food levels. */
public final class HealthHud extends HudElement {
    public HealthHud() {
        super("health", "Health", 110, 57);
    }

    @Override
    public int getWidth() {
        return 96;
    }

    @Override
    public int getHeight() {
        return 20;
    }

    private String healthText() {
        Player player = HudData.mc().player;
        if (player == null) {
            return "Health 0.0";
        }
        String text = "Health " + String.format(java.util.Locale.ROOT, "%.1f", player.getHealth());
        float absorption = player.getAbsorptionAmount();
        if (absorption > 0f) {
            text += " +" + String.format(java.util.Locale.ROOT, "%.1f", absorption);
        }
        return text;
    }

    private String foodText() {
        Player player = HudData.mc().player;
        if (player == null) {
            return "Food 0";
        }
        return "Food " + player.getFoodData().getFoodLevel() + " ("
                + String.format(java.util.Locale.ROOT, "%.1f", player.getFoodData().getSaturationLevel()) + ")";
    }

    @Override
    public void render(GuiGraphics graphics) {
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        RenderUtils.text(graphics, healthText(), x + 1, y + 1, 0xFFFF7070);
        RenderUtils.text(graphics, foodText(), x + 1, y + 11, 0xFFD0A070);
    }
}
