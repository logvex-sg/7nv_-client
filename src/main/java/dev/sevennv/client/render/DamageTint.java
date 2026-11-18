package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Replaces the red damage overlay with a configurable colour. Only the screen
 * tint is changed; the underlying hurt animation is untouched.
 */
public final class DamageTint extends Module {
    public DamageTint() {
        super("Damage Tint", "Recolours the damage screen overlay", Category.RENDER);
        tint = color("Tint", "Overlay colour", 0x40FF0000);
        hideWhenFull = bool("Hide when full", "Do not tint while at full health", true);
    }

    private final ColorSetting tint;
    private final BooleanSetting hideWhenFull;

    public int overlayColor() {
        if (!isEnabled()) {
            return 0;
        }
        if (hideWhenFull.get() && mc.player != null && mc.player.getHealth() >= mc.player.getMaxHealth()) {
            return 0;
        }
        return tint.get();
    }
}
