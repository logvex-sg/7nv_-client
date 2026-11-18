package dev.sevennv.client.player;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Lists active potion effects with their level and remaining duration. Reads
 * the player's effect list each frame; no state is cached.
 */
public final class PotionHud extends Module {
    public PotionHud() {
        super("Potion HUD", "Shows your active potion effects", Category.PLAYER);
        showDuration = bool("Duration", "Show the remaining time", true);
        showAmplifier = bool("Amplifier", "Show the effect level", true);
        anchorRight = bool("Anchor right", "Draw on the right of the screen", true);
    }

    private final BooleanSetting showDuration;
    private final BooleanSetting showAmplifier;
    private final BooleanSetting anchorRight;

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        if (mc.player == null) {
            return;
        }
        var effects = mc.player.getActiveEffects();
        if (effects.isEmpty()) {
            return;
        }
        Theme theme = theme();
        int width = 130;
        int height = 4 + effects.size() * 10;
        int x = anchorRight.get() ? graphics.guiWidth() - width - 4 : 4;
        int y = graphics.guiHeight() - height - 4;

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);

        int rowY = y + 2;
        for (var effect : effects) {
            StringBuilder builder = new StringBuilder(effect.getEffect().value().getDisplayName().getString());
            if (showAmplifier.get() && effect.getAmplifier() > 0) {
                builder.append(' ').append(dev.sevennv.client.hud.HudFormat.toRoman(effect.getAmplifier() + 1));
            }
            RenderUtils.text(graphics, builder.toString(), x + 3, rowY + 1, theme.text);
            if (showDuration.get()) {
                RenderUtils.textRight(graphics, dev.sevennv.client.hud.HudFormat.effectDuration(effect),
                        x + width - 3, rowY + 1, theme.textDim);
            }
            rowY += 10;
        }
    }

    private Theme theme() {
        var config = dev.sevennv.client.SevennvClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
