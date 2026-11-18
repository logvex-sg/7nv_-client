package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Removes the movement slowdown applied while using an item (eating, drinking,
 * blocking). The vanilla penalty is a scale on the move impulse inside
 * {@code LocalPlayer.modifyInput}, which runs every tick and would overwrite any
 * value written after the fact, so the penalty is cancelled at the source by
 * {@link dev.sevennv.client.mixin.LocalPlayerMixin}.
 */
public final class NoSlow extends Module {
    public NoSlow() {
        super("No Slow", "Removes the item-use movement slowdown", Category.MOVEMENT);
        includeSneak = bool("Include sneak", "Also counter the sneak slowdown", false);
    }

    private final BooleanSetting includeSneak;

    /** True when the sneak slowdown should also be cancelled. */
    public boolean cancelsSneakSlow() {
        return includeSneak.get();
    }
}
