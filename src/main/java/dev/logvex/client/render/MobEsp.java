package dev.logvex.client.render;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.ColorSetting;
import dev.logvex.client.config.DoubleSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import net.minecraft.world.entity.Mob;

/**
 * Boxes around hostile and passive mobs. The two groups have separate colours
 * so an anarchy fight stays readable.
 */
public final class MobEsp extends Module {
    public MobEsp() {
        super("Mob ESP", "Outlines nearby mobs", Category.RENDER);
        range = number("Range", "Maximum draw distance", 48.0, 4.0, 256.0, 4.0);
        hostileColor = color("Hostile color", "Colour for hostile mobs", 0xFFFFA040);
        passiveColor = color("Passive color", "Colour for passive mobs", 0xFF50A0FF);
        lineWidth = number("Line width", "Stroke thickness", 1.5, 0.5, 4.0, 0.5);
        hostilesOnly = bool("Hostiles only", "Skip passive mobs", false);
    }

    private final DoubleSetting range;
    private final ColorSetting hostileColor;
    private final ColorSetting passiveColor;
    private final DoubleSetting lineWidth;
    private final BooleanSetting hostilesOnly;

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        double rangeSq = range.get() * range.get();
        for (Mob mob : mc.level.getEntitiesOfClass(Mob.class, mc.player.getBoundingBox().inflate(range.get()))) {
            if (!mob.isAlive() || mc.player.distanceToSqr(mob) > rangeSq) {
                continue;
            }
            boolean hostile = mob instanceof net.minecraft.world.entity.monster.Enemy;
            if (hostilesOnly.get() && !hostile) {
                continue;
            }
            int outline = hostile ? hostileColor.get() : passiveColor.get();
            WorldRenderUtils.box(WorldRenderUtils.entityBox(mob), outline, (float) lineWidth.get());
        }
    }
}
