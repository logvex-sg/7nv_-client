package dev.logvex.client.combat;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.DoubleSetting;
import dev.logvex.client.config.Theme;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.render.RenderUtils;
import dev.logvex.client.utils.CombatUtils;
import dev.logvex.client.utils.TargetUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Read-only damage readout for the current target: estimated crystal damage,
 * the target's remaining health and whether the hit would be lethal.
 */
public final class DamageCalculator extends Module {
    public DamageCalculator() {
        super("Damage Calculator", "Estimates crystal damage against the target", Category.COMBAT);
        range = number("Range", "Maximum target distance", 8.0, 1.0, 32.0, 1.0);
        includeArmor = bool("Include armor", "Apply armour reduction to the estimate", true);
        crystal = bool("Crystal", "Show crystal damage", true);
        anchor = option("Anchor", "Corner to draw in", TargetHud.Anchor.TOP_LEFT);
    }

    private final DoubleSetting range;
    private final BooleanSetting includeArmor;
    private final BooleanSetting crystal;
    private final dev.logvex.client.config.EnumSetting<TargetHud.Anchor> anchor;

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
        Vec3 centre = target.position().add(0, 1.0, 0);
        float damage = crystal.get()
                ? CombatUtils.estimateExplosionDamage(target, centre, CombatUtils.CRYSTAL_POWER, includeArmor.get())
                : 0f;
        boolean lethal = damage >= target.getHealth();

        int width = 120;
        int height = 28;
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        int x = switch (anchor.get()) {
            case TOP_LEFT, BOTTOM_LEFT -> 4;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - width - 4;
        };
        int y = switch (anchor.get()) {
            case TOP_LEFT, TOP_RIGHT -> 34;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - height - 34;
        };

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);
        RenderUtils.text(graphics, "Damage Estimate", x + 4, y + 3, theme.text);
        String value = String.format(java.util.Locale.ROOT, "%.1f", damage);
        RenderUtils.text(graphics, "Crystal: " + value, x + 4, y + 14,
                lethal ? 0xFF5FBF5F : theme.textDim);
        if (lethal) {
            RenderUtils.textRight(graphics, "LETHAL", x + width - 4, y + 14, 0xFF5FBF5F);
        }
    }

    private Theme theme() {
        var config = dev.logvex.client.LogvexClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
