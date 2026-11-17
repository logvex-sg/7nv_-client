package dev.logvex.client.mixin;

import dev.logvex.client.LogvexClient;
import dev.logvex.client.events.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Observational packet hook for the diagnostics modules. Nothing here alters,
 * drops or delays a packet: the events are posted read-only, before the
 * vanilla send/receive path runs.
 */
@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void logvex$observeOutbound(Packet<?> packet, CallbackInfo info) {
        postSafely(packet, PacketEvent.Direction.OUTBOUND);
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
            at = @At("HEAD"))
    private void logvex$observeInbound(ChannelHandlerContext context, Packet<?> packet, CallbackInfo info) {
        postSafely(packet, PacketEvent.Direction.INBOUND);
    }

    private static void postSafely(Packet<?> packet, PacketEvent.Direction direction) {
        try {
            dev.logvex.client.utils.PacketLog.record(direction, packet);
        } catch (Exception exception) {
            // Never let an observer break the network thread.
            LogvexClient.LOGGER.debug("Packet observer failed", exception);
        }
    }
}
