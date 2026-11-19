package dev.sevennv.client.mixin;

import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.movement.Velocity;
import dev.sevennv.client.render.NoRotate;
import dev.sevennv.client.utils.TpsTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket;
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

    /**
     * Records the player's rotation immediately before vanilla applies a
     * server rotation correction, then measures the jump once vanilla has
     * written the new rotation. Vanilla overwrites the rotation in place inside
     * these handlers, so this is the only place both sides of the change can be
     * seen. Nothing is modified here.
     */
    @Inject(method = "handleMovePlayer", at = @At("HEAD"))
    private void sevennv$beforeMove(ClientboundPlayerPositionPacket packet, CallbackInfo info) {
        NoRotate noRotate = noRotate();
        if (noRotate != null) {
            noRotate.capturePreRotation(Minecraft.getInstance().player.getYRot(),
                    Minecraft.getInstance().player.getXRot());
        }
    }

    @Inject(method = "handleMovePlayer", at = @At("TAIL"))
    private void sevennv$afterMove(ClientboundPlayerPositionPacket packet, CallbackInfo info) {
        NoRotate noRotate = noRotate();
        if (noRotate != null) {
            noRotate.applyCorrection();
        }
    }

    @Inject(method = "handleRotatePlayer", at = @At("HEAD"))
    private void sevennv$beforeRotate(ClientboundPlayerRotationPacket packet, CallbackInfo info) {
        NoRotate noRotate = noRotate();
        if (noRotate != null) {
            noRotate.capturePreRotation(Minecraft.getInstance().player.getYRot(),
                    Minecraft.getInstance().player.getXRot());
        }
    }

    @Inject(method = "handleRotatePlayer", at = @At("TAIL"))
    private void sevennv$afterRotate(ClientboundPlayerRotationPacket packet, CallbackInfo info) {
        NoRotate noRotate = noRotate();
        if (noRotate != null) {
            noRotate.applyCorrection();
        }
    }

    private static NoRotate noRotate() {
        if (SevennvClient.getModuleManager() == null || Minecraft.getInstance().player == null) {
            return null;
        }
        NoRotate noRotate = SevennvClient.getModuleManager().get(NoRotate.class);
        return noRotate != null && noRotate.isEnabled() ? noRotate : null;
    }
}
