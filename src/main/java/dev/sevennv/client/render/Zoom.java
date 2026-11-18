package dev.sevennv.client.render;

import com.mojang.blaze3d.platform.InputConstants;
import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.KeybindSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.InputUtils;
import dev.sevennv.client.utils.MathUtils;

/**
 * Zooms the camera while a key is held by scaling the field of view. The value
 * returned to the renderer is smoothly approached each frame, so the zoom is
 * gradual rather than an instant snap.
 */
public final class Zoom extends Module {
    public Zoom() {
        super("Zoom", "Hold a key to zoom in", Category.RENDER);
        key = keybindSetting("Key", "Key to hold while zooming");
        factor = number("Factor", "Zoom strength (higher is closer)", 4.0, 1.5, 10.0, 0.5);
        smooth = bool("Smooth", "Animate the zoom in and out", true);
    }

    private final KeybindSetting key;
    private final DoubleSetting factor;
    private final BooleanSetting smooth;

    private float current = 1.0f;

    @Override
    public void onDisable() {
        current = 1.0f;
    }

    @Override
    public void onWorldChange() {
        current = 1.0f;
    }

    public boolean isZooming() {
        if (!isEnabled()) {
            return false;
        }
        InputConstants.Key bound = key.get();
        if (bound == null || bound == InputConstants.UNKNOWN) {
            return false;
        }
        return InputUtils.isDown(bound);
    }

    /** Scales the base FOV down by the configured factor. */
    public float zoomFov(float baseFov) {
        float target = 1.0f;
        float desired = (float) (1.0 / factor.get());
        if (smooth.get()) {
            target = current = (float) MathUtils.approach(current, isZooming() ? desired : 1.0f, 0.25);
        } else {
            target = isZooming() ? desired : 1.0f;
            current = target;
        }
        return baseFov * target;
    }

    @Override
    public String getArraylistSuffix() {
        return String.format(java.util.Locale.ROOT, "%.1fx", factor.get());
    }
}
