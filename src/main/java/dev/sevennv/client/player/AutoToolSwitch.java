package dev.sevennv.client.player;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.TargetUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.item.Items;

/**
 * Holds a shield when an enemy is close and returns to the previous slot when
 * the threat is gone. The swap is an ordinary hotbar selection.
 */
public final class AutoToolSwitch extends Module {
    public AutoToolSwitch() {
        super("Auto Tool Switch", "Holds a shield near enemies, restores afterwards", Category.PLAYER);
        range = number("Range", "Threat detection radius", 6.0, 1.0, 16.0, 0.5);
        delay = integer("Delay", "Milliseconds between switches", 250, 0, 2000);
        restore = bool("Restore slot", "Return to your previous slot", true);
    }

    private final DoubleSetting range;
    private final IntSetting delay;
    private final BooleanSetting restore;
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
        var target = TargetUtils.best(range.get(), TargetUtils.Targets.PLAYERS, TargetUtils.Sort.DISTANCE);
        int shield = InventoryUtils.findHotbarSlot(Items.SHIELD);
        if (target != null && shield != -1 && mc.player.getInventory().getSelectedSlot() != shield) {
            remember();
            InventoryUtils.selectSlot(shield);
        } else if (target == null) {
            restoreSlot();
        }
    }

    private void remember() {
        if (previousSlot == -1 && mc.player != null) {
            previousSlot = mc.player.getInventory().getSelectedSlot();
        }
    }

    private void restoreSlot() {
        if (restore.get() && previousSlot != -1 && mc.player != null) {
            InventoryUtils.selectSlot(previousSlot);
        }
        previousSlot = -1;
    }
}
