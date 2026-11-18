package dev.sevennv.client.hud;

import dev.sevennv.client.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;

/** Shared data gathering for the HUD elements, so none of them re-derive it. */
public final class HudData {
    private HudData() {
    }

    public static Minecraft mc() {
        return Minecraft.getInstance();
    }

    /** Round-trip latency to the current server in ms, or -1 when unknown. */
    public static int ping() {
        Minecraft mc = mc();
        ClientPacketListener connection = mc.getConnection();
        Player player = mc.player;
        if (connection == null || player == null) {
            return -1;
        }
        PlayerInfo info = connection.getPlayerInfo(player.getUUID());
        return info == null ? -1 : info.getLatency();
    }

    public static String pingText() {
        int ping = ping();
        return ping < 0 ? "0ms" : ping + "ms";
    }

    /** Blocks per second, horizontal only. */
    public static double speedBps() {
        Player player = mc().player;
        if (player == null) {
            return 0.0;
        }
        double dx = player.getX() - player.xOld;
        double dz = player.getZ() - player.zOld;
        return Math.sqrt(dx * dx + dz * dz) * 20.0;
    }

    /** Current coordinates formatted to one decimal place. */
    public static String coordsText() {
        Player player = mc().player;
        if (player == null) {
            return "0.0, 0.0, 0.0";
        }
        return MathUtils.round(player.getX(), 1) + ", "
                + MathUtils.round(player.getY(), 1) + ", "
                + MathUtils.round(player.getZ(), 1);
    }

    /** Compass direction of the player, e.g. "North-East". */
    public static String directionText() {
        Player player = mc().player;
        if (player == null) {
            return "South";
        }
        return dev.sevennv.client.utils.WorldUtils.compass(player.getYRot());
    }

    /** Biome name under the player, or "Unknown" when no world is loaded. */
    public static String biomeText() {
        Minecraft mc = mc();
        if (mc.level == null || mc.player == null) {
            return "Unknown";
        }
        var holder = mc.level.getBiome(mc.player.blockPosition());
        return holder.unwrapKey()
                .map(key -> prettify(key.identifier().getPath()))
                .orElse("Unknown");
    }

    public static String dimensionText() {
        Minecraft mc = mc();
        if (mc.level == null) {
            return "Unknown";
        }
        return prettify(mc.level.dimension().identifier().getPath());
    }

    /** The current server name, or "Singleplayer". */
    public static String serverText() {
        Minecraft mc = mc();
        if (mc.isSingleplayer()) {
            return "Singleplayer";
        }
        if (mc.getCurrentServer() != null) {
            return mc.getCurrentServer().name;
        }
        return mc.getConnection() == null ? "Disconnected" : "Multiplayer";
    }

    /** The launched game version string, e.g. "1.21.11". */
    public static String versionText() {
        return mc().getLaunchedVersion();
    }

    public static String worldTimeText() {
        Minecraft mc = mc();
        if (mc.level == null) {
            return "--:--";
        }
        long time = mc.level.getDayTime() % 24000L;
        long hours = (time / 1000L + 6L) % 24L;
        long minutes = (time % 1000L) * 60L / 1000L;
        return String.format(java.util.Locale.ROOT, "%02d:%02d", hours, minutes);
    }

    /** True when it is night time in the current world. */
    public static boolean isNight() {
        Minecraft mc = mc();
        if (mc.level == null) {
            return false;
        }
        long time = mc.level.getDayTime() % 24000L;
        return time >= 13000L && time <= 23000L;
    }

    private static String prettify(String path) {
        String[] parts = path.split("[_/]");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.isEmpty() ? path : builder.toString();
    }
}
