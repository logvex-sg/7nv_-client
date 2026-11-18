package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Raises the player's step-height attribute, so the vanilla movement code
 * itself steps over blocks. Restores the original base value on disable.
 */
public final class StepHeight extends Module {
    public StepHeight() {
        super("Step", "Increases your step height", Category.MOVEMENT);
        stepHeight = number("Step height", "Base step height in blocks", 1.0, 0.5, 2.5, 0.1);
    }

    private final DoubleSetting stepHeight;

    private double original = Double.NaN;

    @Override
    public void onEnable() {
        apply();
    }

    @Override
    public void onDisable() {
        restore();
    }

    @Override
    public void onWorldChange() {
        original = Double.NaN;
    }

    @Override
    public void onTick() {
        apply();
    }

    private void apply() {
        if (mc.player == null) {
            return;
        }
        AttributeInstance instance = mc.player.getAttribute(Attributes.STEP_HEIGHT);
        if (instance == null) {
            return;
        }
        if (Double.isNaN(original)) {
            original = instance.getBaseValue();
        }
        if (instance.getBaseValue() != stepHeight.get()) {
            instance.setBaseValue(stepHeight.get());
        }
    }

    private void restore() {
        if (mc.player == null || Double.isNaN(original)) {
            return;
        }
        AttributeInstance instance = mc.player.getAttribute(Attributes.STEP_HEIGHT);
        if (instance != null) {
            instance.setBaseValue(original);
        }
        original = Double.NaN;
    }
}
