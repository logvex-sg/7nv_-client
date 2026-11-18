package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.Timer;

/**
 * Switches to the best weapon when a target is nearby and back to the previous
 * slot when the fight ends. Uses {@code InventoryUtils.bestHotbarWeapon} to
 * score candidates.
 */
public final class AutoWeaponSwitch extends Module {
    public AutoWeaponSwitch() {
        super("Auto Weapon Switch", "Switches to your best weapon near a target", Category.COMBAT);
        range = number("Range", "Maximum target distance", 6.0, 1.0, 16.0, 0.5);
        preferSword = bool("Prefer sword", "Bias towards swords", true);
        preferAxe = bool("Prefer axe", "Bias towards axes", false);
        restore = bool("Restore slot", "Return to the previous slot afterwards", true);
        delay = integer("Delay", "Milliseconds between switches", 200, 0, 2000);
    }

    private final dev.sevennv.client.config.DoubleSetting range;
    private final BooleanSetting preferSword;
    private final BooleanSetting preferAxe;
    private final BooleanSetting restore;
    private final IntSetting delay;
    private final Timer timer = new Timer();

    private int previousSlot = -1;

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @Override
    public void onWorldChange() {
        previousSlot = -1;
    }

    @Override
    public void onTick() {
        if (mc.player == null || !timer.tick(delay.get())) {
            return;
        }
        var target = dev.sevennv.client.utils.TargetUtils.best(range.get(),
                dev.sevennv.client.utils.TargetUtils.Targets.BOTH,
                dev.sevennv.client.utils.TargetUtils.Sort.DISTANCE);
        if (target == null) {
            restoreSlot();
            return;
        }
        int best = InventoryUtils.bestHotbarWeapon(preferSword.get(), preferAxe.get());
        if (best == -1 || best == mc.player.getInventory().getSelectedSlot()) {
            return;
        }
        if (previousSlot == -1) {
            previousSlot = mc.player.getInventory().getSelectedSlot();
        }
        InventoryUtils.selectSlot(best);
    }

    private void restoreSlot() {
        if (restore.get() && previousSlot != -1 && mc.player != null) {
            InventoryUtils.selectSlot(previousSlot);
        }
        previousSlot = -1;
    }
}
