package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * Generic entity boxes for every living entity in range, with a single colour.
 * {@link PlayerEsp} and {@link MobEsp} are the class-specialised variants.
 */
public final class EntityBoxes extends Module {
    public EntityBoxes() {
        super("Entity Boxes", "Outlines all living entities", Category.RENDER);
        range = number("Range", "Maximum draw distance", 48.0, 4.0, 256.0, 4.0);
        color = color("Color", "Outline colour", 0xFFFFFFFF);
        lineWidth = number("Line width", "Stroke thickness", 1.0, 0.5, 4.0, 0.5);
        skipSelf = bool("Skip self", "Never outline the local player", true);
    }

    private final DoubleSetting range;
    private final ColorSetting color;
    private final DoubleSetting lineWidth;
    private final BooleanSetting skipSelf;

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        double rangeSq = range.get() * range.get();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity) || !entity.isAlive()) {
                continue;
            }
            if (skipSelf.get() && entity == mc.player) {
                continue;
            }
            if (mc.player.distanceToSqr(entity) > rangeSq) {
                continue;
            }
            AABB box = WorldRenderUtils.entityBox(entity);
            WorldRenderUtils.box(box, color.get(), (float) lineWidth.get());
        }
    }
}
