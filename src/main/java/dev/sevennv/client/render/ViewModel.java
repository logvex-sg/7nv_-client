package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Scales the first-person hand and item model. The scale is applied by the
 * item-in-hand renderer while this module is enabled.
 */
public final class ViewModel extends Module {
    public ViewModel() {
        super("View Model", "Resizes your first-person hands and items", Category.RENDER);
        scale = number("Scale", "Overall model scale", 1.0, 0.3, 2.0, 0.05);
        swing = bool("Swing", "Keep the vanilla swing animation", true);
    }

    private final DoubleSetting scale;
    private final BooleanSetting swing;

    public float scale() {
        return isEnabled() ? (float) scale.get() : 1.0f;
    }

    public boolean swing() {
        return swing.get();
    }
}
