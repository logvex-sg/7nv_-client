package dev.logvex.client.player;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.IntSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.utils.InventoryUtils;
import dev.logvex.client.utils.Timer;
import net.minecraft.world.item.ItemStack;

/**
 * Keeps the hotbar tidy: refills stacks that are running low and drops junk
 * items. Item movement uses the ordinary container click path, so the server
 * sees a normal inventory interaction.
 */
public final class InventoryManager extends Module {
    public InventoryManager() {
        super("Inventory Manager", "Refills the hotbar and drops junk", Category.PLAYER);
        refillHotbar = bool("Refill hotbar", "Top up hotbar stacks from the inventory", true);
        minStackSize = integer("Min stack", "Refill a hotbar stack below this size", 8, 1, 64);
        dropJunk = bool("Drop junk", "Drop blocks and items on the junk list", false);
        delay = integer("Delay", "Milliseconds between actions", 200, 50, 2000);
    }

    private final BooleanSetting refillHotbar;
    private final IntSetting minStackSize;
    private final BooleanSetting dropJunk;
    private final IntSetting delay;

    private final Timer timer = new Timer();

    private static final String[] JUNK = {"dirt", "gravel", "flint", "rotten_flesh", "poisonous_potato",
            "cobblestone", "netherrack", "andesite", "diorite", "granite", "tuff"};

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null || !timer.tick(delay.get())) {
            return;
        }
        if (refillHotbar.get()) {
            refill();
        }
        if (dropJunk.get()) {
            drop();
        }
    }

    private void refill() {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack hotbar = mc.player.getInventory().getItem(slot);
            if (hotbar.isEmpty() || !hotbar.isStackable() || hotbar.getCount() >= minStackSize.get()) {
                continue;
            }
            int source = findSameStack(hotbar, 9, 36);
            if (source == -1) {
                continue;
            }
            InventoryUtils.pickup(InventoryUtils.menuSlot(source), 0);
            InventoryUtils.pickup(InventoryUtils.menuSlot(slot), 0);
            InventoryUtils.pickup(InventoryUtils.menuSlot(source), 0);
            return;
        }
    }

    private int findSameStack(ItemStack target, int from, int to) {
        for (int slot = from; slot < to; slot++) {
            ItemStack candidate = mc.player.getInventory().getItem(slot);
            if (!candidate.isEmpty() && ItemStack.isSameItemSameComponents(candidate, target)) {
                return slot;
            }
        }
        return -1;
    }

    private void drop() {
        for (int slot = 9; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            String name = stack.getItem().toString().toLowerCase(java.util.Locale.ROOT);
            for (String junk : JUNK) {
                if (name.contains(junk)) {
                    InventoryUtils.quickMove(InventoryUtils.menuSlot(slot));
                    return;
                }
            }
        }
    }
}
