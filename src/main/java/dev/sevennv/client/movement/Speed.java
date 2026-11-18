package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.world.phys.Vec3;

/**
 * Applies a horizontal speed boost while the player is airborne, the classic
 * "bhop"-style acceleration. Vanilla ground friction still applies, so this is
 * a modest boost rather than a teleport.
 */
public final class Speed extends Module {
    public Speed() {
        super("Speed", "Adds a horizontal speed boost while airborne", Category.MOVEMENT);
        multiplier = number("Multiplier", "Horizontal speed multiplier", 1.15, 1.0, 2.0, 0.05);
        onlyForward = bool("Only forward", "Only boost while moving forward", true);
        requireAirborne = bool("Require airborne", "Only apply while off the ground", true);
    }

    private final DoubleSetting multiplier;
    private final BooleanSetting onlyForward;
    private final BooleanSetting requireAirborne;

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        if (requireAirborne.get() && mc.player.onGround()) {
            return;
        }
        if (onlyForward.get() && mc.player.zza <= 0.0f) {
            return;
        }
        Vec3 motion = mc.player.getDeltaMovement();
        double factor = multiplier.get();
        mc.player.setDeltaMovement(motion.x * factor, motion.y, motion.z * factor);
    }
}
