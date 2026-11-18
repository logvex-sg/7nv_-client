package dev.sevennv.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Rotation maths shared by combat and movement modules. */
public final class RotationUtils {
    private RotationUtils() {
    }

    public static float[] anglesTo(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Mth.atan2(dz, dx) * 180.0 / Math.PI) - 90f;
        float pitch = (float) -(Mth.atan2(dy, horizontal) * 180.0 / Math.PI);
        return new float[]{yaw, Mth.clamp(pitch, -90f, 90f)};
    }

    /** Angles from the local player's eyes to an entity's mid-body. */
    public static float[] anglesTo(Entity target) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || target == null) {
            return new float[]{0f, 0f};
        }
        Vec3 eyes = mc.player.getEyePosition();
        Vec3 centre = new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());
        return anglesTo(eyes, centre);
    }

    /** Shortest signed difference between two yaw angles. */
    public static float yawDifference(float current, float target) {
        return Mth.wrapDegrees(target - current);
    }

    /** GCD-legal pitch clamp used when aiming. */
    public static float clampPitch(float pitch) {
        return Mth.clamp(pitch, -90f, 90f);
    }
}
