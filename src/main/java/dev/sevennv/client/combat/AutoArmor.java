package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.NotificationManager;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Equips the best armour found in the inventory.
 *
 * <p>Each armour slot is compared against every candidate stack in the
 * inventory; the best one is swapped in with a pickup/place click pair.
 */
public final class AutoArmor extends Module {
    public AutoArmor() {
        super("Auto Armor", "Equips the best armour from the inventory", Category.COMBAT);
        delay = integer("Delay", "Milliseconds between swaps", 200, 0, 2000);
        onlyWhenDamaged = bool("Only when damaged", "Skip slots that still have durability left", true);
        notify = bool("Notify", "Show a notification on swap", false);
    }

    private final IntSetting delay;
    private final BooleanSetting onlyWhenDamaged;
    private final BooleanSetting notify;

    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        for (EquipmentSlot slot : InventoryUtils.armorSlots()) {
            if (equipBest(slot)) {
                return;
            }
        }
    }

    private boolean equipBest(EquipmentSlot slot) {
        ItemStack current = mc.player.getItemBySlot(slot);
        double currentScore = InventoryUtils.armorScore(current);
        if (onlyWhenDamaged.get() && !current.isEmpty() && dev.sevennv.client.utils.ItemUtils.durabilityPercent(current) > 0.15) {
            return false;
        }
        int bestIndex = -1;
        double bestScore = currentScore;
        var inventory = mc.player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack candidate = inventory.getItem(i);
            if (candidate.isEmpty() || !isArmorFor(candidate, slot)) {
                continue;
            }
            double score = InventoryUtils.armorScore(candidate);
            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }
        if (bestIndex == -1) {
            return false;
        }
        int armorSlot = InventoryUtils.armorMenuSlot(slot);
        if (armorSlot == -1) {
            return false;
        }
        // Pick the candidate up, place it in the armour slot, then drop the
        // replaced item back where the candidate came from.
        int source = InventoryUtils.menuSlot(bestIndex);
        InventoryUtils.pickup(source, 0);
        InventoryUtils.pickup(armorSlot, 0);
        if (!current.isEmpty()) {
            InventoryUtils.pickup(source, 0);
        }
        if (notify.get()) {
            NotificationManager.info("Auto Armor", "Equipped " + candidateName(bestIndex) + " (" + slot.getName() + ")");
        }
        return true;
    }

    private String candidateName(int index) {
        ItemStack stack = mc.player.getInventory().getItem(index);
        return stack.getHoverName().getString();
    }

    /** True when the stack is equippable in the given armour slot. */
    private static boolean isArmorFor(ItemStack stack, EquipmentSlot slot) {
        var equippable = stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.slot() == slot) {
            return true;
        }
        // Fall back to the attribute-based heuristic for items without the
        // equippable component (older datapacks, modded armour).
        return InventoryUtils.attributeOf(stack, net.minecraft.world.entity.ai.attributes.Attributes.ARMOR) > 0.0;
    }
}
