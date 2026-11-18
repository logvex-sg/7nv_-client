package dev.sevennv.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Applies a transient interaction-range bonus to the local player and reports
 * the effective reach. The bonus is an ordinary attribute modifier, so it is
 * removed cleanly when the module is disabled or the player leaves the world.
 */
public final class ReachTracker {
    private static final Identifier BLOCK_ID = Identifier.fromNamespaceAndPath("sevennv", "reach_block");
    private static final Identifier ENTITY_ID = Identifier.fromNamespaceAndPath("sevennv", "reach_entity");

    private ReachTracker() {
    }

    public static void apply(double blockBonus, double entityBonus) {
        update(Attributes.BLOCK_INTERACTION_RANGE, BLOCK_ID, blockBonus);
        update(Attributes.ENTITY_INTERACTION_RANGE, ENTITY_ID, entityBonus);
    }

    public static void clear() {
        AttributeInstance block = instance(Attributes.BLOCK_INTERACTION_RANGE);
        AttributeInstance entity = instance(Attributes.ENTITY_INTERACTION_RANGE);
        if (block != null) {
            block.removeModifier(BLOCK_ID);
        }
        if (entity != null) {
            entity.removeModifier(ENTITY_ID);
        }
    }

    public static double blockReach() {
        AttributeInstance instance = instance(Attributes.BLOCK_INTERACTION_RANGE);
        return instance == null ? 0.0 : instance.getValue();
    }

    public static double entityReach() {
        AttributeInstance instance = instance(Attributes.ENTITY_INTERACTION_RANGE);
        return instance == null ? 0.0 : instance.getValue();
    }

    private static void update(Holder<Attribute> holder, Identifier id, double amount) {
        AttributeInstance instance = instance(holder);
        if (instance == null) {
            return;
        }
        instance.removeModifier(id);
        if (amount > 0.0) {
            instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static AttributeInstance instance(Holder<Attribute> holder) {
        var player = Minecraft.getInstance().player;
        return player == null ? null : player.getAttribute(holder);
    }
}
