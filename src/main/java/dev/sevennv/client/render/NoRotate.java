package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Eases the camera when the server snaps the player's rotation.
 *
 * <p>Anarchy servers send frequent rotation corrections and a hard correction
 * yanks the view mid-fight. The correction cannot be intercepted on the client
 * tick: {@code Minecraft.runTick} drains the packet queue before it ticks, so by
 * the time any tick handler runs the player's rotation has already been
 * overwritten. Instead the packet mixin records the rotation the player had
 * <em>before</em> vanilla applied the correction, and this module keeps the
 * difference as a decaying camera offset.
 *
 * <p>The offset is applied to the camera only. The player entity keeps the exact
 * rotation the server asked for, so movement, hit registration and the position
 * the client reports back are untouched — nothing is spoofed and no packet is
 * cancelled.
 */
public final class NoRotate extends Module {
    public NoRotate() {
        super("No Rotate", "Eases the camera through server rotation corrections", Category.RENDER);
        strength = number("Strength", "Fraction of the remaining offset removed per tick", 0.4, 0.1, 1.0, 0.05);
        maxOffset = number("Max offset", "Largest camera offset in degrees", 90.0, 5.0, 180.0, 5.0);
        ignoreTeleport = bool("Ignore teleports", "Do not smooth full position teleports", true);
    }

    private final DoubleSetting strength;
    private final DoubleSetting maxOffset;
    private final BooleanSetting ignoreTeleport;

    private float preYaw;
    private float prePitch;
    private double preX;
    private double preY;
    private double preZ;
    private boolean havePre;

    private float offsetYaw;
    private float offsetPitch;
    private boolean active;

    /**
     * Called from the packet mixin immediately before vanilla applies a
     * rotation change. Nothing is mutated here; the current view is only
     * remembered so the jump can be measured afterwards.
     */
    public void capturePreRotation(float yaw, float pitch) {
        if (mc.player == null) {
            return;
        }
        preYaw = yaw;
        prePitch = pitch;
        preX = mc.player.getX();
        preY = mc.player.getY();
        preZ = mc.player.getZ();
        havePre = true;
    }

    /**
     * Called from the packet mixin immediately after vanilla applied the
     * change. The player entity now holds the server's rotation, so the
     * difference to the remembered view is exactly how far the camera would
     * have jumped.
     */
    public void applyCorrection() {
        if (!havePre || mc.player == null) {
            havePre = false;
            return;
        }
        havePre = false;

        double dx = mc.player.getX() - preX;
        double dy = mc.player.getY() - preY;
        double dz = mc.player.getZ() - preZ;
        if (ignoreTeleport.get() && dx * dx + dy * dy + dz * dz > 16.0) {
            // A real teleport moved the player; easing the view there would just
            // look like the world lagging behind.
            return;
        }

        float limit = (float) maxOffset.get();
        offsetYaw = clamp(wrapDegrees(preYaw - mc.player.getYRot()), limit);
        offsetPitch = clamp(prePitch - mc.player.getXRot(), limit);
        active = Math.abs(offsetYaw) >= 0.1f || Math.abs(offsetPitch) >= 0.1f;
    }

    @Override
    public void onTick() {
        if (!active) {
            return;
        }
        if (mc.player == null) {
            reset();
            return;
        }
        float step = (float) strength.get();
        offsetYaw -= offsetYaw * step;
        offsetPitch -= offsetPitch * step;
        if (Math.abs(offsetYaw) < 0.1f && Math.abs(offsetPitch) < 0.1f) {
            reset();
        }
    }

    @Override
    public void onDisable() {
        reset();
    }

    @Override
    public void onWorldChange() {
        reset();
    }

    private void reset() {
        offsetYaw = 0;
        offsetPitch = 0;
        active = false;
        havePre = false;
    }

    /** Camera-only yaw offset in degrees; zero when nothing is being smoothed. */
    public float cameraYawOffset() {
        return active ? offsetYaw : 0;
    }

    /** Camera-only pitch offset in degrees; zero when nothing is being smoothed. */
    public float cameraPitchOffset() {
        return active ? offsetPitch : 0;
    }

    private static float clamp(float value, float limit) {
        return Math.max(-limit, Math.min(limit, value));
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
