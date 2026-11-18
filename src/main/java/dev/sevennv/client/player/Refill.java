package dev.sevennv.client.player;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.Timer;

/**
 * Tops up the hotbar stack that is running low from the rest of the inventory.
 * Refill uses the vanilla quick-move click, so the server sees a normal
 * inventory action.
 */
public final class Refill extends Module {
    public Refill() {
        super("Refill", "Refills low hotbar stacks from your inventory", Category.PLAYER);
        threshold = integer("Threshold", "Refill a stack at or below this count", 16, 1, 63);
        delay = integer("Delay", "Milliseconds between refills", 250, 0, 2000);
        onlyBlocks = bool("Only blocks", "Only refill stackable building blocks", false);
    }

    private final IntSetting threshold;
    private final IntSetting delay;
    private final BooleanSetting onlyBlocks;
    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null || !timer.tick(delay.get())) {
            return;
        }
        var inventory = mc.player.getInventory();
        for (int hotbar = 0; hotbar < 9; hotbar++) {
            var stack = inventory.getItem(hotbar);
            if (stack.isEmpty() || !stack.isStackable() || stack.getCount() > threshold.get()) {
                continue;
            }
            if (onlyBlocks.get() && !stack.getItem().toString().contains("block")) {
                continue;
            }
            int source = findSameItem(inventory, stack, hotbar);
            if (source == -1) {
                continue;
            }
            // One SWAP click moves the whole source stack into the hotbar slot.
            InventoryUtils.swapWithHotbar(InventoryUtils.menuSlot(source), hotbar);
            return;
        }
    }

    private int findSameItem(net.minecraft.world.entity.player.Inventory inventory,
                             net.minecraft.world.item.ItemStack target, int exclude) {
        for (int i = 9; i < inventory.getContainerSize(); i++) {
            if (i == exclude) {
                continue;
            }
            var other = inventory.getItem(i);
            if (!other.isEmpty() && other.is(target.getItem())) {
                return i;
            }
        }
        return -1;
    }
}
