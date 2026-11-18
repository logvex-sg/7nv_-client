package dev.sevennv.client.events;

import net.minecraft.network.protocol.Packet;

/**
 * Observational hook for the diagnostics modules. Fired for every packet that
 * passes through the client connection in either direction.
 */
public class PacketEvent {
    public enum Direction {
        INBOUND,
        OUTBOUND
    }

    private final Packet<?> packet;
    private final Direction direction;

    public PacketEvent(Packet<?> packet, Direction direction) {
        this.packet = packet;
        this.direction = direction;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public Direction getDirection() {
        return direction;
    }

    public String getPacketName() {
        String name = packet.getClass().getSimpleName();
        return name.isEmpty() ? packet.getClass().getSimpleName() : name;
    }
}
