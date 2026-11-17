package dev.logvex.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Rotation helpers for the combat modules.
 *
 * <p>Two modes are offered: a visible turn (the player model actually rotates)
 * and a silent turn (the rotation is only sent to the server, so the local view
 * is unchanged). Silent rotations are followed by a restore packet so the
 * server's view of the player stays consistent.
 */
public final class Rotation {
    private Rotation() {
    }

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    public static float[] anglesTo(Vec3 target) {
        Minecraft mc = mc();
        if (mc.player == null) {
            return new float[]{0f, 0f};
        }
        return RotationUtils.anglesTo(mc.player.getEyePosition(), target);
    }

    /** Turns the player toward a point. Returns true when a rotation was sent. */
    public static boolean face(Vec3 target, boolean silent, float speed) {
        Minecraft mc = mc();
        if (mc.player == null) {
            return false;
        }
        float[] angles = anglesTo(target);
        float yaw = angles[0];
        float pitch = angles[1];
        if (speed < 180f) {
            yaw = mc.player.getYRot() + Mth.clamp(Mth.wrapDegrees(yaw - mc.player.getYRot()), -speed, speed);
            pitch = mc.player.getXRot() + Mth.clamp(pitch - mc.player.getXRot(), -speed, speed);
        }
        if (silent) {
            sendRotation(yaw, pitch);
        } else {
            mc.player.setYRot(yaw);
            mc.player.setXRot(pitch);
        }
        return true;
    }

    public static boolean face(Entity entity, boolean silent, float speed) {
        if (entity == null) {
            return false;
        }
        Vec3 centre = new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ());
        return face(centre, silent, speed);
    }

    public static void sendRotation(float yaw, float pitch) {
        Minecraft mc = mc();
        if (mc.player == null) {
            return;
        }
        mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
                yaw, Mth.clamp(pitch, -90f, 90f), mc.player.onGround(), mc.player.horizontalCollision));
    }

    /** Sends the player's current rotation, used to restore the server view. */
    public static void restore() {
        Minecraft mc = mc();
        if (mc.player == null) {
            return;
        }
        sendRotation(mc.player.getYRot(), mc.player.getXRot());
    }

    public static float yawTo(Vec3 target) {
        return anglesTo(target)[0];
    }

    public static float pitchTo(Vec3 target) {
        return anglesTo(target)[1];
    }
}
