package dev.sevennv.client.player;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Equips the best available armour in each slot. Every candidate is scored with
 * the same protection weighting the rest of the client uses, and the swap is an
 * ordinary inventory click.
 */
public final class AutoArmorSwitch extends Module {
    public AutoArmorSwitch() {
        super("Auto Armor Switch", "Equips the best armour you are carrying", Category.PLAYER);
        delay = integer("Delay", "Milliseconds between swaps", 250, 0, 2000);
        replaceOnly = bool("Only upgrade", "Never swap to worse armour", true);
        includeInventory = bool("Include inventory", "Search the whole inventory", true);
    }

    private final IntSetting delay;
    private final BooleanSetting replaceOnly;
    private final BooleanSetting includeInventory;
    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null || !timer.tick(delay.get())) {
            return;
        }
        for (EquipmentSlot slot : InventoryUtils.armorSlots()) {
            int bestSlot = -1;
            double bestScore = currentScore(slot);
            var inventory = mc.player.getInventory();
            int limit = includeInventory.get() ? inventory.getContainerSize() : 9;
            for (int i = 0; i < limit; i++) {
                var stack = inventory.getItem(i);
                if (stack.isEmpty()) {
                    continue;
                }
                if (!isArmorFor(stack, slot)) {
                    continue;
                }
                double score = InventoryUtils.armorScore(stack);
                if (score > bestScore) {
                    bestScore = score;
                    bestSlot = i;
                }
            }
            if (bestSlot == -1) {
                continue;
            }
            if (replaceOnly.get() && bestScore <= currentScore(slot)) {
                continue;
            }
            int menuSlot = InventoryUtils.menuSlot(bestSlot);
            int armorSlot = InventoryUtils.armorMenuSlot(slot);
            // Deterministic three-click swap: pick up the new piece, swap it into
            // the armour slot, then drop the replaced piece back into the source.
            InventoryUtils.pickup(menuSlot, 0);
            InventoryUtils.pickup(armorSlot, 0);
            InventoryUtils.pickup(menuSlot, 0);
            return;
        }
    }

    private double currentScore(EquipmentSlot slot) {
        return InventoryUtils.armorScore(mc.player.getItemBySlot(slot));
    }

    private boolean isArmorFor(net.minecraft.world.item.ItemStack stack, EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> stack.is(net.minecraft.world.item.Items.LEATHER_HELMET)
                    || stack.is(net.minecraft.world.item.Items.CHAINMAIL_HELMET)
                    || stack.is(net.minecraft.world.item.Items.IRON_HELMET)
                    || stack.is(net.minecraft.world.item.Items.GOLDEN_HELMET)
                    || stack.is(net.minecraft.world.item.Items.DIAMOND_HELMET)
                    || stack.is(net.minecraft.world.item.Items.NETHERITE_HELMET);
            case CHEST -> stack.is(net.minecraft.world.item.Items.LEATHER_CHESTPLATE)
                    || stack.is(net.minecraft.world.item.Items.CHAINMAIL_CHESTPLATE)
                    || stack.is(net.minecraft.world.item.Items.IRON_CHESTPLATE)
                    || stack.is(net.minecraft.world.item.Items.GOLDEN_CHESTPLATE)
                    || stack.is(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE)
                    || stack.is(net.minecraft.world.item.Items.NETHERITE_CHESTPLATE);
            case LEGS -> stack.is(net.minecraft.world.item.Items.LEATHER_LEGGINGS)
                    || stack.is(net.minecraft.world.item.Items.CHAINMAIL_LEGGINGS)
                    || stack.is(net.minecraft.world.item.Items.IRON_LEGGINGS)
                    || stack.is(net.minecraft.world.item.Items.GOLDEN_LEGGINGS)
                    || stack.is(net.minecraft.world.item.Items.DIAMOND_LEGGINGS)
                    || stack.is(net.minecraft.world.item.Items.NETHERITE_LEGGINGS);
            case FEET -> stack.is(net.minecraft.world.item.Items.LEATHER_BOOTS)
                    || stack.is(net.minecraft.world.item.Items.CHAINMAIL_BOOTS)
                    || stack.is(net.minecraft.world.item.Items.IRON_BOOTS)
                    || stack.is(net.minecraft.world.item.Items.GOLDEN_BOOTS)
                    || stack.is(net.minecraft.world.item.Items.DIAMOND_BOOTS)
                    || stack.is(net.minecraft.world.item.Items.NETHERITE_BOOTS);
            default -> false;
        };
    }
}
