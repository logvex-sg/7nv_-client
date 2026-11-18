package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.phys.Vec3;

/**
 * Adds a small forward impulse while gliding with an elytra. The boost only
 * runs while the player is actually fall-flying, and is capped per second so it
 * never turns into a teleport.
 */
public final class ElytraBoost extends Module {
    public ElytraBoost() {
        super("Elytra Boost", "Adds a forward nudge while gliding", Category.MOVEMENT);
        strength = number("Strength", "Forward impulse per boost", 0.05, 0.01, 0.3, 0.01);
        interval = integer("Interval", "Milliseconds between boosts", 200, 50, 2000);
        requireFirework = bool("Require firework", "Only boost while a rocket is in use", false);
    }

    private final dev.sevennv.client.config.DoubleSetting strength;
    private final IntSetting interval;
    private final BooleanSetting requireFirework;

    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        if (!mc.player.isFallFlying()) {
            return;
        }
        if (requireFirework.get() && !mc.player.isUsingItem()) {
            return;
        }
        if (!timer.tick(interval.get())) {
            return;
        }
        Vec3 look = mc.player.getLookAngle();
        Vec3 motion = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(motion.x + look.x * strength.get(),
                motion.y + look.y * strength.get() * 0.5,
                motion.z + look.z * strength.get());
    }
}
