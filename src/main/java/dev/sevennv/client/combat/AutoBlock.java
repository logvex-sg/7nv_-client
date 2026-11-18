package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.item.Items;

/**
 * Holds a shield or sword block for you. The item is selected through the
 * normal hotbar path and the use key is held, so the server sees a regular
 * block action.
 */
public final class AutoBlock extends Module {
    public AutoBlock() {
        super("Auto Block", "Blocks with your shield automatically", Category.COMBAT);
        requireTarget = bool("Require target", "Only block while a target is nearby", true);
        targetRange = number("Target range", "Maximum distance to a target", 5.0, 1.0, 12.0, 0.5);
        delay = integer("Delay", "Milliseconds between block attempts", 50, 0, 500);
        onlyWhileAttacked = bool("Only when attacked", "Only block right after taking damage", false);
    }

    private final BooleanSetting requireTarget;
    private final dev.sevennv.client.config.DoubleSetting targetRange;
    private final IntSetting delay;
    private final BooleanSetting onlyWhileAttacked;
    private final Timer timer = new Timer();

    private boolean blocking;

    @Override
    public void onDisable() {
        stop();
    }

    @Override
    public void onWorldChange() {
        stop();
    }

    private void stop() {
        if (blocking && mc.options != null) {
            mc.options.keyUse.setDown(false);
            blocking = false;
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }
        boolean shieldHeld = mc.player.getOffhandItem().is(Items.SHIELD)
                || mc.player.getMainHandItem().is(Items.SHIELD);
        if (!shieldHeld) {
            stop();
            return;
        }
        if (onlyWhileAttacked.get() && mc.player.hurtTime <= 0) {
            stop();
            return;
        }
        if (requireTarget.get()) {
            var target = dev.sevennv.client.utils.TargetUtils.best(targetRange.get(),
                    dev.sevennv.client.utils.TargetUtils.Targets.BOTH,
                    dev.sevennv.client.utils.TargetUtils.Sort.DISTANCE);
            if (target == null) {
                stop();
                return;
            }
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        mc.options.keyUse.setDown(true);
        blocking = true;
    }
}
