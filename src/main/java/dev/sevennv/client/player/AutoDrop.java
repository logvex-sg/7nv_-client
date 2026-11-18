package dev.sevennv.client.player;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Drops junk from your inventory: common blocks, seeds and rotten flesh. The
 * drop uses the vanilla THROW click, so the server sees an ordinary drop.
 */
public final class AutoDrop extends Module {
    public AutoDrop() {
        super("Auto Drop", "Drops junk items from your inventory", Category.PLAYER);
        dropCobble = bool("Cobblestone", "Drop cobblestone and deepslate", true);
        dropDirt = bool("Dirt", "Drop dirt, gravel and sand", true);
        dropSeeds = bool("Seeds", "Drop seeds and wheat", true);
        dropRottenFlesh = bool("Rotten flesh", "Drop rotten flesh", true);
        delay = integer("Delay", "Milliseconds between drops", 150, 20, 2000);
        wholeStack = bool("Whole stack", "Drop the whole stack at once", true);
    }

    private final BooleanSetting dropCobble;
    private final BooleanSetting dropDirt;
    private final BooleanSetting dropSeeds;
    private final BooleanSetting dropRottenFlesh;
    private final IntSetting delay;
    private final BooleanSetting wholeStack;
    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null || !timer.tick(delay.get())) {
            return;
        }
        var inventory = mc.player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty() || !junk(stack)) {
                continue;
            }
            int menuSlot = InventoryUtils.menuSlot(i);
            // Button 1 throws the whole stack, button 0 throws a single item.
            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, menuSlot,
                    wholeStack.get() ? 1 : 0, ClickType.THROW, mc.player);
            return;
        }
    }

    private boolean junk(ItemStack stack) {
        var item = stack.getItem();
        if (dropCobble.get() && (item == Items.COBBLESTONE || item == Items.DEEPSLATE
                || item == Items.COBBLED_DEEPSLATE || item == Items.ANDESITE)) {
            return true;
        }
        if (dropDirt.get() && (item == Items.DIRT || item == Items.GRAVEL || item == Items.SAND
                || item == Items.RED_SAND)) {
            return true;
        }
        if (dropSeeds.get() && (item == Items.WHEAT_SEEDS || item == Items.WHEAT
                || item == Items.BEETROOT_SEEDS)) {
            return true;
        }
        return dropRottenFlesh.get() && item == Items.ROTTEN_FLESH;
    }
}
