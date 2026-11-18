package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.phys.Vec3;

/**
 * Adds a forward horizontal impulse at the moment of jumping, extending the
 * jump distance. Bounded by the configured boost.
 */
public final class LongJump extends Module {
    public LongJump() {
        super("Long Jump", "Extends jumps with a forward boost", Category.MOVEMENT);
        boost = number("Boost", "Forward velocity added on jump", 0.3, 0.05, 1.0, 0.05);
        requireSprint = bool("Require sprint", "Only boost while sprinting", true);
        delay = integer("Delay", "Milliseconds between boosts", 300, 0, 2000);
    }

    private final DoubleSetting boost;
    private final BooleanSetting requireSprint;
    private final dev.sevennv.client.config.IntSetting delay;
    private final Timer timer = new Timer();
    private boolean wasOnGround = true;

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        boolean onGround = mc.player.onGround();
        boolean justJumped = wasOnGround && !onGround;
        wasOnGround = onGround;
        if (!justJumped || !timer.tick(delay.get())) {
            return;
        }
        if (requireSprint.get() && !mc.player.isSprinting()) {
            return;
        }
        Vec3 look = mc.player.getLookAngle();
        Vec3 motion = mc.player.getDeltaMovement();
        double factor = boost.get();
        mc.player.setDeltaMovement(motion.x + look.x * factor, motion.y, motion.z + look.z * factor);
    }
}
