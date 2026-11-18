package dev.sevennv.client.player;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.Items;

/**
 * Drinks a bucket of milk when a harmful effect is active, clearing it. The
 * drink goes through the vanilla use-item path, so the animation and timing are
 * exactly what the server expects.
 */
public final class AutoMilk extends Module {
    public AutoMilk() {
        super("Auto Milk", "Drinks milk to clear bad effects", Category.PLAYER);
        delay = integer("Delay", "Milliseconds between drinks", 500, 100, 3000);
        onlyHarmful = bool("Only harmful", "Only drink for harmful effects", true);
        autoSwitch = bool("Auto switch", "Select milk automatically", true);
    }

    private final IntSetting delay;
    private final BooleanSetting onlyHarmful;
    private final BooleanSetting autoSwitch;

    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }
        if (!hasEffectToClear()) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        if (autoSwitch.get() && !mc.player.getMainHandItem().is(Items.MILK_BUCKET)) {
            InventoryUtils.switchTo(Items.MILK_BUCKET);
        }
        if (!mc.player.getMainHandItem().is(Items.MILK_BUCKET)) {
            return;
        }
        mc.gameMode.useItem(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND);
        mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
    }

    private boolean hasEffectToClear() {
        for (var effect : mc.player.getActiveEffects()) {
            if (!onlyHarmful.get() || effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                return true;
            }
        }
        return false;
    }
}
