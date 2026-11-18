package dev.sevennv.client.render;

import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Timer;

/**
 * Pulls in the distance at which entities are drawn, which is the single
 * cheapest entity-side saving there is: fewer entity render states extracted,
 * fewer models built, fewer draw calls. The clamp is a normal client option, so
 * nothing is sent to the server.
 *
 * <p>This changes what is drawn (distant entities stop appearing), so it is a
 * normal toggleable visual module rather than an always-on optimisation.
 */
public final class EntityDistance extends Module {
    public EntityDistance() {
        super("Entity Distance", "Draws distant entities closer to the camera for more FPS", Category.RENDER);
        scaling = number("Scaling", "Entity render distance multiplier", 0.75, 0.25, 1.0, 0.05);
        minimum = integer("Minimum scaling", "Never scale below this while recovering", 40, 10, 100);
        dynamic = bool("Dynamic", "Lower the scaling when the frame rate drops", true);
        targetFps = integer("Target FPS", "Frame rate to keep before scaling down", 70, 30, 240);
    }

    private final DoubleSetting scaling;
    private final IntSetting minimum;
    private final dev.sevennv.client.config.BooleanSetting dynamic;
    private final IntSetting targetFps;

    private final Timer timer = new Timer();
    private double original = -1.0;
    private double applied = -1.0;

    @Override
    public void onEnable() {
        if (mc.options == null) {
            return;
        }
        if (original < 0.0) {
            original = mc.options.entityDistanceScaling().get();
        }
        apply();
        timer.reset();
    }

    @Override
    public void onDisable() {
        if (mc.options != null && original >= 0.0) {
            mc.options.entityDistanceScaling().set(original);
            applied = original;
        }
    }

    /** Pushes the current scaling value into the client option. */
    public void apply() {
        if (mc.options == null) {
            return;
        }
        double want = Math.max(minimum.get() / 100.0, scaling.get());
        if (Math.abs(want - applied) < 0.005) {
            return;
        }
        applied = want;
        mc.options.entityDistanceScaling().set(want);
    }

    @Override
    public void onTick() {
        if (mc.options == null || mc.level == null) {
            return;
        }
        if (!dynamic.get()) {
            apply();
            return;
        }
        if (!timer.tick(2000L)) {
            return;
        }
        double current = mc.options.entityDistanceScaling().get();
        double floor = minimum.get() / 100.0;
        if (mc.getFps() < targetFps.get() && current > floor) {
            mc.options.entityDistanceScaling().set(Math.max(floor, current - 0.05));
            applied = mc.options.entityDistanceScaling().get();
        } else if (mc.getFps() > targetFps.get() * 1.4 && current < scaling.get()) {
            mc.options.entityDistanceScaling().set(Math.min(scaling.get(), current + 0.05));
            applied = mc.options.entityDistanceScaling().get();
        } else {
            apply();
        }
    }

    @Override
    public String getArraylistSuffix() {
        if (mc.options == null) {
            return "";
        }
        return Math.round(mc.options.entityDistanceScaling().get() * 100) + "%";
    }
}
