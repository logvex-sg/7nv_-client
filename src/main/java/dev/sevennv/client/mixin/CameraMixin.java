package dev.sevennv.client.mixin;

import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.render.CameraClip;
import dev.sevennv.client.render.Freecam;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Overrides the camera position and rotation while Freecam is active. The
 * vanilla {@code setup} still runs first, so all other camera state (level,
 * entity, partial tick) is populated normally; only the final transform is
 * replaced.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    private float getMaxZoom(float requested) {
        throw new AssertionError();
    }

    @Inject(method = "setup", at = @At("TAIL"))
    private void sevennv$applyFreecam(Level level, Entity entity, boolean detached, boolean mirrored,
                                      float partialTick, CallbackInfo info) {
        if (SevennvClient.getModuleManager() == null) {
            return;
        }
        Freecam freecam = SevennvClient.getModuleManager().get(Freecam.class);
        if (freecam == null || !freecam.isEnabled()) {
            return;
        }
        Vec3 position = freecam.cameraPosition();
        setPosition(position.x, position.y, position.z);
        setRotation(freecam.cameraYaw(), freecam.cameraPitch());
    }

    /**
     * Overrides the third-person zoom distance. The redirect covers both the
     * "camera distance" setting and the terrain clip, so with No Clip enabled
     * the camera can be pushed through blocks.
     */
    @Redirect(method = "setup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"))
    private float sevennv$cameraClip(Camera camera, float requested) {
        if (SevennvClient.getModuleManager() == null) {
            return getMaxZoom(requested);
        }
        CameraClip clip = SevennvClient.getModuleManager().get(CameraClip.class);
        if (clip == null || !clip.isEnabled()) {
            return getMaxZoom(requested);
        }
        float distance = (float) clip.distance();
        return clip.noClip() ? distance : getMaxZoom(distance);
    }
}
