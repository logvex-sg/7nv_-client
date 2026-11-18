package dev.sevennv.client.diagnostics;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.ExposureAudit;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * A self-audit of how exposed your currently enabled modules are to a typical
 * anti-cheat, plus the server's own detection surface (latency, jitter, TPS).
 *
 * <p>The assessment is a heuristic based on how far each module diverges from
 * vanilla client behaviour. It is not a promise about any specific server and
 * does not suggest how to evade detection — it only shows your footprint so you
 * can make your own choices.
 */
public final class DetectionExposure extends Module {
    public DetectionExposure() {
        super("Detection Exposure", "Self-audit of your own anti-cheat exposure", Category.DIAGNOSTICS);
        maxRows = integer("Rows", "How many risky modules to list", 5, 1, 15);
        showReasons = bool("Show reasons", "Explain why each module is flagged", true);
        showServer = bool("Server signals", "Include latency, jitter and TPS", true);
        warnOnHigh = bool("Warn on high", "Chat-warn when a high-risk module turns on", false);
    }

    private final IntSetting maxRows;
    private final BooleanSetting showReasons;
    private final BooleanSetting showServer;
    private final BooleanSetting warnOnHigh;

    private List<ExposureAudit.Assessment> assessments = List.of();
    private int high;
    private int medium;
    private int low;
    private String lastWarned = "";

    @Override
    public void onTick() {
        if (mc.player == null) {
            assessments = List.of();
            return;
        }
        List<Module> enabled = dev.sevennv.client.SevennvClient.getModuleManager().getEnabled();
        assessments = ExposureAudit.audit(enabled);
        high = ExposureAudit.count(assessments, ExposureAudit.Risk.HIGH);
        medium = ExposureAudit.count(assessments, ExposureAudit.Risk.MEDIUM);
        low = ExposureAudit.count(assessments, ExposureAudit.Risk.LOW);

        if (warnOnHigh.get() && high > 0) {
            String first = assessments.isEmpty() ? "" : assessments.get(0).module();
            if (!first.equals(lastWarned)) {
                lastWarned = first;
                dev.sevennv.client.utils.NotificationManager.warn("Exposure",
                        high + " high-risk module" + (high == 1 ? "" : "s") + " enabled");
            }
        } else if (high == 0) {
            lastWarned = "";
        }
    }

    @Override
    public void onWorldChange() {
        lastWarned = "";
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Theme theme = theme();
        DiagPanel panel = new DiagPanel().title("Detection Exposure");
        panel.row("High", String.valueOf(high));
        panel.row("Medium", String.valueOf(medium));
        panel.row("Low", String.valueOf(low));

        int shown = 0;
        for (ExposureAudit.Assessment assessment : assessments) {
            if (shown >= maxRows.get()) {
                break;
            }
            String value = assessment.risk().label();
            if (showReasons.get()) {
                value = value + " - " + assessment.reason();
            }
            panel.row(assessment.module(), value);
            shown++;
        }

        if (showServer.get()) {
            appendServer(panel);
        }
        panel.render(graphics, theme, 4, 4);
    }

    private void appendServer(DiagPanel panel) {
        ConnectionDiagnostics connection = dev.sevennv.client.SevennvClient.getModuleManager()
                .get(ConnectionDiagnostics.class);
        if (connection != null && connection.isEnabled()) {
            int latency = connection.latency();
            panel.row("Latency", latency < 0 ? "-" : latency + "ms");
            panel.row("Jitter", Math.round(connection.jitter()) + "ms");
        }
        panel.row("TPS", dev.sevennv.client.utils.TpsTracker.format());
    }

    /** The current risk counts, for the HUD. */
    public int highRiskCount() {
        return high;
    }

    public int mediumRiskCount() {
        return medium;
    }

    private Theme theme() {
        var config = dev.sevennv.client.SevennvClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
