package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Timer;

/**
 * Produces a critical hit on the next attack by hopping just before the swing.
 *
 * <p>Criticals require the player to be falling; this module briefly lifts the
 * player off the ground so the following attack lands as a crit. The lift is a
 * small, bounded velocity change, not a teleport.
 */
public final class Criticals extends Module {
    public Criticals() {
        super("Criticals", "Makes your attacks critical hits", Category.COMBAT);
        onlyOnGround = bool("Only on ground", "Only hop while grounded", true);
        delay = integer("Delay", "Milliseconds between hops", 200, 0, 1000);
        requireFall = bool("Require fall", "Only hop when there is room to fall", true);
    }

    private final BooleanSetting onlyOnGround;
    private final IntSetting delay;
    private final BooleanSetting requireFall;
    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || !mc.options.keyAttack.isDown()) {
            return;
        }
        if (onlyOnGround.get() && !mc.player.onGround()) {
            return;
        }
        if (requireFall.get() && !mc.level.getBlockState(mc.player.blockPosition().below()).isAir()) {
            return;
        }
        if (mc.player.isInWater() || mc.player.isInLava() || mc.player.onClimbable()) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        var target = dev.sevennv.client.utils.TargetUtils.crosshairTarget();
        if (target == null || mc.player.distanceTo(target) > 3.5) {
            return;
        }
        // A small upward impulse puts the player in the "falling" state the
        // vanilla crit check requires, without moving them off the block.
        var motion = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(motion.x, 0.11, motion.z);
        mc.player.setOnGround(false);
    }
}
