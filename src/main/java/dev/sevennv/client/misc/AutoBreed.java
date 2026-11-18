package dev.sevennv.client.misc;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Items;

/**
 * Feeds nearby breedable animals that are ready to mate. Only animals that are
 * in love mode are skipped, and the feed uses the vanilla interaction path.
 */
public final class AutoBreed extends Module {
    public AutoBreed() {
        super("Auto Breed", "Feeds nearby breedable animals", Category.MISC);
        range = number("Range", "Maximum feed distance", 4.0, 1.0, 6.0, 0.5);
        delay = integer("Delay", "Milliseconds between feeds", 300, 50, 2000);
        autoSwitch = bool("Auto switch", "Select wheat automatically", true);
        useCarrots = bool("Use carrots", "Use carrots instead of wheat", false);
    }

    private final dev.sevennv.client.config.DoubleSetting range;
    private final IntSetting delay;
    private final BooleanSetting autoSwitch;
    private final BooleanSetting useCarrots;

    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        net.minecraft.world.item.Item food = useCarrots.get() ? Items.CARROT : Items.WHEAT;
        Animal target = nearestHungry(food);
        if (target == null) {
            return;
        }
        if (autoSwitch.get() && !mc.player.getMainHandItem().is(food)) {
            InventoryUtils.switchTo(food);
        }
        if (!mc.player.getMainHandItem().is(food)) {
            return;
        }
        mc.gameMode.interact(mc.player, target, net.minecraft.world.InteractionHand.MAIN_HAND);
        mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
    }

    private Animal nearestHungry(net.minecraft.world.item.Item food) {
        Animal best = null;
        double bestDistance = range.get() * range.get();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Animal animal) || animal.isBaby()) {
                continue;
            }
            double distance = mc.player.distanceToSqr(animal);
            if (distance <= bestDistance) {
                bestDistance = distance;
                best = animal;
            }
        }
        return best;
    }
}
