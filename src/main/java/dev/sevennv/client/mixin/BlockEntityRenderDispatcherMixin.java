package dev.sevennv.client.mixin;

import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.render.BlockEntityCulling;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Skips render-state extraction for block entities the Block Entity Culling
 * module hides. The hook runs at the head of {@code tryExtractRenderState} and
 * returns null, which is exactly the value vanilla uses for a block entity it
 * does not draw — so nothing downstream needs to change.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {

    @Shadow
    private net.minecraft.world.phys.Vec3 cameraPos;

    @Inject(method = "tryExtractRenderState", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity, S extends BlockEntityRenderState> void sevennv$cullBlockEntity(
            E blockEntity, float partialTick, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            CallbackInfoReturnable<S> info) {
        if (SevennvClient.getModuleManager() == null) {
            return;
        }
        BlockEntityCulling culling = SevennvClient.getModuleManager().get(BlockEntityCulling.class);
        if (culling == null || !culling.isEnabled()) {
            return;
        }
        if (culling.shouldCull(blockEntity, cameraPos)) {
            info.setReturnValue(null);
        }
    }
}
