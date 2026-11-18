package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Keeps you sprinting while you move. Sprint is only forced when the vanilla
 * conditions are already met (moving forward, food above the threshold), unless
 * "Ignore hunger" is enabled, so it never fights the game's own logic.
 */
public final class Sprint extends Module {
    public Sprint() {
        super("Sprint", "Keeps you sprinting while moving", Category.MOVEMENT);
        onlyForward = bool("Only forward", "Only sprint while holding forward", true);
        ignoreHunger = bool("Ignore hunger", "Sprint even when your food is low", true);
        stopWhenSneaking = bool("Stop when sneaking", "Let go of sprint while sneaking", true);
        minFood = integer("Min food", "Food level below which sprint is dropped", 6, 0, 20);
    }

    private final BooleanSetting onlyForward;
    private final BooleanSetting ignoreHunger;
    private final BooleanSetting stopWhenSneaking;
    private final IntSetting minFood;

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        if (stopWhenSneaking.get() && mc.player.isShiftKeyDown()) {
            return;
        }
        if (!ignoreHunger.get() && mc.player.getFoodData().getFoodLevel() <= minFood.get()) {
            return;
        }
        boolean moving = mc.player.zza > 0.0f;
        if (onlyForward.get() && !moving) {
            return;
        }
        if (!mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
    }
}
