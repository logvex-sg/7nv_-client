package dev.sevennv.client.movement;

import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.world.phys.Vec3;

/**
 * Increases the climb speed on ladders and vines by topping up the upward
 * motion while the player is on a climbable block and holding jump.
 */
public final class FastLadder extends Module {
    public FastLadder() {
        super("Fast Ladder", "Climbs ladders and vines faster", Category.MOVEMENT);
        speed = number("Speed", "Upward speed while climbing", 0.2, 0.05, 0.5, 0.05);
        onlyUp = bool("Only up", "Only speed up, never down", true);
    }

    private final DoubleSetting speed;
    private final dev.sevennv.client.config.BooleanSetting onlyUp;

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        if (!mc.player.onClimbable()) {
            return;
        }
        if (!mc.options.keyJump.isDown() && onlyUp.get()) {
            return;
        }
        Vec3 motion = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(motion.x, Math.max(motion.y, speed.get()), motion.z);
    }
}
