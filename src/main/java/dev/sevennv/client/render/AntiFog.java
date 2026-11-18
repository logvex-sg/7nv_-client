package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.world.level.material.FogType;

/**
 * Clears distance fog by reporting the player's view as unobstructed.
 *
 * <p>The fog suppression is applied through the vanilla fog-distance query, so
 * it only affects what the client draws; the world and the server are untouched.
 */
public final class AntiFog extends Module {
    public AntiFog() {
        super("Anti Fog", "Removes distance and liquid fog", Category.RENDER);
        distance = bool("Distance fog", "Remove the render-distance fog", true);
        liquid = bool("Liquid fog", "Remove water and lava fog", true);
        blindness = bool("Blindness fog", "Remove the blindness and darkness fog", true);
    }

    private final BooleanSetting distance;
    private final BooleanSetting liquid;
    private final BooleanSetting blindness;

    public boolean shouldRemove(FogType type) {
        if (!isEnabled()) {
            return false;
        }
        return switch (type) {
            case WATER, LAVA, POWDER_SNOW -> liquid.get();
            case NONE -> distance.get();
            default -> blindness.get();
        };
    }
}
