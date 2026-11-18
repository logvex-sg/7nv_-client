package dev.sevennv.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.module.ModuleManager;
import dev.sevennv.client.render.AspectRatio;
import dev.sevennv.client.render.NoHurtCam;
import dev.sevennv.client.render.NoRender;
import dev.sevennv.client.render.Zoom;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Render hooks for the visual modules: field-of-view overrides (Zoom and
 * Aspect Ratio) and suppression of the damage camera tilt (No Hurt Cam / No
 * Render). Only the local projection and pose are affected.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void sevennv$applyFov(Camera camera, float partialTick, boolean useFovSetting,
                                  CallbackInfoReturnable<Float> info) {
        ModuleManager manager = SevennvClient.getModuleManager();
        if (manager == null) {
            return;
        }
        float fov = info.getReturnValue();
        Zoom zoom = manager.get(Zoom.class);
        if (zoom != null && zoom.isEnabled() && zoom.isZooming()) {
            fov = zoom.zoomFov(fov);
        }
        AspectRatio aspect = manager.get(AspectRatio.class);
        if (aspect != null && aspect.isEnabled()) {
            fov = aspect.apply(fov);
        }
        info.setReturnValue(fov);
    }

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void sevennv$removeHurtTilt(PoseStack poseStack, float partialTick, CallbackInfo info) {
        ModuleManager manager = SevennvClient.getModuleManager();
        if (manager == null) {
            return;
        }
        NoHurtCam hurtCam = manager.get(NoHurtCam.class);
        if (hurtCam != null && hurtCam.isEnabled()) {
            info.cancel();
            return;
        }
        NoRender noRender = manager.get(NoRender.class);
        if (noRender != null && noRender.hideHurtCamera()) {
            info.cancel();
        }
    }
}
