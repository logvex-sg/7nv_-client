package dev.sevennv.client.utils;

import dev.sevennv.client.SevennvClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Chat helpers, including the client prefix. */
public final class ChatUtils {
    private ChatUtils() {
    }

    public static void info(String message) {
        display("\u00a7d[7nv_] \u00a7r" + message);
    }

    public static void error(String message) {
        display("\u00a7d[7nv_] \u00a7c" + message);
    }

    public static void moduleState(dev.sevennv.client.module.Module module) {
        String state = module.isEnabled() ? "\u00a7aON" : "\u00a77OFF";
        display("\u00a7d[7nv_] \u00a7f" + module.getName() + " " + state);
    }

    /** Shows a message locally, without sending anything to the server. */
    public static void display(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            SevennvClient.LOGGER.info("[chat] {}", message);
            return;
        }
        mc.player.displayClientMessage(Component.literal(message), false);
    }

    /**
     * Sends a chat line to the server, or logs it locally when not connected.
     *
     * <p>Uses the vanilla chat path so signed chat and the server's own
     * rate limits still apply; this is not a packet-injection shortcut.
     */
    public static void send(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) {
            SevennvClient.LOGGER.info("[chat] {}", message);
            return;
        }
        mc.getConnection().sendChat(message);
    }
}
