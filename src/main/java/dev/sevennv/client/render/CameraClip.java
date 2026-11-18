package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Pushes the third-person camera out further by widening the camera-distance
 * attribute. The value is applied as a normal attribute modifier.
 */
public final class CameraClip extends Module {
    public CameraClip() {
        super("Camera Clip", "Extends the third-person camera distance", Category.RENDER);
        distance = number("Distance", "Camera distance in blocks", 12.0, 4.0, 48.0, 1.0);
        noClip = bool("No clip", "Push the camera through terrain", true);
    }

    private final DoubleSetting distance;
    private final BooleanSetting noClip;

    public double distance() {
        return distance.get();
    }

    public boolean noClip() {
        return isEnabled() && noClip.get();
    }
}
