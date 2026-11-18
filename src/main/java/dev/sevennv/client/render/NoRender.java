package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Suppresses selected client-side visual effects. The list is intentionally
 * limited to things the client draws itself: nothing here changes world state
 * or the packets sent to the server.
 */
public final class NoRender extends Module {
    public NoRender() {
        super("No Render", "Hides selected client-side effects", Category.RENDER);
        particles = bool("Particles", "Hide particles", true);
        totemPops = bool("Totem pops", "Hide totem animation particles", true);
        potionEffects = bool("Potion particles", "Hide potion effect particles", true);
        explosion = bool("Explosions", "Hide explosion particles", false);
        hurtCamera = bool("Hurt camera", "Remove the damage camera tilt", true);
        nausea = bool("Nausea", "Remove the nether-portal wobble", false);
    }

    private final BooleanSetting particles;
    private final BooleanSetting totemPops;
    private final BooleanSetting potionEffects;
    private final BooleanSetting explosion;
    private final BooleanSetting hurtCamera;
    private final BooleanSetting nausea;

    public boolean hideParticles() {
        return isEnabled() && particles.get();
    }

    public boolean hideTotemPops() {
        return isEnabled() && totemPops.get();
    }

    public boolean hidePotionParticles() {
        return isEnabled() && potionEffects.get();
    }

    public boolean hideExplosions() {
        return isEnabled() && explosion.get();
    }

    public boolean hideHurtCamera() {
        return isEnabled() && hurtCamera.get();
    }

    public boolean hideNausea() {
        return isEnabled() && nausea.get();
    }
}
