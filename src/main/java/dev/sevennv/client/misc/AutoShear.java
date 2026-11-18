package dev.sevennv.client.misc;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.Items;

/**
 * Shears nearby sheep that are ready to be sheared. Only sheep that are loaded
 * and within reach are touched, and the interaction goes through the vanilla
 * interaction path.
 */
public final class AutoShear extends Module {
    public AutoShear() {
        super("Auto Shear", "Shears nearby sheep automatically", Category.MISC);
        range = number("Range", "Maximum shear distance", 4.0, 1.0, 6.0, 0.5);
        delay = integer("Delay", "Milliseconds between shears", 250, 50, 2000);
        autoSwitch = bool("Auto switch", "Select shears automatically", true);
    }

    private final dev.sevennv.client.config.DoubleSetting range;
    private final IntSetting delay;
    private final BooleanSetting autoSwitch;

    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        Sheep target = nearestReadySheep();
        if (target == null) {
            return;
        }
        if (autoSwitch.get() && !mc.player.getMainHandItem().is(Items.SHEARS)) {
            InventoryUtils.switchTo(Items.SHEARS);
        }
        if (!mc.player.getMainHandItem().is(Items.SHEARS)) {
            return;
        }
        mc.gameMode.interact(mc.player, target, net.minecraft.world.InteractionHand.MAIN_HAND);
        mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
    }

    private Sheep nearestReadySheep() {
        Sheep best = null;
        double bestDistance = range.get() * range.get();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Sheep sheep) || !sheep.readyForShearing()) {
                continue;
            }
            double distance = mc.player.distanceToSqr(sheep);
            if (distance <= bestDistance) {
                bestDistance = distance;
                best = sheep;
            }
        }
        return best;
    }
}
