package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.events.PacketEvent;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;

/**
 * Smooths the camera when the server snaps the player's rotation.
 *
 * <p>Anarchy servers send frequent position/rotation corrections, and a hard
 * correction yanks the view mid-fight. When a correction arrives this module
 * records the server's rotation and eases the client's view toward it over the
 * next few ticks, so the camera settles instead of jumping.
 *
 * <p>This is presentation only: the position and rotation the client reports
 * to the server are never altered, and the vanilla packet handler still runs in
 * full. Nothing is cancelled or spoofed.
 */
public final class NoRotate extends Module {
    public NoRotate() {
        super("No Rotate", "Smooths server rotation corrections", Category.RENDER);
        factor = number("Smoothing", "Fraction of the correction applied per tick", 0.35, 0.05, 1.0, 0.05);
        ignoreTeleport = bool("Ignore teleports", "Do not smooth full position teleports", true);
    }

    private final DoubleSetting factor;
    private final BooleanSetting ignoreTeleport;

    private float targetYaw;
    private float targetPitch;
    private boolean pending;

    @Override
    public void onPacket(PacketEvent event) {
        if (mc.player == null || !(event.getPacket() instanceof ClientboundPlayerPositionPacket position)) {
            return;
        }
        if (ignoreTeleport.get() && position.relatives().isEmpty()) {
            // No relative flags means a full teleport; let vanilla handle it.
            return;
        }
        targetYaw = position.change().yRot();
        targetPitch = position.change().xRot();
        pending = true;
    }

    /** Eases the view toward the recorded rotation once per tick. */
    @Override
    public void onTick() {
        if (!pending || mc.player == null) {
            return;
        }
        float deltaYaw = wrapDegrees(targetYaw - mc.player.getYRot());
        float deltaPitch = targetPitch - mc.player.getXRot();
        if (Math.abs(deltaYaw) < 0.5f && Math.abs(deltaPitch) < 0.5f) {
            pending = false;
            return;
        }
        float step = (float) factor.get();
        mc.player.setYRot(mc.player.getYRot() + deltaYaw * step);
        mc.player.setXRot(mc.player.getXRot() + deltaPitch * step);
    }

    @Override
    public void onWorldChange() {
        pending = false;
    }

    private static float wrapDegrees(float degrees) {
        float wrapped = degrees % 360.0f;
        if (wrapped >= 180.0f) {
            wrapped -= 360.0f;
        }
        if (wrapped < -180.0f) {
            wrapped += 360.0f;
        }
        return wrapped;
    }
}
