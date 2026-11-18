package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.NotificationManager;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.phys.Vec3;

/**
 * Detects a fall into the void and warns the player.
 *
 * <p>When "Catch" is enabled the player's downward motion is zeroed for a short
 * window, giving them a moment to react. This is a client-side assist only: it
 * cannot and does not move the player back to safety.
 */
public final class AntiVoid extends Module {
    public AntiVoid() {
        super("Anti Void", "Warns when you are about to fall into the void", Category.MOVEMENT);
        threshold = integer("Y level", "Warn below this height", -64, -128, 32);
        warningDistance = integer("Warn distance", "Blocks of free fall before warning", 5, 1, 40);
        catchFall = bool("Catch", "Zero downward motion while falling in the void", true);
        cooldown = integer("Cooldown", "Milliseconds between warnings", 2000, 200, 10000);
    }

    private final IntSetting threshold;
    private final IntSetting warningDistance;
    private final BooleanSetting catchFall;
    private final IntSetting cooldown;

    private final Timer timer = new Timer();
    private boolean warned;

    @Override
    public void onWorldChange() {
        warned = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) {
            return;
        }
        boolean voidRisk = mc.player.getY() < threshold.get();
        if (!voidRisk) {
            warned = false;
            return;
        }
        if (catchFall.get() && mc.player.getDeltaMovement().y < 0) {
            Vec3 motion = mc.player.getDeltaMovement();
            mc.player.setDeltaMovement(motion.x, 0.0, motion.z);
        }
        if (!warned && timer.tick(cooldown.get())) {
            warned = true;
            NotificationManager.warn("Anti Void",
                    "Falling below Y " + threshold.get() + " at " + Math.round(mc.player.getY()));
        }
    }
}
