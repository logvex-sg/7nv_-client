package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Holds the forward key. Movement still flows through the vanilla input path,
 * so the server sees ordinary walking.
 */
public final class AutoWalk extends Module {
    public AutoWalk() {
        super("Auto Walk", "Walks forward automatically", Category.MOVEMENT);
        stopInLiquid = bool("Stop in liquid", "Pause while in water or lava", true);
        stopOnHunger = bool("Stop when hungry", "Pause when food is low", false);
    }

    private final BooleanSetting stopInLiquid;
    private final BooleanSetting stopOnHunger;

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) {
            return;
        }
        boolean blocked = (stopInLiquid.get() && (mc.player.isInWater() || mc.player.isInLava()))
                || (stopOnHunger.get() && mc.player.getFoodData().getFoodLevel() <= 6);
        mc.options.keyUp.setDown(!blocked);
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.keyUp.setDown(false);
        }
    }

    @Override
    public void onWorldChange() {
        onDisable();
    }
}
