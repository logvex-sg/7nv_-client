package dev.sevennv.client.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Rough vanilla damage model, used by the damage calculator and combat
 * notifications. Deliberately conservative: it reports a base sword/axe hit
 * plus a simplified protection reduction, not a full simulation.
 */
public final class DamageUtils {
    private DamageUtils() {
    }

    /**
     * Approximate damage of one fully charged melee hit with the given weapon.
     * The weapon's own attack-damage modifier is the primary term; the
     * attacker's attribute is only used to fill in the base value.
     */
    public static float estimateMelee(LivingEntity attacker, LivingEntity target, ItemStack weapon) {
        float base = 1.0f;
        if (!weapon.isEmpty()) {
            ItemAttributeModifiers modifiers =
                    weapon.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
            base = (float) modifiers.compute(Attributes.ATTACK_DAMAGE, base, EquipmentSlot.MAINHAND);
        }
        if (attacker != null) {
            base += (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) - 1.0f;
        }
        float sharpness = ItemUtils.sharpnessLevel(weapon) * 0.5f;
        return reduceByArmor(base + sharpness, target);
    }

    /** Applies a simple armour + protection reduction. */
    public static float reduceByArmor(float damage, LivingEntity target) {
        if (target == null) {
            return damage;
        }
        int armor = target.getArmorValue();
        float armorReduction = Math.min(20, armor) / 25f;
        int protection = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            protection += ItemUtils.protectionLevel(target.getItemBySlot(slot));
        }
        float protectionReduction = Math.min(20, protection) * 0.04f;
        float remaining = damage * (1f - Math.min(0.8f, armorReduction)) * (1f - Math.min(0.8f, protectionReduction));
        return Math.max(0f, remaining);
    }

    /** Number of hits needed to kill, rounded up. */
    public static int hitsToKill(float damagePerHit, float targetHealth) {
        if (damagePerHit <= 0f) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.ceil(targetHealth / damagePerHit);
    }
}
