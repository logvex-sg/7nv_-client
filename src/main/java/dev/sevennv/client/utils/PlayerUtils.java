package dev.sevennv.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Player-specific helpers: reach checks, entity lookup, distance maths. */
public final class PlayerUtils {
    private PlayerUtils() {
    }

    public static Minecraft mc() {
        return Minecraft.getInstance();
    }

    /** Squared distance from the local player's eyes to an entity's centre. */
    public static double distanceSq(Entity entity) {
        Player player = mc().player;
        if (player == null || entity == null) {
            return Double.MAX_VALUE;
        }
        return player.distanceToSqr(entity);
    }

    public static double distance(Entity entity) {
        return Math.sqrt(distanceSq(entity));
    }

    /** True when {@code entity} is a real player and not the local player. */
    public static boolean isOtherPlayer(Entity entity) {
        return entity instanceof Player && entity != mc().player;
    }

    /** True when {@code entity} is a living entity and not the local player. */
    public static boolean isOtherLiving(Entity entity) {
        return entity instanceof LivingEntity && entity != mc().player;
    }

    public static Vec3 eyePosition() {
        Player player = mc().player;
        return player == null ? Vec3.ZERO : player.getEyePosition();
    }

    /** The entity the crosshair is pointing at, if any. */
    public static Entity crosshairTarget() {
        Minecraft mc = mc();
        if (mc.hitResult == null || mc.level == null) {
            return null;
        }
        if (mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult entityHit) {
            return entityHit.getEntity();
        }
        return null;
    }

    /** The block position the crosshair is pointing at, or null. */
    public static net.minecraft.core.BlockPos crosshairBlock() {
        Minecraft mc = mc();
        if (mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
            return blockHit.getBlockPos();
        }
        return null;
    }
}
