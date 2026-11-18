package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.phys.Vec3;

/**
 * Keeps the player swimming forward while the forward key is held, matching the
 * auto-sprint behaviour for land movement. Only the local player's own sprint
 * flag is set; nothing is sent beyond the ordinary movement the client already
 * produces.
 */
public final class AutoSwim extends Module {
    public AutoSwim() {
        super("Auto Swim", "Sprints automatically while swimming", Category.MOVEMENT);
        onlyForward = bool("Only forward", "Only sprint while moving forward", true);
        checkInterval = integer("Check interval", "Milliseconds between checks", 100, 0, 1000);
    }

    private final BooleanSetting onlyForward;
    private final IntSetting checkInterval;
    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || !timer.tick(checkInterval.get())) {
            return;
        }
        if (!mc.player.isInWater() && !mc.player.isSwimming()) {
            return;
        }
        if (onlyForward.get()) {
            Vec3 motion = mc.player.getDeltaMovement();
            if (motion.horizontalDistanceSqr() < 1.0E-4) {
                return;
            }
        }
        mc.player.setSprinting(true);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setSprinting(false);
        }
    }
}
