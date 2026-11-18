package dev.sevennv.client.player;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.item.Items;

/**
 * Automatically eats when hungry. Food is selected from the hotbar or swapped
 * in from the inventory, and the item is used with the normal right-click path.
 */
public final class AutoEat extends Module {
    public AutoEat() {
        super("Auto Eat", "Eats food when you are hungry", Category.PLAYER);
        hungerThreshold = integer("Hunger", "Eat below this food level", 18, 1, 20);
        healthThreshold = integer("Health", "Also eat below this health", 0, 0, 20);
        delay = integer("Delay", "Milliseconds between bites", 250, 50, 2000);
        preferGolden = bool("Prefer golden", "Prefer golden apples over normal food", false);
        swapFromInventory = bool("Swap from inventory", "Search the whole inventory for food", true);
    }

    private final IntSetting hungerThreshold;
    private final IntSetting healthThreshold;
    private final IntSetting delay;
    private final BooleanSetting preferGolden;
    private final BooleanSetting swapFromInventory;

    private final Timer timer = new Timer();
    private int previousSlot = -1;

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @Override
    public void onWorldChange() {
        previousSlot = -1;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }
        boolean hungry = mc.player.getFoodData().getFoodLevel() <= hungerThreshold.get();
        boolean hurt = healthThreshold.get() > 0 && mc.player.getHealth() <= healthThreshold.get();
        if (!hungry && !hurt) {
            restoreSlot();
            return;
        }
        if (mc.player.isUsingItem()) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        if (!ensureFood()) {
            return;
        }
        mc.gameMode.useItem(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND);
    }

    private boolean ensureFood() {
        if (isFood(mc.player.getMainHandItem())) {
            return true;
        }
        if (previousSlot == -1) {
            previousSlot = mc.player.getInventory().getSelectedSlot();
        }
        if (preferGolden.get() && trySelect(Items.GOLDEN_APPLE)) {
            return true;
        }
        if (preferGolden.get() && trySelect(Items.ENCHANTED_GOLDEN_APPLE)) {
            return true;
        }
        for (int slot = 0; slot < 9; slot++) {
            if (isFood(mc.player.getInventory().getItem(slot))) {
                InventoryUtils.selectSlot(slot);
                return true;
            }
        }
        if (!swapFromInventory.get()) {
            return false;
        }
        for (int slot = 9; slot < 36; slot++) {
            var stack = mc.player.getInventory().getItem(slot);
            if (isFood(stack)) {
                return InventoryUtils.switchTo(stack.getItem());
            }
        }
        return false;
    }

    private boolean trySelect(net.minecraft.world.item.Item item) {
        int slot = InventoryUtils.findHotbarSlot(item);
        if (slot != -1) {
            InventoryUtils.selectSlot(slot);
            return true;
        }
        return false;
    }

    private boolean isFood(net.minecraft.world.item.ItemStack stack) {
        return stack.get(net.minecraft.core.component.DataComponents.FOOD) != null;
    }

    private void restoreSlot() {
        if (previousSlot != -1 && mc.player != null) {
            InventoryUtils.selectSlot(previousSlot);
            previousSlot = -1;
        }
    }
}
