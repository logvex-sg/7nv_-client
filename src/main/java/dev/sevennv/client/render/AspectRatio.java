package dev.sevennv.client.render;

import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * A wider field of view, expressed as a permanent FOV multiplier. This is the
 * classic "stretched" look without touching the window size.
 */
public final class AspectRatio extends Module {
    public AspectRatio() {
        super("Aspect Ratio", "Stretches the field of view wider", Category.RENDER);
        multiplier = number("Multiplier", "Field-of-view multiplier", 1.15, 1.0, 1.6, 0.05);
    }

    private final DoubleSetting multiplier;

    /** Applied on top of the player's configured FOV. */
    public float apply(float baseFov) {
        return isEnabled() ? (float) (baseFov * multiplier.get()) : baseFov;
    }
}
