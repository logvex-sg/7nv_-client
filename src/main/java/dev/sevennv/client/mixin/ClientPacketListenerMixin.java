package dev.sevennv.client.mixin;

import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.movement.Velocity;
import dev.sevennv.client.utils.TpsTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Packet hooks that need to run on the client thread, where game state is
 * safely accessible. The only packet that is ever altered is the local
 * player's motion packet, and only when the Velocity module is enabled.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"), cancellable = true)
    private void sevennv$onMotion(ClientboundSetEntityMotionPacket packet, CallbackInfo info) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || packet.getId() != mc.player.getId()) {
            return;
        }
        Velocity velocity = SevennvClient.getModuleManager() == null
                ? null : SevennvClient.getModuleManager().get(Velocity.class);
        if (velocity != null && velocity.isEnabled() && velocity.handleKnockback(packet)) {
            info.cancel();
        }
    }

    @Inject(method = "handleSetTime", at = @At("HEAD"))
    private void sevennv$onTime(ClientboundSetTimePacket packet, CallbackInfo info) {
        TpsTracker.onTimePacket(packet.gameTime());
    }
}
