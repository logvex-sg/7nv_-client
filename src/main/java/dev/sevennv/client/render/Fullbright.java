package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Raises the brightness (gamma) option beyond its vanilla maximum, so the world
 * is fully lit. The original value is restored on disable.
 */
public final class Fullbright extends Module {
    public Fullbright() {
        super("Fullbright", "Brightens the world beyond the vanilla limit", Category.RENDER);
        brightness = number("Brightness", "Gamma value to apply", 15.0, 1.0, 30.0, 1.0);
        restoreOnDisable = bool("Restore", "Restore the previous gamma on disable", true);
    }

    private final DoubleSetting brightness;
    private final BooleanSetting restoreOnDisable;

    private double original = Double.NaN;

    @Override
    public void onEnable() {
        if (mc.options != null && Double.isNaN(original)) {
            original = mc.options.gamma().get();
        }
        apply();
    }

    @Override
    public void onTick() {
        apply();
    }

    @Override
    public void onDisable() {
        if (restoreOnDisable.get() && mc.options != null && !Double.isNaN(original)) {
            mc.options.gamma().set(original);
        }
        original = Double.NaN;
    }

    private void apply() {
        if (mc.options != null) {
            mc.options.gamma().set(brightness.get());
        }
    }
}
