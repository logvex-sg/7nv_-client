package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.KeybindSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.world.phys.Vec3;

/**
 * Detaches the camera from the player so the world can be inspected from a
 * distance. The player's own motion is frozen while freecam is active, so the
 * body stays put and only the viewpoint moves.
 *
 * <p>The camera still uses the player's look angles, so normal mouse look
 * steers the free camera.
 */
public final class Freecam extends Module {
    public Freecam() {
        super("Freecam", "Detaches the camera from your player", Category.RENDER);
        key = keybindSetting("Key", "Key to toggle freecam");
        speed = number("Speed", "Movement speed in blocks per tick", 0.5, 0.05, 3.0, 0.05);
        vertical = number("Vertical", "Vertical movement multiplier", 1.0, 0.1, 3.0, 0.1);
        freezePlayer = bool("Freeze player", "Hold your player still while detached", true);
    }

    private final KeybindSetting key;
    private final DoubleSetting speed;
    private final DoubleSetting vertical;
    private final BooleanSetting freezePlayer;

    private Vec3 position = Vec3.ZERO;

    @Override
    public void onEnable() {
        if (mc.player != null) {
            position = mc.player.getEyePosition();
        }
    }

    @Override
    public void onWorldChange() {
        if (mc.player != null) {
            position = mc.player.getEyePosition();
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        double step = speed.get();
        float yaw = mc.player.getYRot();
        double forwardX = -Math.sin(Math.toRadians(yaw));
        double forwardZ = Math.cos(Math.toRadians(yaw));
        double rightX = -Math.sin(Math.toRadians(yaw + 90.0));
        double rightZ = Math.cos(Math.toRadians(yaw + 90.0));

        double dx = 0;
        double dy = 0;
        double dz = 0;
        if (mc.options.keyUp.isDown()) {
            dx += forwardX;
            dz += forwardZ;
        }
        if (mc.options.keyDown.isDown()) {
            dx -= forwardX;
            dz -= forwardZ;
        }
        if (mc.options.keyRight.isDown()) {
            dx += rightX;
            dz += rightZ;
        }
        if (mc.options.keyLeft.isDown()) {
            dx -= rightX;
            dz -= rightZ;
        }
        if (mc.options.keyJump.isDown()) {
            dy += vertical.get();
        }
        if (mc.options.keyShift.isDown()) {
            dy -= vertical.get();
        }
        position = position.add(dx * step, dy * step, dz * step);

        if (freezePlayer.get()) {
            mc.player.setDeltaMovement(Vec3.ZERO);
            mc.player.zza = 0f;
            mc.player.xxa = 0f;
        }
    }

    public Vec3 cameraPosition() {
        return position;
    }

    public float cameraYaw() {
        return mc.player == null ? 0f : mc.player.getYRot();
    }

    public float cameraPitch() {
        return mc.player == null ? 0f : mc.player.getXRot();
    }
}
