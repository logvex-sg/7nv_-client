package dev.logvex.client.render;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.ColorSetting;
import dev.logvex.client.config.DoubleSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * Draws a health bar above nearby living entities, complementing the nametag
 * module. The bar is a world-space rectangle above the entity's head.
 */
public final class HealthBars extends Module {
    public HealthBars() {
        super("Health Bars", "Draws health bars above entities", Category.RENDER);
        range = number("Range", "Maximum draw distance", 48.0, 4.0, 256.0, 4.0);
        width = number("Width", "Bar width in blocks", 0.8, 0.2, 2.0, 0.1);
        playersOnly = bool("Players only", "Only draw bars above players", false);
        lowColor = color("Low color", "Colour when health is low", 0xFFFF5050);
        highColor = color("High color", "Colour when health is high", 0xFF50FF50);
        background = color("Background", "Bar background colour", 0x80000000);
    }

    private final DoubleSetting range;
    private final DoubleSetting width;
    private final BooleanSetting playersOnly;
    private final ColorSetting lowColor;
    private final ColorSetting highColor;
    private final ColorSetting background;

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        double rangeSq = range.get() * range.get();
        for (net.minecraft.world.entity.Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || entity == mc.player || !living.isAlive()) {
                continue;
            }
            if (playersOnly.get() && !(entity instanceof Player)) {
                continue;
            }
            if (mc.player.distanceToSqr(entity) > rangeSq || living.getMaxHealth() <= 0) {
                continue;
            }
            float fraction = Math.max(0f, Math.min(1f, living.getHealth() / living.getMaxHealth()));
            AABB box = living.getBoundingBox();
            double centreX = (box.minX + box.maxX) / 2.0;
            double centreZ = (box.minZ + box.maxZ) / 2.0;
            double y = box.maxY + 0.25;
            double half = width.get() / 2.0;
            double thickness = 0.08;
            int barColor = RenderUtils.mix(lowColor.get(), highColor.get(), fraction);
            WorldRenderUtils.filledBox(
                    new AABB(centreX - half, y, centreZ - half, centreX + half, y + thickness, centreZ + half),
                    background.get(), 1.0f, background.get());
            double filled = width.get() * fraction;
            WorldRenderUtils.filledBox(
                    new AABB(centreX - half, y + 0.001, centreZ - half, centreX - half + filled, y + thickness + 0.001, centreZ + half),
                    barColor, 1.0f, barColor);
        }
    }
}
