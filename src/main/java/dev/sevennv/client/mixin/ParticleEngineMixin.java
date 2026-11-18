package dev.sevennv.client.mixin;

import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.module.ModuleManager;
import dev.sevennv.client.render.NoRender;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Drops particle creation for the categories the No Render module hides. This
 * only stops the client from spawning its own particle objects; the world and
 * the packets received from the server are untouched.
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void sevennv$filterParticles(ParticleOptions options, double x, double y, double z,
                                         double velocityX, double velocityY, double velocityZ,
                                         CallbackInfoReturnable<Particle> info) {
        ModuleManager manager = SevennvClient.getModuleManager();
        if (manager == null) {
            return;
        }
        NoRender noRender = manager.get(NoRender.class);
        if (noRender == null || !noRender.isEnabled()) {
            return;
        }
        var type = options.getType();
        boolean hide = (noRender.hideParticles())
                || (noRender.hideTotemPops() && type == ParticleTypes.TOTEM_OF_UNDYING)
                || (noRender.hidePotionParticles() && (type == ParticleTypes.EFFECT
                        || type == ParticleTypes.ENTITY_EFFECT || type == ParticleTypes.INSTANT_EFFECT))
                || (noRender.hideExplosions() && (type == ParticleTypes.EXPLOSION
                        || type == ParticleTypes.EXPLOSION_EMITTER));
        if (hide) {
            info.setReturnValue(null);
        }
    }
}
