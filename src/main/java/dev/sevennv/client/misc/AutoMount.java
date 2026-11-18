package dev.sevennv.client.misc;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

/**
 * Mounts the nearest horse-like animal you own when you are on foot. Only
 * animals the client considers tamed and within reach are mounted, and the
 * mount goes through the vanilla interaction path.
 */
public final class AutoMount extends Module {
    public AutoMount() {
        super("Auto Mount", "Mounts a nearby horse when you are on foot", Category.MISC);
        range = number("Range", "Maximum mount distance", 4.0, 1.0, 6.0, 0.5);
        delay = integer("Delay", "Milliseconds between attempts", 500, 100, 3000);
        tamedOnly = bool("Tamed only", "Only mount tamed horses", true);
    }

    private final dev.sevennv.client.config.DoubleSetting range;
    private final IntSetting delay;
    private final BooleanSetting tamedOnly;

    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }
        if (mc.player.isPassenger()) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        AbstractHorse target = nearestHorse();
        if (target == null) {
            return;
        }
        mc.gameMode.interact(mc.player, target, net.minecraft.world.InteractionHand.MAIN_HAND);
        mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
    }

    private AbstractHorse nearestHorse() {
        AbstractHorse best = null;
        double bestDistance = range.get() * range.get();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof AbstractHorse horse) || horse.isVehicle()) {
                continue;
            }
            if (tamedOnly.get() && !horse.isTamed()) {
                continue;
            }
            double distance = mc.player.distanceToSqr(horse);
            if (distance <= bestDistance) {
                bestDistance = distance;
                best = horse;
            }
        }
        return best;
    }
}
