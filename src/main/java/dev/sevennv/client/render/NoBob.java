package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Disables the first-person view bob so the camera stays level while walking.
 * The vanilla option is turned off while enabled and restored on disable.
 */
public final class NoBob extends Module {
    public NoBob() {
        super("No Bob", "Removes first-person view bobbing", Category.RENDER);
        restore = bool("Restore", "Restore the previous setting on disable", true);
    }

    private final BooleanSetting restore;

    private Boolean original;

    @Override
    public void onEnable() {
        if (mc.options != null && original == null) {
            original = mc.options.bobView().get();
        }
        apply();
    }

    @Override
    public void onTick() {
        apply();
    }

    @Override
    public void onDisable() {
        if (restore.get() && mc.options != null && original != null) {
            mc.options.bobView().set(original);
        }
        original = null;
    }

    private void apply() {
        if (mc.options != null) {
            mc.options.bobView().set(false);
        }
    }
}
