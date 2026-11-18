package dev.sevennv.client.player;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Health, absorption and hunger readout as a small bar panel. The HUD health
 * element shows the same values in the movable HUD layer; this module is the
 * fixed-position variant.
 */
public final class HealthHud extends Module {
    public HealthHud() {
        super("Health HUD", "Shows your health and hunger", Category.PLAYER);
        showAbsorption = bool("Absorption", "Show absorption hearts", true);
        showHunger = bool("Hunger", "Show the hunger bar", true);
        showNumbers = bool("Numbers", "Show the numeric values", true);
    }

    private final BooleanSetting showAbsorption;
    private final BooleanSetting showHunger;
    private final BooleanSetting showNumbers;

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        if (mc.player == null) {
            return;
        }
        Theme theme = theme();
        int width = 116;
        int height = showHunger.get() ? 34 : 22;
        int x = 4;
        int y = graphics.guiHeight() - height - 4;

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);

        float health = mc.player.getHealth();
        float maxHealth = Math.max(1f, mc.player.getMaxHealth());
        int rowY = y + 4;
        RenderUtils.text(graphics, "Health", x + 4, rowY, theme.text);
        if (showNumbers.get()) {
            RenderUtils.textRight(graphics, trim(health) + "/" + trim(maxHealth), x + width - 4, rowY, theme.textDim);
        }
        rowY += 10;
        RenderUtils.rect(graphics, x + 4, rowY, width - 8, 5, theme.sliderTrack);
        RenderUtils.rect(graphics, x + 4, rowY, (int) ((width - 8) * (health / maxHealth)), 5, 0xFFFF5050);

        if (showAbsorption.get() && mc.player.getAbsorptionAmount() > 0) {
            rowY += 7;
            RenderUtils.rect(graphics, x + 4, rowY, (int) ((width - 8) * Math.min(1f, mc.player.getAbsorptionAmount() / 20f)),
                    3, 0xFFFFC040);
        }

        if (showHunger.get()) {
            rowY = y + 22;
            RenderUtils.text(graphics, "Food", x + 4, rowY, theme.text);
            if (showNumbers.get()) {
                RenderUtils.textRight(graphics, String.valueOf(mc.player.getFoodData().getFoodLevel()),
                        x + width - 4, rowY, theme.textDim);
            }
            RenderUtils.rect(graphics, x + 4, rowY + 10, width - 8, 4, theme.sliderTrack);
            RenderUtils.rect(graphics, x + 4, rowY + 10,
                    (int) ((width - 8) * (mc.player.getFoodData().getFoodLevel() / 20f)), 4, 0xFFC09040);
        }
    }

    private static String trim(float value) {
        return String.valueOf(Math.round(value));
    }

    private Theme theme() {
        var config = dev.sevennv.client.SevennvClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
