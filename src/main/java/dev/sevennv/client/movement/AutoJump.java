package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Timer;

/**
 * Jumps automatically when you run into a one-block obstacle. The jump goes
 * through the vanilla jump key, so the server sees an ordinary jump.
 */
public final class AutoJump extends Module {
    public AutoJump() {
        super("Auto Jump", "Jumps over one-block obstacles automatically", Category.MOVEMENT);
        requireMoving = bool("Require moving", "Only jump while moving forward", true);
        delay = integer("Delay", "Milliseconds between jumps", 200, 0, 1000);
        onlyOnGround = bool("Only on ground", "Only jump while grounded", true);
    }

    private final BooleanSetting requireMoving;
    private final IntSetting delay;
    private final BooleanSetting onlyOnGround;
    private final Timer timer = new Timer();
    private int heldTicks;

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) {
            return;
        }
        // Release the jump key we pressed last tick, so the player jumps once
        // per obstacle instead of bouncing continuously.
        if (heldTicks > 0 && --heldTicks == 0) {
            mc.options.keyJump.setDown(false);
            return;
        }
        if (requireMoving.get() && mc.player.zza <= 0.0f) {
            return;
        }
        if (onlyOnGround.get() && !mc.player.onGround()) {
            return;
        }
        if (!mc.player.horizontalCollision || !timer.tick(delay.get())) {
            return;
        }
        mc.options.keyJump.setDown(true);
        heldTicks = 2;
    }

    @Override
    public void onDisable() {
        release();
    }

    @Override
    public void onWorldChange() {
        release();
    }

    private void release() {
        heldTicks = 0;
        if (mc.options != null) {
            mc.options.keyJump.setDown(false);
        }
    }
}
