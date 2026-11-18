package dev.sevennv.client.movement;

import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.AttributeUtils;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Counteracts the levitation effect by applying a downward gravity modifier
 * while it is active. Only the local player's attribute is touched.
 */
public final class NoLevitation extends Module {
    public NoLevitation() {
        super("No Levitation", "Cancels the levitation effect", Category.MOVEMENT);
        strength = number("Strength", "Downward gravity added while levitating", 0.08, 0.02, 0.3, 0.02);
    }

    private final DoubleSetting strength;
    private static final String KEY = "no_levitation";

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        if (mc.player.hasEffect(net.minecraft.world.effect.MobEffects.LEVITATION)) {
            AttributeUtils.apply(Attributes.GRAVITY, KEY, -strength.get());
        } else {
            AttributeUtils.clear(Attributes.GRAVITY, KEY);
        }
    }

    @Override
    public void onDisable() {
        AttributeUtils.clear(Attributes.GRAVITY, KEY);
    }

    @Override
    public void onWorldChange() {
        AttributeUtils.clear(Attributes.GRAVITY, KEY);
    }
}
