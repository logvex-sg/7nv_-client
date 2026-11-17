package dev.logvex.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;

/**
 * Packet send helpers for the interactive modules. Everything here goes through
 * the normal vanilla connection, so the server sees ordinary player actions.
 */
public final class PacketUtils {
    private PacketUtils() {
    }

    public static void send(Packet<?> packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.connection != null) {
            mc.player.connection.send(packet);
        }
    }

    /** Swings the arm client-side, so the swing animation plays. */
    public static void swing() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    /**
     * Attacks an entity through the normal game mode path, which sends the
     * interact packet and applies local prediction.
     */
    public static boolean attack(Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null || entity == null) {
            return false;
        }
        mc.gameMode.attack(mc.player, entity);
        mc.player.swing(InteractionHand.MAIN_HAND);
        return true;
    }
}
