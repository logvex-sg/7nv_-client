package dev.sevennv.client.render;

import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Removes the camera tilt that plays when the player takes damage. The hurt
 * animation is purely visual and client-side.
 */
public final class NoHurtCam extends Module {
    public NoHurtCam() {
        super("No Hurt Cam", "Removes the damage camera shake", Category.RENDER);
    }
}
