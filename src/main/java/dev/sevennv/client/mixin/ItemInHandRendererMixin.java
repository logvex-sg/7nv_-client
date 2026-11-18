package dev.sevennv.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.module.ModuleManager;
import dev.sevennv.client.render.ViewModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Scales the first-person hand and item model. The scale is applied to the pose
 * stack the vanilla renderer has already positioned at the camera, so the model
 * grows or shrinks in place.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Inject(method = "renderHandsWithItems", at = @At("HEAD"))
    private void sevennv$scaleViewModel(float partialTick, PoseStack poseStack,
                                        SubmitNodeCollector collector, LocalPlayer player, int light,
                                        CallbackInfo info) {
        ModuleManager manager = SevennvClient.getModuleManager();
        if (manager == null) {
            return;
        }
        ViewModel viewModel = manager.get(ViewModel.class);
        if (viewModel != null && viewModel.isEnabled()) {
            float scale = viewModel.scale();
            poseStack.scale(scale, scale, scale);
        }
    }
}
