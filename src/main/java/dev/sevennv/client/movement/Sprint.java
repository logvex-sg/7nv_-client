package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Timer;

/**
 * Forces the sprint key state on while the player is moving forward, so sprint
 * is maintained without holding the key.
 */
public final class Sprint extends Module {
    public Sprint() {
        super("Sprint", "Keeps you sprinting while moving forward", Category.MOVEMENT);
        onlyForward = bool("Only forward", "Only sprint while moving forward", true);
        ignoreHunger = bool("Ignore hunger", "Sprint even when hungry", true);
        delay = integer("Delay", "Milliseconds between checks", 50, 0, 500);
    }

    private final BooleanSetting onlyForward;
    private final BooleanSetting ignoreHunger;
    private final IntSetting delay;

    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        if (mc.player.isShiftKeyDown() || mc.player.isUsingItem()) {
            return;
        }
        if (!ignoreHunger.get() && mc.player.getFoodData().getFoodLevel() <= 6) {
            return;
        }
        if (onlyForward.get() && mc.player.zza <= 0.0f) {
            return;
        }
        if (!mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
    }
}
