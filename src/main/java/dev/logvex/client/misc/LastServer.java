package dev.logvex.client.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

/**
 * Remembers the server address most recently joined, so the reconnect module
 * has something to dial. The address is captured on join and cleared only when
 * a different server is joined.
 */
public final class LastServer {
    private static String address;
    private static String name;

    private LastServer() {
    }

    /** Records the address of the server the client is currently joining. */
    public static void capture(Minecraft mc) {
        if (mc == null) {
            return;
        }
        ServerData data = mc.getCurrentServer();
        if (data != null && data.ip != null && !data.ip.isBlank()) {
            address = data.ip;
            name = data.name == null ? data.ip : data.name;
        }
    }

    public static String address() {
        return address;
    }

    public static String name() {
        return name == null ? address : name;
    }

    public static boolean hasAddress() {
        return address != null && !address.isBlank();
    }

    public static void clear() {
        address = null;
        name = null;
    }
}
