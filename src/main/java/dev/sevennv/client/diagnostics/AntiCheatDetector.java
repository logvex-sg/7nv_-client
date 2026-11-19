package dev.sevennv.client.diagnostics;

import dev.sevennv.client.config.Theme;
import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.events.PacketEvent;
import dev.sevennv.client.utils.NotificationManager;
import dev.sevennv.client.utils.ServerFingerprint;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Names the anti-cheat, proxy and server software the current connection is
 * running, from data the server already sends (brand, plugin channels, command
 * tree, tab text). It also lists the top non-anti-cheat plugins it recognises.
 *
 * <p>This is descriptive only. It never probes the server, never sends an extra
 * packet and never attempts to evade anything — it is an information readout so
 * you know what you are connected to.
 */
public final class AntiCheatDetector extends DiagModule {
    public AntiCheatDetector() {
        super("Anti Cheat Detector", "Identifies the server's anti-cheat and software");
        notifyOnJoin = bool("Notify on join", "Announce the detection once per connection", true);
        showPlugins = bool("Show plugins", "Also list recognised plugins", true);
        showEvidence = bool("Show evidence", "Show the signal each match came from", false);
        showServer = bool("Show server software", "Include the detected server flavour", true);
        pluginLimit = integer("Plugin limit", "How many plugins to list", 3, 0, 8);
    }

    private final BooleanSetting notifyOnJoin;
    private final BooleanSetting showPlugins;
    private final BooleanSetting showEvidence;
    private final BooleanSetting showServer;
    private final IntSetting pluginLimit;

    private String lastNotified = "";
    private boolean announced;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getDirection() == PacketEvent.Direction.INBOUND) {
            ServerFingerprint.observe(event);
        }
    }

    @Override
    public void onTick() {
        if (mc.getConnection() == null) {
            return;
        }
        ServerFingerprint.sample(mc);
        if (!announced && notifyOnJoin.get() && ServerFingerprint.hasSignals()) {
            announced = true;
            ServerFingerprint.Match antiCheat = ServerFingerprint.bestAntiCheat();
            String summary = antiCheat == null
                    ? "none detected (" + ServerFingerprint.brand() + ")"
                    : antiCheat.name() + " \u00a77" + percent(antiCheat.confidence());
            lastNotified = summary;
            NotificationManager.info("Anti-cheat", summary);
        }
    }

    @Override
    public void onEnable() {
        announced = false;
    }

    @Override
    public void onWorldChange() {
        announced = false;
        lastNotified = "";
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Theme theme = theme();
        DiagPanel panel = new DiagPanel().title("Anti Cheat Detector");
        if (mc.getConnection() == null) {
            panel.row("State", "Disconnected");
            drawPanel(graphics, panel);
            return;
        }

        ServerFingerprint.Match antiCheat = ServerFingerprint.bestAntiCheat();
        if (antiCheat == null) {
            panel.row("Anti-cheat", ServerFingerprint.hasSignals() ? "none matched" : "scanning");
        } else {
            panel.row("Anti-cheat", antiCheat.name() + "  " + percent(antiCheat.confidence()));
            if (showEvidence.get() && !antiCheat.evidence().isEmpty()) {
                panel.row("  via", String.join(", ", antiCheat.evidence()));
            }
        }

        if (showServer.get()) {
            ServerFingerprint.Match server = ServerFingerprint.best(ServerFingerprint.Category.SERVER_SOFTWARE);
            String brand = ServerFingerprint.brand();
            String value = server == null
                    ? (brand.isEmpty() ? "unknown" : brand)
                    : server.name();
            panel.row("Server", value);
        }

        ServerFingerprint.Match proxy = ServerFingerprint.best(ServerFingerprint.Category.PROXY);
        if (proxy != null) {
            panel.row("Proxy", proxy.name());
        }

        if (showPlugins.get() && pluginLimit.get() > 0) {
            List<ServerFingerprint.Match> plugins = plugins();
            for (int i = 0; i < Math.min(pluginLimit.get(), plugins.size()); i++) {
                ServerFingerprint.Match plugin = plugins.get(i);
                panel.row("Plugin", plugin.name() + "  " + percent(plugin.confidence()));
            }
        }

        drawPanel(graphics, panel);
    }

    private List<ServerFingerprint.Match> plugins() {
        List<ServerFingerprint.Match> plugins = new ArrayList<>();
        for (ServerFingerprint.Match match : ServerFingerprint.detect()) {
            if (match.category() == ServerFingerprint.Category.PLUGIN) {
                plugins.add(match);
            }
        }
        return plugins;
    }

    private static String percent(double confidence) {
        return String.format(Locale.ROOT, "%d%%", Math.round(confidence * 100));
    }

    /** Last announced summary, for other modules or the HUD. */
    public String lastSummary() {
        return lastNotified;
    }
}
