package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.world.phys.Vec3;

/**
 * Adds a small upward impulse when the jump key is released, extending a jump.
 * Bounded by the configured boost so it cannot be used to fly.
 */
public final class JumpAssist extends Module {
    public JumpAssist() {
        super("Jump Assist", "Extends jumps with a small extra boost", Category.MOVEMENT);
        boost = number("Boost", "Extra upward velocity on jump release", 0.08, 0.0, 0.3, 0.01);
        requireSprint = bool("Require sprint", "Only boost while sprinting", false);
        onlyAirborne = bool("Only airborne", "Only apply while in the air", true);
    }

    private final DoubleSetting boost;
    private final BooleanSetting requireSprint;
    private final BooleanSetting onlyAirborne;

    private boolean jumpHeld;

    @Override
    public void onWorldChange() {
        jumpHeld = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        boolean jump = mc.options.keyJump.isDown();
        boolean released = jumpHeld && !jump;
        jumpHeld = jump;
        if (!released) {
            return;
        }
        if (onlyAirborne.get() && mc.player.onGround()) {
            return;
        }
        if (requireSprint.get() && !mc.player.isSprinting()) {
            return;
        }
        Vec3 motion = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(motion.x, motion.y + boost.get(), motion.z);
    }
}
