package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Keeps you sprinting while you hold a movement key. Sprint is only forced when
 * the vanilla conditions are already met (moving forward, food above the
 * threshold), so it never fights the game's own logic.
 */
public final class Sprint extends Module {
    public Sprint() {
        super("Sprint", "Keeps you sprinting while moving", Category.MOVEMENT);
        requireForward = bool("Require forward", "Only sprint while holding forward", true);
        minFood = integer("Min food", "Do not force sprint below this food level", 6, 0, 20);
        stopWhenSneaking = bool("Stop when sneaking", "Let go of sprint while sneaking", true);
    }

    private final BooleanSetting requireForward;
    private final dev.sevennv.client.config.IntSetting minFood;
    private final BooleanSetting stopWhenSneaking;

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        if (mc.player.getFoodData().getFoodLevel() <= minFood.get()) {
            return;
        }
        if (stopWhenSneaking.get() && mc.player.isShiftKeyDown()) {
            return;
        }
        boolean moving = mc.player.zza > 0.0f;
        if (requireForward.get() && !moving) {
            return;
        }
        if (!mc.player.isSprinting() && (moving || !requireForward.get())) {
            mc.player.setSprinting(true);
        }
    }
}
