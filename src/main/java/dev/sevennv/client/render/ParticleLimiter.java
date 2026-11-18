package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

/**
 * Caps how many particles the client spawns per frame and drops particles that
 * are too far from the camera to notice. Each particle is an object that is
 * ticked and drawn every frame, so a big explosion or a busy farm can cost real
 * frame time; this trims the excess.
 *
 * <p>The budget is deliberately generous, so ordinary play is unchanged; only a
 * burst beyond it is trimmed. Explosion, totem and portal particles are kept by
 * default because those carry information.
 *
 * <p>Non-visual enough to be always-on: it never hides an entity, a block, or a
 * player, only decorative particles beyond a high budget.
 */
public final class ParticleLimiter extends Module {
    public ParticleLimiter() {
        super("Particle Limiter", "Caps particles per frame and drops distant ones", Category.RENDER);
        setAlwaysOn();
        budget = integer("Budget", "Maximum new particles spawned per frame", 400, 40, 4000);
        maxDistance = number("Max distance", "Drop particles spawned beyond this many blocks", 32.0, 8.0, 128.0, 4.0);
        keepImportant = bool("Keep important", "Always keep explosion, totem and portal particles", true);
    }

    private final IntSetting budget;
    private final DoubleSetting maxDistance;
    private final BooleanSetting keepImportant;

    private static volatile double camX;
    private static volatile double camY;
    private static volatile double camZ;
    private static int spawnedThisFrame;

    /** Cached on the render thread once per frame. */
    public static void updateCamera(double x, double y, double z) {
        camX = x;
        camY = y;
        camZ = z;
    }

    /** Resets the per-frame spawn counter. Called on the render thread. */
    public static void onFrameStart() {
        spawnedThisFrame = 0;
    }

    public static int spawnedThisFrame() {
        return spawnedThisFrame;
    }

    /**
     * Returns true when this particle should be skipped. Called from the
     * particle engine, on the client thread.
     */
    public boolean shouldCull(ParticleOptions options, double x, double y, double z) {
        if (!isEnabled()) {
            return false;
        }
        boolean important = keepImportant.get() && isImportant(options);
        double dx = x - camX;
        double dy = y - camY;
        double dz = z - camZ;
        double max = maxDistance.get();
        if (!important && dx * dx + dy * dy + dz * dz > max * max) {
            return true;
        }
        if (important) {
            spawnedThisFrame++;
            return false;
        }
        if (spawnedThisFrame >= budget.get()) {
            return true;
        }
        spawnedThisFrame++;
        return false;
    }

    private boolean isImportant(ParticleOptions options) {
        var type = options.getType();
        return type == ParticleTypes.EXPLOSION || type == ParticleTypes.EXPLOSION_EMITTER
                || type == ParticleTypes.TOTEM_OF_UNDYING
                || type == ParticleTypes.PORTAL || type == ParticleTypes.REVERSE_PORTAL
                || type == ParticleTypes.FLASH;
    }

    @Override
    public void onDisable() {
        spawnedThisFrame = 0;
    }

    @Override
    public String getArraylistSuffix() {
        return spawnedThisFrame > 0 ? String.valueOf(spawnedThisFrame) : "";
    }
}
