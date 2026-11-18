package dev.sevennv.client.mixin;

import dev.sevennv.client.utils.CpsTracker;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Feeds the clicks-per-second tracker. This only observes the raw button event
 * at the head of {@code MouseHandler.onButton}; the vanilla handling still runs
 * unchanged, so no input is consumed or altered.
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Inject(method = "onButton", at = @At("HEAD"))
    private void sevennv$onButton(long window, MouseButtonInfo info, int action, CallbackInfo callback) {
        if (action == 1 && info.button() == 0) {
            CpsTracker.onClick();
        }
    }
}
