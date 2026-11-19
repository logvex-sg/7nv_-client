package dev.sevennv.client.diagnostics;

import dev.sevennv.client.config.Theme;
import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.events.PacketEvent;
import dev.sevennv.client.utils.Timer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

/**
 * Detects client/server position desynchronisation.
 *
 * <p>The authoritative reference is the last {@code ClientboundPlayerPosition}
 * correction the server sent; it is compared with the position the client most
 * recently reported. This is purely a readout: it never re-sends a position and
 * never attempts to correct the server's view.
 */
public final class DesyncDetector extends DiagModule {
    public DesyncDetector() {
        super("Desync Detector", "Reports drift between local and server position");
        threshold = number("Threshold", "Distance in blocks before it counts as desync", 0.5, 0.05, 10.0, 0.05);
        warnOnly = bool("Warn only", "Only report, never correct", true);
        showVector = bool("Show offset", "Show the offset components", true);
        staleMillis = integer("Stale after", "Mark the reading stale after this many ms", 2000, 200, 20000);
    }

    private final dev.sevennv.client.config.DoubleSetting threshold;
    private final BooleanSetting warnOnly;
    private final BooleanSetting showVector;
    private final IntSetting staleMillis;

    private final Timer sinceCorrection = new Timer();
    private Vec3 serverPos;
    private Vec3 sentPos;
    private double distance;
    private boolean desynced;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof ClientboundPlayerPositionPacket position) {
            serverPos = position.change().position();
            sinceCorrection.reset();
            return;
        }
        if (event.getPacket() instanceof ServerboundMovePlayerPacket move && move.hasPosition() && mc.player != null) {
            sentPos = new Vec3(move.getX(mc.player.getX()), move.getY(mc.player.getY()), move.getZ(mc.player.getZ()));
        }
    }

    @Override
    public void onTick() {
        if (serverPos == null || sentPos == null) {
            return;
        }
        distance = sentPos.distanceTo(serverPos);
        desynced = distance > threshold.get();
    }

    @Override
    public void onWorldChange() {
        serverPos = null;
        sentPos = null;
        distance = 0;
        desynced = false;
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Theme theme = theme();
        DiagPanel panel = new DiagPanel().title("Desync Detector");
        if (serverPos == null || sentPos == null) {
            panel.row("State", "awaiting data");
        } else {
            panel.row("State", desynced ? "drift" : "synced");
            panel.row("Offset", String.format(java.util.Locale.ROOT, "%.2f", distance) + "m");
            if (sinceCorrection.elapsed() > staleMillis.get()) {
                panel.row("Age", "stale");
            }
            if (showVector.get()) {
                Vec3 delta = sentPos.subtract(serverPos);
                panel.row("X", String.format(java.util.Locale.ROOT, "%.2f", delta.x));
                panel.row("Y", String.format(java.util.Locale.ROOT, "%.2f", delta.y));
                panel.row("Z", String.format(java.util.Locale.ROOT, "%.2f", delta.z));
            }
        }
        if (warnOnly.get() && desynced) {
            panel.row("Note", "report only");
        }
        drawPanel(graphics, panel);
    }

    public boolean isDesynced() {
        return desynced;
    }

    public double offset() {
        return distance;
    }
}
