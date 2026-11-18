package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.client.CloudStatus;
import net.minecraft.server.level.ParticleStatus;

/**
 * A single switch that trades visual fidelity for frame rate. Every change is a
 * local client option, applied on enable and restored on disable, so nothing
 * here is visible to the server. Sub-toggles let you keep the parts you care
 * about (for example clouds off but particles on).
 */
public final class FastGraphics extends Module {
    public FastGraphics() {
        super("Fast Graphics", "Applies a bundle of cheap graphics settings for more FPS", Category.RENDER);
        clouds = bool("Clouds off", "Turn clouds off", true);
        particles = bool("Fewer particles", "Reduce particles to the minimum", true);
        ambientOcclusion = bool("Ambient occlusion off", "Turn smooth lighting off", true);
        entityShadows = bool("Entity shadows off", "Hide entity shadows", false);
        clampEntityDistance = bool("Clamp entity distance", "Pull entity render distance in", true);
        entityDistance = number("Entity distance", "Entity distance scaling to apply", 0.75, 0.25, 1.0, 0.05);
        restoreOnDisable = bool("Restore on disable", "Put the old settings back when you turn this off", true);
    }

    private final BooleanSetting clouds;
    private final BooleanSetting particles;
    private final BooleanSetting ambientOcclusion;
    private final BooleanSetting entityShadows;
    private final BooleanSetting clampEntityDistance;
    private final DoubleSetting entityDistance;
    private final BooleanSetting restoreOnDisable;

    private CloudStatus oldClouds;
    private ParticleStatus oldParticles;
    private boolean oldAmbientOcclusion;
    private boolean oldEntityShadows;
    private double oldEntityDistance;
    private boolean applied;

    @Override
    public void onEnable() {
        if (mc.options == null) {
            return;
        }
        oldClouds = mc.options.cloudStatus().get();
        oldParticles = mc.options.particles().get();
        oldAmbientOcclusion = mc.options.ambientOcclusion().get();
        oldEntityShadows = mc.options.entityShadows().get();
        oldEntityDistance = mc.options.entityDistanceScaling().get();
        apply();
        applied = true;
    }

    @Override
    public void onDisable() {
        if (!applied || mc.options == null) {
            return;
        }
        if (restoreOnDisable.get()) {
            mc.options.cloudStatus().set(oldClouds);
            mc.options.particles().set(oldParticles);
            mc.options.ambientOcclusion().set(oldAmbientOcclusion);
            mc.options.entityShadows().set(oldEntityShadows);
            mc.options.entityDistanceScaling().set(oldEntityDistance);
        }
        applied = false;
    }

    /** Re-applies the current toggles; call when a sub-setting changes. */
    public void apply() {
        if (mc.options == null) {
            return;
        }
        if (clouds.get()) {
            mc.options.cloudStatus().set(CloudStatus.OFF);
        } else if (applied) {
            mc.options.cloudStatus().set(oldClouds);
        }
        if (particles.get()) {
            mc.options.particles().set(ParticleStatus.MINIMAL);
        } else if (applied) {
            mc.options.particles().set(oldParticles);
        }
        if (ambientOcclusion.get()) {
            mc.options.ambientOcclusion().set(false);
        } else if (applied) {
            mc.options.ambientOcclusion().set(oldAmbientOcclusion);
        }
        if (entityShadows.get()) {
            mc.options.entityShadows().set(false);
        } else if (applied) {
            mc.options.entityShadows().set(oldEntityShadows);
        }
        if (clampEntityDistance.get()) {
            mc.options.entityDistanceScaling().set(entityDistance.get());
        } else if (applied) {
            mc.options.entityDistanceScaling().set(oldEntityDistance);
        }
    }
}
