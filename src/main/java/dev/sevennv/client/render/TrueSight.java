package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Outlines invisible entities so you can still see where they are. Only a
 * client-side box is drawn; the entity's visibility to the server and the
 * rendering of everyone else are unchanged.
 */
public final class TrueSight extends Module {
    public TrueSight() {
        super("True Sight", "Outlines invisible entities", Category.RENDER);
        range = number("Range", "Maximum draw distance", 32.0, 4.0, 128.0, 4.0);
        color = color("Color", "Outline colour", 0xFFAAAAFF);
        lineWidth = number("Line width", "Stroke thickness", 1.5, 0.5, 4.0, 0.5);
        playersOnly = bool("Players only", "Only outline invisible players", true);
    }

    private final DoubleSetting range;
    private final ColorSetting color;
    private final DoubleSetting lineWidth;
    private final BooleanSetting playersOnly;

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        double rangeSq = range.get() * range.get();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || !living.isInvisible()) {
                continue;
            }
            if (entity == mc.player || mc.player.distanceToSqr(entity) > rangeSq) {
                continue;
            }
            if (playersOnly.get() && !(entity instanceof net.minecraft.world.entity.player.Player)) {
                continue;
            }
            WorldRenderUtils.box(WorldRenderUtils.entityBox(entity), color.get(), (float) lineWidth.get());
        }
    }
}
