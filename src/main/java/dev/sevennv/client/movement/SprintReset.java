package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Resets sprint just before a hit lands, the way a human "W-tap" does, to keep
 * knockback controllable. The reset is a one-tick release followed by a
 * re-apply, so movement resumes immediately.
 */
public final class SprintReset extends Module {
    public SprintReset() {
        super("Sprint Reset", "Releases sprint briefly on each attack", Category.MOVEMENT);
        holdTicks = integer("Release ticks", "Ticks sprint is held off", 1, 1, 5);
        onlyOnGround = bool("Only on ground", "Only reset while grounded", true);
    }

    private final dev.sevennv.client.config.IntSetting holdTicks;
    private final BooleanSetting onlyOnGround;

    private int released;

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        if (released > 0) {
            released--;
            mc.player.setSprinting(false);
            return;
        }
        if (onlyOnGround.get() && !mc.player.onGround()) {
            return;
        }
        if (mc.options.keyAttack.isDown() && mc.player.isSprinting() && mc.player.zza > 0.0f) {
            released = holdTicks.get();
        }
    }

    @Override
    public void onDisable() {
        released = 0;
    }

    @Override
    public void onWorldChange() {
        released = 0;
    }
}
