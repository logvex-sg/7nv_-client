package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.EnumSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.RotationUtils;
import dev.sevennv.client.utils.TargetUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Nudges your aim toward the nearest target while you are already attacking or
 * holding the aim key. It never attacks for you and never turns past the
 * configured speed, so the result stays inside what a player could do by hand.
 *
 * <p>Two modes: a visible assist that moves your real view (so you keep control
 * and can pull away at any time), and a silent assist that only reports the
 * rotation to the server without moving your camera. The visible mode is the
 * default because it is the least surprising.
 */
public final class AimAssist extends Module {
    public AimAssist() {
        super("Aim Assist", "Smoothly pulls your aim toward nearby targets", Category.COMBAT);
        range = number("Range", "Maximum target distance", 5.0, 1.0, 8.0, 0.5);
        targets = option("Targets", "Which entities to assist toward", TargetUtils.Targets.PLAYERS);
        speed = number("Speed", "Maximum degrees turned per tick", 4.0, 0.5, 20.0, 0.5);
        fov = number("FOV", "Only assist within this field of view", 70.0, 10.0, 180.0, 5.0);
        mode = option("Mode", "How the rotation is applied", Mode.VISIBLE);
        requireAim = bool("Require aim", "Only assist while attacking or holding a key", true);
        vertical = bool("Vertical", "Also adjust pitch", true);
        minHealth = number("Min health", "Stop assisting below this health", 0.0, 0.0, 20.0, 0.5);
    }

    public enum Mode {
        VISIBLE,
        SILENT
    }

    private final DoubleSetting range;
    private final EnumSetting<TargetUtils.Targets> targets;
    private final DoubleSetting speed;
    private final DoubleSetting fov;
    private final EnumSetting<Mode> mode;
    private final BooleanSetting requireAim;
    private final BooleanSetting vertical;
    private final DoubleSetting minHealth;

    private final Timer timer = new Timer();
    private LivingEntity current;

    @Override
    public void onWorldChange() {
        current = null;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (mc.player.getHealth() < minHealth.get()) {
            current = null;
            return;
        }
        if (requireAim.get() && !mc.options.keyAttack.isDown()
                && !mc.options.keyUse.isDown()) {
            current = null;
            return;
        }
        LivingEntity target = pick();
        current = target;
        if (target == null) {
            return;
        }
        float[] angles = RotationUtils.anglesTo(mc.player.getEyePosition(),
                target.getEyePosition());
        float yaw = mc.player.getYRot();
        float pitch = mc.player.getXRot();
        float deltaYaw = Mth.wrapDegrees(angles[0] - yaw);
        float deltaPitch = angles[1] - pitch;
        float step = (float) speed.get();
        yaw += Mth.clamp(deltaYaw, -step, step);
        if (vertical.get()) {
            pitch += Mth.clamp(deltaPitch, -step, step);
        }
        pitch = Mth.clamp(pitch, -90.0f, 90.0f);
        if (mode.get() == Mode.SILENT) {
            dev.sevennv.client.utils.Rotation.sendRotation(yaw, pitch);
        } else {
            mc.player.setYRot(yaw);
            mc.player.setXRot(pitch);
        }
    }

    private LivingEntity pick() {
        double rangeSq = range.get() * range.get();
        double cosFov = Math.cos(Math.toRadians(fov.get() * 0.5));
        Vec3 look = mc.player.getLookAngle();
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        for (LivingEntity candidate : TargetUtils.candidates(range.get(), targets.get())) {
            Vec3 toTarget = candidate.getEyePosition().subtract(mc.player.getEyePosition());
            double distance = toTarget.length();
            if (distance < 0.001 || distance * distance > rangeSq) {
                continue;
            }
            double dot = look.dot(toTarget.scale(1.0 / distance));
            if (dot < cosFov) {
                continue;
            }
            if (distance < bestScore) {
                bestScore = distance;
                best = candidate;
            }
        }
        return best;
    }

    @Override
    public String getArraylistSuffix() {
        return current != null ? current.getName().getString() : "";
    }
}
