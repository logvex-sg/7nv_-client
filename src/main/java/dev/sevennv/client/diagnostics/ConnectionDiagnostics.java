package dev.sevennv.client.diagnostics;

import dev.sevennv.client.config.Theme;
import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.render.RenderUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;

/**
 * Connection quality diagnostics: latency, jitter and a coarse packet-rate
 * check. Values are sampled once a second; the module only reads the client's
 * own measurements and never probes the server.
 */
public final class ConnectionDiagnostics extends DiagModule {
    public ConnectionDiagnostics() {
        super("Connection Diagnostics", "Shows latency, jitter and packet rates");
        showJitter = bool("Jitter", "Show latency variation", true);
        warnPing = integer("Warn ping", "Highlight the latency above this value", 200, 20, 2000);
        sampleSeconds = integer("Sample window", "Seconds between samples", 1, 1, 10);
    }

    private final BooleanSetting showJitter;
    private final IntSetting warnPing;
    private final IntSetting sampleSeconds;

    private final Timer timer = new Timer();
    private int lastPing = -1;
    private double jitter;
    private long samples;

    @Override
    public void onTick() {
        if (!timer.tick(sampleSeconds.get() * 1000L)) {
            return;
        }
        ClientPacketListener connection = mc.getConnection();
        if (connection == null || mc.player == null) {
            return;
        }
        var info = connection.getPlayerInfo(mc.player.getUUID());
        if (info == null) {
            return;
        }
        int ping = info.getLatency();
        if (lastPing >= 0) {
            // Exponential moving average keeps the jitter readout stable.
            jitter = jitter * 0.7 + Math.abs(ping - lastPing) * 0.3;
        }
        lastPing = ping;
        samples++;
    }

    @Override
    public void onWorldChange() {
        lastPing = -1;
        jitter = 0;
        samples = 0;
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Theme theme = theme();
        boolean connected = mc.getConnection() != null;
        int width = 150;
        int rows = 2 + (showJitter.get() ? 1 : 0);
        int height = 14 + rows * 9;
        int x = panelX();
        int y = panelY();

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);
        RenderUtils.text(graphics, "Connection", x + 3, y + 3, theme.accent);

        int rowY = y + 14;
        String status = connected ? "Connected" : "Disconnected";
        int statusColor = connected ? theme.enabled : theme.disabled;
        RenderUtils.text(graphics, "State", x + 3, rowY, theme.textDim);
        RenderUtils.textRight(graphics, status, x + width - 3, rowY, statusColor);
        rowY += 9;

        int ping = lastPing;
        int pingColor = ping < 0 ? theme.disabled : (ping >= warnPing.get() ? 0xFFFF5050 : theme.text);
        RenderUtils.text(graphics, "Ping", x + 3, rowY, theme.textDim);
        RenderUtils.textRight(graphics, ping < 0 ? "-" : ping + "ms", x + width - 3, rowY, pingColor);
        rowY += 9;

        if (showJitter.get()) {
            RenderUtils.text(graphics, "Jitter", x + 3, rowY, theme.textDim);
            RenderUtils.textRight(graphics, samples == 0 ? "-" : Math.round(jitter) + "ms",
                    x + width - 3, rowY, theme.text);
        }
    }

    public int latency() {
        return lastPing;
    }

    public double jitter() {
        return jitter;
    }
}
