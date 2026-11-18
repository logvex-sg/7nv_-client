package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Timer;

/**
 * Nudges the render distance down when the frame rate is poor and back up when
 * there is headroom, with hysteresis so it does not oscillate. The change is a
 * normal client option, so the server sees the same reduced view distance it
 * would if you had changed it by hand.
 */
public final class DynamicRenderDistance extends Module {
    public DynamicRenderDistance() {
        super("Dynamic Render Distance", "Lowers render distance when FPS is low", Category.RENDER);
        minDistance = integer("Min chunks", "Lowest render distance to drop to", 4, 2, 32);
        maxDistance = integer("Max chunks", "Highest render distance to raise to", 12, 2, 32);
        targetFps = integer("Target FPS", "Try to keep the frame rate at or above this", 70, 30, 240);
        cooldown = number("Cooldown", "Seconds between adjustments", 5.0, 1.0, 30.0, 1.0);
        restore = bool("Restore on disable", "Put your original render distance back when disabled", true);
    }

    private final IntSetting minDistance;
    private final IntSetting maxDistance;
    private final IntSetting targetFps;
    private final DoubleSetting cooldown;
    private final BooleanSetting restore;

    private final Timer timer = new Timer();
    private int original = -1;

    @Override
    public void onEnable() {
        if (mc.options != null) {
            original = mc.options.renderDistance().get();
        }
        timer.reset();
    }

    @Override
    public void onDisable() {
        if (restore.get() && original > 0 && mc.options != null) {
            mc.options.renderDistance().set(original);
        }
        original = -1;
    }

    @Override
    public void onTick() {
        if (mc.options == null || mc.level == null) {
            return;
        }
        if (!timer.tick((long) (cooldown.get() * 1000.0))) {
            return;
        }
        int current = mc.options.renderDistance().get();
        int fps = mc.getFps();
        int min = Math.min(minDistance.get(), maxDistance.get());
        int max = Math.max(minDistance.get(), maxDistance.get());
        if (fps < targetFps.get() && current > min) {
            mc.options.renderDistance().set(Math.max(min, current - 1));
        } else if (fps > targetFps.get() * 1.4 && current < max) {
            mc.options.renderDistance().set(Math.min(max, current + 1));
        }
    }

    @Override
    public String getArraylistSuffix() {
        return mc.options == null ? "" : mc.options.renderDistance().get() + "c";
    }
}
