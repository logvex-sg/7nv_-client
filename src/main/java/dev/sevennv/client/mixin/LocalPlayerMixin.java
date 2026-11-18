package dev.sevennv.client.mixin;

import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.module.ModuleManager;
import dev.sevennv.client.movement.NoSlow;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cancels the movement slowdown applied while using an item (and, optionally,
 * while sneaking) by returning the move impulse without those scales. Only the
 * client's own input vector is touched, so the server still sees normal
 * movement packets.
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Inject(method = "modifyInput", at = @At("RETURN"), cancellable = true)
    private void sevennv$noSlow(Vec2 input, CallbackInfoReturnable<Vec2> info) {
        // The zero-vector early return must stay untouched.
        if (input == null || input.lengthSquared() == 0.0f) {
            return;
        }
        ModuleManager manager = SevennvClient.getModuleManager();
        NoSlow noSlow = manager == null ? null : manager.get(NoSlow.class);
        if (noSlow == null || !noSlow.isEnabled()) {
            return;
        }
        LocalPlayer self = (LocalPlayer) (Object) this;
        boolean usingItem = self.isUsingItem() && !self.isPassenger();
        boolean sneaking = noSlow.cancelsSneakSlow() && self.isMovingSlowly();
        if (!usingItem && !sneaking) {
            return;
        }
        // The vanilla key impulse is already normalized, so dropping the item-use
        // and sneak scales yields the full-speed vector.
        info.setReturnValue(input.scale(0.98f));
    }
}
