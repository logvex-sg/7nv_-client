package dev.sevennv.client.mixin;

import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.module.ModuleManager;
import dev.sevennv.client.render.AntiBlind;
import dev.sevennv.client.render.AntiFog;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Removes selected fog layers by widening the fog distances the renderer writes
 * into its uniform buffer. Only the client's own fog data is touched.
 */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Redirect(method = "setupFog",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/fog/FogEnvironment;setupFog(Lnet/minecraft/client/renderer/fog/FogData;Lnet/minecraft/client/Camera;Lnet/minecraft/client/multiplayer/ClientLevel;FLnet/minecraft/client/DeltaTracker;)V"))
    private void sevennv$adjustFog(net.minecraft.client.renderer.fog.environment.FogEnvironment environment,
                                   FogData fogData, Camera camera, ClientLevel level, float renderDistance,
                                   DeltaTracker deltaTracker) {
        environment.setupFog(fogData, camera, level, renderDistance, deltaTracker);
        ModuleManager manager = SevennvClient.getModuleManager();
        if (manager == null) {
            return;
        }
        AntiFog antiFog = manager.get(AntiFog.class);
        if (antiFog == null || !antiFog.isEnabled()) {
            return;
        }
        FogType type = camera.getFluidInCamera();
        if (antiFog.shouldRemove(type)) {
            float far = renderDistance * 16.0f;
            fogData.environmentalStart = far;
            fogData.environmentalEnd = far;
            fogData.renderDistanceStart = far;
            fogData.renderDistanceEnd = far;
            fogData.skyEnd = far;
            fogData.cloudEnd = far;
        }
    }

    /**
     * Removes the blindness/darkness darkening of the fog colour. Returning zero
     * keeps the sky colour unchanged, so the effect no longer dims the view.
     */
    @Redirect(method = "computeFogColor",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/fog/environment/FogEnvironment;getModifiedDarkness(Lnet/minecraft/world/entity/LivingEntity;FF)F"))
    private float sevennv$removeDarkness(net.minecraft.client.renderer.fog.environment.FogEnvironment environment,
                                         net.minecraft.world.entity.LivingEntity entity, float darkness, float partialTick) {
        ModuleManager manager = SevennvClient.getModuleManager();
        if (manager != null) {
            AntiBlind antiBlind = manager.get(AntiBlind.class);
            if (antiBlind != null && antiBlind.isEnabled()) {
                return 0.0f;
            }
        }
        return environment.getModifiedDarkness(entity, darkness, partialTick);
    }
}
