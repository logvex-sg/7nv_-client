package dev.sevennv.client.utils;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/** Item inspection helpers used by the combat and HUD modules. */
public final class ItemUtils {
    private ItemUtils() {
    }

    /**
     * Enchantment level on a stack, looked up by registry key.
     *
     * <p>The stored {@code ItemEnchantments} keys are {@code Holder}s, and the
     * {@code Enchantments} constants are {@code ResourceKey}s, so the two are
     * matched through the holder's unwrapped key rather than a registry lookup.
     */
    public static int level(ItemStack stack, ResourceKey<Enchantment> enchantment) {
        if (stack.isEmpty()) {
            return 0;
        }
        for (var entry : stack.getEnchantments().entrySet()) {
            var key = entry.getKey().unwrapKey();
            if (key.isPresent() && key.get().equals(enchantment)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }

    public static int protectionLevel(ItemStack stack) {
        return level(stack, Enchantments.PROTECTION);
    }

    public static int blastProtectionLevel(ItemStack stack) {
        return level(stack, Enchantments.BLAST_PROTECTION);
    }

    public static int unbreakingLevel(ItemStack stack) {
        return level(stack, Enchantments.UNBREAKING);
    }

    public static int sharpnessLevel(ItemStack stack) {
        return level(stack, Enchantments.SHARPNESS);
    }

    public static int remainingDurability(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageableItem()) {
            return -1;
        }
        return stack.getMaxDamage() - stack.getDamageValue();
    }

    public static float durabilityPercent(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageableItem() || stack.getMaxDamage() <= 0) {
            return 1f;
        }
        return (stack.getMaxDamage() - stack.getDamageValue()) / (float) stack.getMaxDamage();
    }
}
