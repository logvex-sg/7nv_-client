package dev.logvex.client.diagnostics;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.Theme;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.player.Player;

/**
 * Reports which optional server features the current connection supports, based
 * on data the client already negotiated during login. This is descriptive only:
 * no capability probe is sent to the server.
 */
public final class ServerCapabilityDetection extends Module {
    public ServerCapabilityDetection() {
        super("Server Capability Detection", "Reports negotiated server features", Category.DIAGNOSTICS);
        showSimulation = bool("Simulation distance", "Show the server simulation distance", true);
        showSecureChat = bool("Secure chat", "Show whether chat signing is enforced", true);
        showBrand = bool("Brand", "Show the detected server brand", true);
    }

    private final BooleanSetting showSimulation;
    private final BooleanSetting showSecureChat;
    private final BooleanSetting showBrand;

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Theme theme = theme();
        ClientPacketListener connection = mc.getConnection();
        DiagPanel panel = new DiagPanel().title("Server Capabilities");

        if (connection == null) {
            panel.row("State", "Disconnected");
        } else {
            panel.row("State", "Connected");
            panel.row("Type", mc.isSingleplayer() ? "Integrated" : "Remote");
            if (showBrand.get()) {
                String brand = connection.serverBrand();
                panel.row("Brand", brand == null || brand.isBlank() ? "unknown" : brand);
            }
            if (showSimulation.get()) {
                panel.row("Sim distance", mc.level == null ? "-" : mc.level.getServerSimulationDistance() + " chunks");
            }
            if (showSecureChat.get()) {
                panel.row("Secure chat", connection.isAcceptingMessages() ? "yes" : "no");
            }
            Player player = mc.player;
            panel.row("Listed players", connection.getOnlinePlayers() == null
                    ? "-" : String.valueOf(connection.getOnlinePlayers().size()));
            if (player != null) {
                panel.row("Own entry", connection.getPlayerInfo(player.getUUID()) == null ? "absent" : "present");
            }
        }
        panel.render(graphics, theme, 4, 4);
    }

    private Theme theme() {
        var config = dev.logvex.client.LogvexClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
