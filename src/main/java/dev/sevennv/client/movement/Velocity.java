package dev.sevennv.client.movement;

import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;

/**
 * Reduces the knockback the server applies to the local player.
 *
 * <p>The incoming motion packet is cancelled and replaced with a scaled copy of
 * its own vector, so the resulting movement is always a fraction of what the
 * server sent. Nothing is invented client-side and the value is clamped, so the
 * player can never be moved further than the server intended.
 */
public final class Velocity extends Module {
    public Velocity() {
        super("Velocity", "Reduces knockback applied to you", Category.MOVEMENT);
        horizontal = number("Horizontal", "Fraction of horizontal knockback kept", 0.0, 0.0, 1.0, 0.05);
        vertical = number("Vertical", "Fraction of vertical knockback kept", 0.0, 0.0, 1.0, 0.05);
        onlyOnGround = bool("Only on ground", "Only modify knockback while grounded", false);
    }

    private final DoubleSetting horizontal;
    private final DoubleSetting vertical;
    private final dev.sevennv.client.config.BooleanSetting onlyOnGround;

    /** Applies the configured reduction. Returns true when the packet was consumed. */
    public boolean handleKnockback(ClientboundSetEntityMotionPacket packet) {
        if (mc.player == null) {
            return false;
        }
        if (onlyOnGround.get() && !mc.player.onGround()) {
            return false;
        }
        Vec3 motion = packet.getMovement();
        double x = motion.x * horizontal.get();
        double y = motion.y * vertical.get();
        double z = motion.z * horizontal.get();
        mc.player.setDeltaMovement(x, y, z);
        mc.player.hurtMarked = true;
        return true;
    }
}
