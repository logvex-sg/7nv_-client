package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.EnumSetting;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.hud.HudFormat;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.render.RenderUtils;
import dev.sevennv.client.utils.TargetUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Floating target readout: name, health bar and distance for the current
 * crosshair target. Drawn in GUI space at a fixed anchor, so it stays visible
 * regardless of the world render state.
 */
public final class TargetHud extends Module {
    public TargetHud() {
        super("Target HUD", "Shows the current target's health and distance", Category.COMBAT);
        anchor = option("Anchor", "Corner to draw in", Anchor.BOTTOM_LEFT);
        showHealth = bool("Health bar", "Draw a health bar", true);
        showDistance = bool("Distance", "Show the distance to the target", true);
        showArmor = bool("Armor", "Show the target's armour points", true);
        range = number("Range", "Maximum tracking distance", 12.0, 1.0, 64.0, 1.0);
    }

    public enum Anchor {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    private final EnumSetting<Anchor> anchor;
    private final BooleanSetting showHealth;
    private final BooleanSetting showDistance;
    private final BooleanSetting showArmor;
    private final dev.sevennv.client.config.DoubleSetting range;

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        if (mc.player == null || mc.level == null) {
            return;
        }
        LivingEntity target = TargetUtils.crosshairTarget();
        if (target == null) {
            target = TargetUtils.best(range.get(), TargetUtils.Targets.BOTH, TargetUtils.Sort.DISTANCE);
        }
        if (target == null) {
            return;
        }
        Theme theme = theme();
        String name = target instanceof Player player
                ? player.getGameProfile().name()
                : dev.sevennv.client.utils.EntityUtils.label(target);
        String healthText = HudFormat.toRoman(0) + (int) target.getHealth() + "/" + (int) target.getMaxHealth();
        String distanceText = String.format(java.util.Locale.ROOT, "%.1f m", mc.player.distanceTo(target));

        int width = 130;
        int height = 30;
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        int x = switch (anchor.get()) {
            case TOP_LEFT, BOTTOM_LEFT -> 4;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - width - 4;
        };
        int y = switch (anchor.get()) {
            case TOP_LEFT, TOP_RIGHT -> 4;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - height - 4;
        };

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);
        RenderUtils.text(graphics, name, x + 4, y + 3, theme.text);
        if (showDistance.get()) {
            RenderUtils.textRight(graphics, distanceText, x + width - 4, y + 3, theme.textDim);
        }
        if (showHealth.get()) {
            float fraction = target.getMaxHealth() <= 0 ? 0f : target.getHealth() / target.getMaxHealth();
            fraction = Math.max(0f, Math.min(1f, fraction));
            int barWidth = width - 8;
            RenderUtils.rect(graphics, x + 4, y + 15, barWidth, 5, theme.sliderTrack);
            RenderUtils.rect(graphics, x + 4, y + 15, (int) (barWidth * fraction), 5, healthColor(fraction));
            RenderUtils.text(graphics, healthText, x + 4, y + 22, theme.textDim);
        }
        if (showArmor.get()) {
            RenderUtils.textRight(graphics, "Armor " + (int) target.getArmorValue(), x + width - 4, y + 22, theme.textDim);
        }
    }

    private static int healthColor(float fraction) {
        if (fraction > 0.6f) {
            return 0xFF5FBF5F;
        }
        if (fraction > 0.3f) {
            return 0xFFFFC040;
        }
        return 0xFFFF5050;
    }

    private Theme theme() {
        var config = dev.sevennv.client.SevennvClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
