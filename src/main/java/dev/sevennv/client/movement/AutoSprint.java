package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Taps the sprint key automatically. Unlike {@link Sprint}, this sets the
 * vanilla sprint key state so the player's own input handling drives sprinting,
 * which matches how a human would hold the key.
 */
public final class AutoSprint extends Module {
    public AutoSprint() {
        super("Auto Sprint", "Holds the sprint key for you", Category.MOVEMENT);
        requireForward = bool("Require forward", "Only sprint while moving forward", true);
        requireFood = bool("Require food", "Stop sprinting when hungry", true);
    }

    private final BooleanSetting requireForward;
    private final BooleanSetting requireFood;

    private boolean previous;

    @Override
    public void onDisable() {
        restore();
    }

    @Override
    public void onWorldChange() {
        restore();
    }

    private void restore() {
        if (previous && mc.options != null) {
            mc.options.keySprint.setDown(false);
            previous = false;
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) {
            return;
        }
        boolean shouldSprint = !mc.player.isShiftKeyDown() && !mc.player.isUsingItem();
        if (requireForward.get()) {
            shouldSprint &= mc.player.zza > 0.0f;
        }
        if (requireFood.get()) {
            shouldSprint &= mc.player.getFoodData().getFoodLevel() > 6;
        }
        if (shouldSprint) {
            mc.options.keySprint.setDown(true);
            previous = true;
        } else {
            restore();
        }
    }
}
