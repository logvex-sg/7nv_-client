package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.NotificationManager;
import net.minecraft.world.phys.Vec3;

/**
 * Detects a fall into the void and warns the player once per fall.
 *
 * <p>When "Catch" is enabled the player's downward motion is zeroed while the
 * fall is inside the warning window, giving them a moment to react. This is a
 * client-side assist only: it cannot and does not move the player back to
 * safety.
 */
public final class AntiVoid extends Module {
    public AntiVoid() {
        super("Anti Void", "Warns when you are about to fall into the void", Category.MOVEMENT);
        threshold = integer("Y level", "Warn below this height", -64, -128, 32);
        warningDistance = integer("Warn distance", "Blocks of free fall before warning", 5, 1, 40);
        catchFall = bool("Catch", "Zero downward motion while falling in the void", true);
    }

    private final IntSetting threshold;
    private final IntSetting warningDistance;
    private final BooleanSetting catchFall;

    private double fallStartY;
    private boolean falling;
    private boolean warned;

    @Override
    public void onWorldChange() {
        falling = false;
        warned = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) {
            return;
        }
        boolean descending = mc.player.getDeltaMovement().y < -0.1 && !mc.player.onGround();
        if (!descending || mc.player.getY() >= threshold.get()) {
            falling = false;
            warned = false;
            return;
        }
        if (!falling) {
            falling = true;
            fallStartY = mc.player.getY();
        }
        if (catchFall.get()) {
            Vec3 motion = mc.player.getDeltaMovement();
            mc.player.setDeltaMovement(motion.x, 0.0, motion.z);
        }
        if (!warned && fallStartY - mc.player.getY() >= warningDistance.get()) {
            warned = true;
            NotificationManager.warn("Anti Void", "Falling below Y " + threshold.get()
                    + " at " + Math.round(mc.player.getY()) + " (" + warningDistance.get() + "+ blocks)");
        }
    }
}
