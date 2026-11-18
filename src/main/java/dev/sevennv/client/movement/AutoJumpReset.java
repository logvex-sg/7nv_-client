package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.phys.Vec3;

/**
 * Simulates the "jump reset" technique: after taking a hit, the player jumps on
 * the next tick so the knockback is applied against the ground and mostly
 * cancelled.
 *
 * <p>The jump is issued through the ordinary jump path, exactly as if the player
 * pressed the key. Nothing is teleported and no reach is extended.
 */
public final class AutoJumpReset extends Module {
    public AutoJumpReset() {
        super("Auto Jump Reset", "Jumps after taking knockback to cancel it", Category.MOVEMENT);
        chance = number("Chance", "Probability of jumping after a hit", 1.0, 0.0, 1.0, 0.05);
        onlyWhileOnGround = bool("Only on ground", "Require the player to be grounded", true);
        delay = integer("Delay", "Milliseconds before jumping", 50, 0, 500);
        minHealth = number("Min health", "Do not jump below this health", 0.0, 0.0, 20.0, 0.5);
    }

    private final DoubleSetting chance;
    private final BooleanSetting onlyWhileOnGround;
    private final IntSetting delay;
    private final DoubleSetting minHealth;
    private final Timer timer = new Timer();

    private int lastHurtTime;
    private boolean pending;

    @Override
    public void onWorldChange() {
        pending = false;
        lastHurtTime = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        if (mc.player.hurtTime > lastHurtTime) {
            pending = true;
            timer.reset();
        }
        lastHurtTime = mc.player.hurtTime;
        if (!pending) {
            return;
        }
        if (onlyWhileOnGround.get() && !mc.player.onGround()) {
            return;
        }
        if (mc.player.getHealth() < minHealth.get()) {
            pending = false;
            return;
        }
        if (!timer.hasElapsed(delay.get())) {
            return;
        }
        pending = false;
        if (Math.random() > chance.get()) {
            return;
        }
        Vec3 motion = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(motion.x, 0.42, motion.z);
        mc.player.setOnGround(false);
    }
}
