package dev.sevennv.client.player;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

/**
 * Uses the held item again as soon as the previous use finishes, so eating and
 * drinking are continuous. The re-use goes through the ordinary item-use path.
 */
public final class FastUse extends Module {
    public FastUse() {
        super("Fast Use", "Re-uses the held item as soon as it finishes", Category.PLAYER);
        foodOnly = bool("Food only", "Only act on food, potions and milk", true);
        delay = integer("Delay", "Milliseconds between uses", 50, 0, 500);
    }

    private final BooleanSetting foodOnly;
    private final IntSetting delay;
    private final Timer timer = new Timer();
    private boolean wasUsing;

    @Override
    public void onWorldChange() {
        wasUsing = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }
        boolean using = mc.player.isUsingItem();
        boolean finished = wasUsing && !using;
        wasUsing = using;
        if (!finished || !timer.tick(delay.get()) || !mc.options.keyUse.isDown()) {
            return;
        }
        var stack = mc.player.getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }
        if (foodOnly.get() && !(stack.has(net.minecraft.core.component.DataComponents.FOOD)
                || stack.is(Items.POTION) || stack.is(Items.MILK_BUCKET))) {
            return;
        }
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
    }
}
