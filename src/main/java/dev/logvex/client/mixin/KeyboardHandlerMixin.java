package dev.logvex.client.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import dev.logvex.client.command.PendingBind;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies a pending {@code .bind} capture. The mixin only observes the key
 * press to assign a module keybind and cancels the vanilla handling while a
 * bind is pending, so the captured key is not also treated as a game action.
 */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void logvex$captureBind(long window, int action, KeyEvent event, CallbackInfo info) {
        if (!PendingBind.isActive() || Minecraft.getInstance().screen != null) {
            return;
        }
        if (action != 1) {
            return;
        }
        InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(event.key());
        PendingBind.apply(key);
        dev.logvex.client.utils.ChatUtils.info("Bound to " + key.getName());
        info.cancel();
    }
}
