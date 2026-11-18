package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Timer;

/**
 * Holds a jump for you so you bunny-hop continuously while you run. Only the
 * ordinary jump key is pressed, and only while you are already moving forward on
 * the ground, so it never fights the player's own input.
 */
public final class BunnyHop extends Module {
    public BunnyHop() {
        super("Bunny Hop", "Jumps continuously while you run", Category.MOVEMENT);
        requireMoving = bool("Require moving", "Only hop while moving forward", true);
        requireSprint = bool("Require sprint", "Only hop while sprinting", false);
        holdJump = bool("Hold jump", "Keep the key held instead of tapping", false);
        delay = integer("Delay", "Minimum milliseconds between hops", 100, 0, 500);
    }

    private final BooleanSetting requireMoving;
    private final BooleanSetting requireSprint;
    private final BooleanSetting holdJump;
    private final dev.sevennv.client.config.IntSetting delay;
    private final Timer timer = new Timer();
    private int heldTicks;

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) {
            return;
        }
        if (holdJump.get()) {
            boolean hop = shouldHop();
            mc.options.keyJump.setDown(hop);
            return;
        }
        if (heldTicks > 0 && --heldTicks == 0) {
            mc.options.keyJump.setDown(false);
            return;
        }
        if (!shouldHop() || !timer.tick(delay.get())) {
            return;
        }
        mc.options.keyJump.setDown(true);
        heldTicks = 2;
    }

    private boolean shouldHop() {
        if (!mc.player.onGround() || mc.player.isInWater() || mc.player.isInLava()
                || mc.player.onClimbable()) {
            return false;
        }
        if (requireMoving.get() && mc.player.zza <= 0.0f) {
            return false;
        }
        return !requireSprint.get() || mc.player.isSprinting();
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
