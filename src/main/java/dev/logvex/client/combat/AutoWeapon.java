package dev.logvex.client.combat;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.DoubleSetting;
import dev.logvex.client.config.EnumSetting;
import dev.logvex.client.config.IntSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.utils.InventoryUtils;
import dev.logvex.client.utils.TargetUtils;
import dev.logvex.client.utils.Timer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Switches to the best weapon in the hotbar when a target is in range.
 * The original slot is restored once the target leaves.
 */
public final class AutoWeapon extends Module {
    public AutoWeapon() {
        super("Auto Weapon", "Switches to the best weapon when a target is near", Category.COMBAT);
        range = number("Range", "Target detection range", 5.0, 1.0, 10.0, 0.5);
        targetMode = option("Targets", "Which entities trigger the switch", TargetUtils.Targets.PLAYERS);
        preferSword = bool("Prefer sword", "Bias towards swords", true);
        preferAxe = bool("Prefer axe", "Bias towards axes", false);
        delay = integer("Delay", "Milliseconds between switches", 150, 0, 1000);
        restore = bool("Restore slot", "Switch back when no target is near", true);
    }

    private final DoubleSetting range;
    private final EnumSetting<TargetUtils.Targets> targetMode;
    private final BooleanSetting preferSword;
    private final BooleanSetting preferAxe;
    private final IntSetting delay;
    private final BooleanSetting restore;

    private final Timer timer = new Timer();
    private int originalSlot = -1;

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @Override
    public void onWorldChange() {
        originalSlot = -1;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) {
            return;
        }
        LivingEntity target = TargetUtils.best(range.get(), targetMode.get(), TargetUtils.Sort.DISTANCE);
        if (target == null) {
            if (restore.get()) {
                restoreSlot();
            }
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        int best = InventoryUtils.bestHotbarWeapon(preferSword.get(), preferAxe.get());
        if (best == -1 || best == mc.player.getInventory().getSelectedSlot()) {
            return;
        }
        if (originalSlot == -1) {
            originalSlot = mc.player.getInventory().getSelectedSlot();
        }
        InventoryUtils.selectSlot(best);
    }

    private void restoreSlot() {
        if (originalSlot != -1 && mc.player != null) {
            InventoryUtils.selectSlot(originalSlot);
            originalSlot = -1;
        }
    }
}
