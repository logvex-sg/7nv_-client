package dev.logvex.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Target selection shared by the combat modules. */
public final class TargetUtils {
    /** Which entity classes a module considers attackable. */
    public enum Targets {
        PLAYERS,
        MOBS,
        BOTH
    }

    /** How the best target is chosen from the candidates. */
    public enum Sort {
        DISTANCE,
        HEALTH,
        ANGLE
    }

    private TargetUtils() {
    }

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    public static boolean matches(Entity entity, Targets mode) {
        Minecraft mc = mc();
        if (entity == null || entity == mc.player || !entity.isAlive()) {
            return false;
        }
        if (entity instanceof Player player) {
            if (player.isSpectator() || player.isCreative()) {
                return false;
            }
            if (FriendManager.isFriend(player.getGameProfile().name())) {
                return false;
            }
            return mode == Targets.PLAYERS || mode == Targets.BOTH;
        }
        if (entity instanceof Mob mob) {
            if (mode == Targets.PLAYERS) {
                return false;
            }
            // In MOBS mode only hostiles count; BOTH also accepts hostiles.
            return mob instanceof Enemy;
        }
        return false;
    }

    /** Every attackable living entity within {@code range} blocks. */
    public static List<LivingEntity> candidates(double range, Targets mode) {
        List<LivingEntity> result = new ArrayList<>();
        Minecraft mc = mc();
        if (mc.level == null || mc.player == null) {
            return result;
        }
        double rangeSq = range * range;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity living && matches(living, mode)
                    && mc.player.distanceToSqr(living) <= rangeSq) {
                result.add(living);
            }
        }
        return result;
    }

    /** The single best target, or null when nothing matches. */
    public static LivingEntity best(double range, Targets mode, Sort sort) {
        List<LivingEntity> list = candidates(range, mode);
        if (list.isEmpty()) {
            return null;
        }
        return switch (sort) {
            case DISTANCE -> list.stream()
                    .min(Comparator.comparingDouble(PlayerUtils::distanceSq)).orElse(null);
            case HEALTH -> list.stream()
                    .min(Comparator.comparingDouble(LivingEntity::getHealth)).orElse(null);
            case ANGLE -> list.stream().min(Comparator.comparingDouble(TargetUtils::angleDelta)).orElse(null);
        };
    }

    /** Absolute yaw difference between the local player and the entity. */
    public static double angleDelta(LivingEntity entity) {
        Minecraft mc = mc();
        if (mc.player == null) {
            return Double.MAX_VALUE;
        }
        float[] angles = RotationUtils.anglesTo(entity);
        return Math.abs(RotationUtils.yawDifference(mc.player.getYRot(), angles[0]));
    }

    /** The living entity the crosshair is over, or null. */
    public static LivingEntity crosshairTarget() {
        Entity hit = PlayerUtils.crosshairTarget();
        return hit instanceof LivingEntity living ? living : null;
    }
}
