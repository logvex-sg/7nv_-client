package dev.sevennv.client.player;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.ItemUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.item.Items;

/**
 * Throws experience bottles while standing still so mending gear repairs. It
 * only runs when a piece of worn gear is actually damaged and a bottle is in
 * the hotbar, and it never moves the player.
 */
public final class AutoExp extends Module {
    public AutoExp() {
        super("Auto Exp", "Throws XP bottles to repair mending gear", Category.PLAYER);
        delay = integer("Delay", "Milliseconds between throws", 250, 50, 2000);
        threshold = number("Repair below", "Repair gear below this durability percent", 90.0, 5.0, 100.0, 5.0);
        standStill = bool("Stand still", "Only throw while not moving", true);
    }

    private final IntSetting delay;
    private final DoubleSetting threshold;
    private final BooleanSetting standStill;

    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }
        if (standStill.get() && (Math.abs(mc.player.zza) > 0.01f || Math.abs(mc.player.xxa) > 0.01f)) {
            return;
        }
        if (!needsRepair()) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        if (!mc.player.getMainHandItem().is(Items.EXPERIENCE_BOTTLE)) {
            InventoryUtils.switchTo(Items.EXPERIENCE_BOTTLE);
        }
        if (!mc.player.getMainHandItem().is(Items.EXPERIENCE_BOTTLE)) {
            return;
        }
        mc.gameMode.useItem(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND);
        mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
    }

    private boolean needsRepair() {
        if (!InventoryUtils.hasInHotbar(Items.EXPERIENCE_BOTTLE)) {
            return false;
        }
        for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[]{
                net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET}) {
            var stack = mc.player.getItemBySlot(slot);
            if (stack.isDamageableItem() && ItemUtils.level(stack,
                    net.minecraft.world.item.enchantment.Enchantments.MENDING) > 0
                    && ItemUtils.durabilityPercent(stack) * 100f < threshold.get()) {
                return true;
            }
        }
        return false;
    }
}
