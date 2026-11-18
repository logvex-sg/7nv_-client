package dev.sevennv.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Adds and removes transient attribute modifiers on the local player. Modifiers
 * are keyed by name so re-applying the same one never stacks, and every removal
 * is a no-op when the player is gone.
 */
public final class AttributeUtils {
    private AttributeUtils() {
    }

    public static void apply(Holder<Attribute> attribute, String key, double amount) {
        AttributeInstance instance = instance(attribute);
        if (instance == null) {
            return;
        }
        Identifier id = id(key);
        instance.removeModifier(id);
        if (amount != 0.0) {
            instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void applyMultiplied(Holder<Attribute> attribute, String key, double factor) {
        AttributeInstance instance = instance(attribute);
        if (instance == null) {
            return;
        }
        Identifier id = id(key);
        instance.removeModifier(id);
        if (factor != 1.0) {
            instance.addTransientModifier(new AttributeModifier(id, factor - 1.0,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    public static void clear(Holder<Attribute> attribute, String key) {
        AttributeInstance instance = instance(attribute);
        if (instance != null) {
            instance.removeModifier(id(key));
        }
    }

    public static double value(Holder<Attribute> attribute) {
        AttributeInstance instance = instance(attribute);
        return instance == null ? 0.0 : instance.getValue();
    }

    public static double base(Holder<Attribute> attribute) {
        AttributeInstance instance = instance(attribute);
        return instance == null ? 0.0 : instance.getBaseValue();
    }

    private static AttributeInstance instance(Holder<Attribute> attribute) {
        var player = Minecraft.getInstance().player;
        return player == null ? null : player.getAttribute(attribute);
    }

    private static Identifier id(String key) {
        return Identifier.fromNamespaceAndPath("sevennv", key);
    }
}
