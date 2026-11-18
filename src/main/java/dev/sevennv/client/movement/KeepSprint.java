package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Keeps you sprinting through an attack. Vanilla drops the sprint flag when you
 * hit an entity, which removes the knockback bonus; this re-applies it on the
 * same tick so the sprint is never lost.
 */
public final class KeepSprint extends Module {
    public KeepSprint() {
        super("Keep Sprint", "Keeps sprinting after you attack", Category.MOVEMENT);
        onlyWhileHolding = bool("Only while holding", "Only force sprint while the attack key is held", true);
    }

    private final BooleanSetting onlyWhileHolding;

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        if (onlyWhileHolding.get() && !mc.options.keyAttack.isDown()) {
            return;
        }
        if (mc.player.getFoodData().getFoodLevel() <= 6 || mc.player.isShiftKeyDown()) {
            return;
        }
        if (!mc.player.isSprinting() && mc.player.zza > 0.0f) {
            mc.player.setSprinting(true);
        }
    }
}
