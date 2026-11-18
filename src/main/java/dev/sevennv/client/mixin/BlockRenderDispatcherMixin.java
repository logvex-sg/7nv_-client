package dev.sevennv.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.sevennv.client.render.PixelBlocks;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Applies the Pixel Blocks scale to a block's tesselation pose. The pose is
 * pushed and popped unconditionally so the stack always stays balanced; only the
 * scale itself depends on the module. This runs on the section-compile worker
 * threads, so it reads the cached camera position rather than client state.
 */
@Mixin(BlockRenderDispatcher.class)
public abstract class BlockRenderDispatcherMixin {

    @Inject(method = "renderBatched", at = @At("HEAD"))
    private void sevennv$pushPixelScale(BlockState blockState, BlockPos blockPos,
                                        BlockAndTintGetter blockAndTintGetter, PoseStack poseStack,
                                        VertexConsumer vertexConsumer, boolean bl,
                                        List<BlockModelPart> list, CallbackInfo info) {
        poseStack.pushPose();
        float scale = PixelBlocks.scaleForBlock(blockState, blockPos);
        if (scale != 1.0f) {
            // Scale about the block centre so the shrink is symmetric.
            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-0.5f, -0.5f, -0.5f);
        }
    }

    @Inject(method = "renderBatched", at = @At("RETURN"))
    private void sevennv$popPixelScale(BlockState blockState, BlockPos blockPos,
                                       BlockAndTintGetter blockAndTintGetter, PoseStack poseStack,
                                       VertexConsumer vertexConsumer, boolean bl,
                                       List<BlockModelPart> list, CallbackInfo info) {
        poseStack.popPose();
    }
}
