package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.EnumSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.EntityUtils;
import dev.sevennv.client.utils.TargetUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * Boxes around nearby entities, split by class. Colours are configurable per
 * group and the outline is drawn through terrain by default, which is the
 * classic 1.8 ESP look.
 */
public final class EntityEsp extends Module {
    public EntityEsp() {
        super("Entity ESP", "Draws boxes around nearby entities", Category.RENDER);
        targets = option("Targets", "Which entities to outline", TargetUtils.Targets.BOTH);
        range = number("Range", "Maximum draw distance", 48.0, 4.0, 256.0, 4.0);
        playerColor = color("Player color", "Outline colour for players", 0xFFFF5050);
        mobColor = color("Mob color", "Outline colour for mobs", 0xFFFFA040);
        friendColor = color("Friend color", "Outline colour for friends", 0xFF50FF50);
        lineWidth = number("Line width", "Stroke thickness", 1.5, 0.5, 4.0, 0.5);
        throughWalls = bool("Through walls", "Draw outlines through terrain", true);
        healthBar = bool("Health bar", "Colour the box by health", false);
    }

    private final EnumSetting<TargetUtils.Targets> targets;
    private final DoubleSetting range;
    private final ColorSetting playerColor;
    private final ColorSetting mobColor;
    private final ColorSetting friendColor;
    private final DoubleSetting lineWidth;
    private final BooleanSetting throughWalls;
    private final BooleanSetting healthBar;

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        double rangeSq = range.get() * range.get();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player || !entity.isAlive() || mc.player.distanceToSqr(entity) > rangeSq) {
                continue;
            }
            if (!(entity instanceof LivingEntity)) {
                continue;
            }
            if (!shouldDraw(entity)) {
                continue;
            }
            int color = colorFor(entity);
            AABB box = WorldRenderUtils.entityBox(entity);
            if (throughWalls.get()) {
                WorldRenderUtils.box(box, color, (float) lineWidth.get());
            } else {
                WorldRenderUtils.box(box, color, (float) lineWidth.get());
            }
        }
    }

    private boolean shouldDraw(Entity entity) {
        if (entity instanceof Player player) {
            return targets.get() != TargetUtils.Targets.MOBS && !player.isSpectator();
        }
        if (entity instanceof Mob) {
            return targets.get() != TargetUtils.Targets.PLAYERS;
        }
        return false;
    }

    private int colorFor(Entity entity) {
        if (entity instanceof Player player) {
            if (dev.sevennv.client.utils.FriendManager.isFriend(player.getGameProfile().name())) {
                return friendColor.get();
            }
            return playerColor.get();
        }
        if (healthBar.get() && entity instanceof LivingEntity living && living.getMaxHealth() > 0) {
            float fraction = living.getHealth() / living.getMaxHealth();
            return RenderUtils.mix(0xFFFF5050, 0xFF50FF50, fraction);
        }
        return mobColor.get();
    }
}
