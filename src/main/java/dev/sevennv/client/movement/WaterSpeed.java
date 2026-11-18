package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.AttributeUtils;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Raises the water-movement efficiency attribute so swimming is faster. The
 * modifier is transient and is cleared on disable and world change.
 */
public final class WaterSpeed extends Module {
    private static final String KEY = "sevennv_water_speed";

    public WaterSpeed() {
        super("Water Speed", "Speeds up swimming", Category.MOVEMENT);
        amount = number("Amount", "Extra water movement efficiency", 1.0, 0.1, 3.0, 0.1);
        onlyInWater = bool("Only in water", "Apply only while in water", true);
    }

    private final dev.sevennv.client.config.DoubleSetting amount;
    private final BooleanSetting onlyInWater;

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        if (onlyInWater.get() && !mc.player.isInWater()) {
            AttributeUtils.clear(Attributes.WATER_MOVEMENT_EFFICIENCY, KEY);
            return;
        }
        AttributeUtils.apply(Attributes.WATER_MOVEMENT_EFFICIENCY, KEY, amount.get());
    }

    @Override
    public void onDisable() {
        AttributeUtils.clear(Attributes.WATER_MOVEMENT_EFFICIENCY, KEY);
    }

    @Override
    public void onWorldChange() {
        AttributeUtils.clear(Attributes.WATER_MOVEMENT_EFFICIENCY, KEY);
    }
}
