package dev.sevennv.client.mixin;

import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.render.EntityCulling;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds a view-cone and distance test to the vanilla entity cull decision. The
 * hook runs after the vanilla frustum check, so it can only ever cull an entity
 * the client was about to draw; it never forces a draw that vanilla skipped.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true)
    private <E extends Entity> void sevennv$cullEntity(E entity, Frustum frustum, double x, double y,
                                                      double z, CallbackInfoReturnable<Boolean> info) {
        if (!Boolean.TRUE.equals(info.getReturnValue())) {
            return;
        }
        if (SevennvClient.getModuleManager() == null) {
            return;
        }
        EntityCulling culling = SevennvClient.getModuleManager().get(EntityCulling.class);
        if (culling == null || !culling.isEnabled()) {
            return;
        }
        if (culling.shouldCull(entity, frustum, x, y, z)) {
            culling.countCulled();
            info.setReturnValue(false);
        }
    }
}
