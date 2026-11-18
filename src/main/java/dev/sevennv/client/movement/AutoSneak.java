package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Timer;

/**
 * Toggles the sneak key on a timer, the classic "shift toggle" convenience. It
 * only holds the ordinary sneak key, so movement and detection behave normally.
 */
public final class AutoSneak extends Module {
    public AutoSneak() {
        super("Auto Sneak", "Toggles sneaking on a timer", Category.MOVEMENT);
        interval = integer("Interval", "Milliseconds between toggles", 200, 50, 5000);
        holdFor = integer("Hold for", "Milliseconds to hold sneak each toggle", 100, 20, 5000);
        onlyWhileMoving = bool("Only while moving", "Only toggle while moving", false);
    }

    private final IntSetting interval;
    private final IntSetting holdFor;
    private final BooleanSetting onlyWhileMoving;
    private final Timer timer = new Timer();
    private boolean holding;

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) {
            return;
        }
        if (onlyWhileMoving.get() && mc.player.zza == 0.0f && mc.player.xxa == 0.0f) {
            release();
            return;
        }
        long wait = holding ? holdFor.get() : interval.get();
        if (!timer.tick(wait)) {
            return;
        }
        holding = !holding;
        mc.options.keyShift.setDown(holding);
    }

    private void release() {
        if (holding) {
            holding = false;
            mc.options.keyShift.setDown(false);
        }
    }

    @Override
    public void onDisable() {
        release();
    }

    @Override
    public void onWorldChange() {
        release();
    }
}
