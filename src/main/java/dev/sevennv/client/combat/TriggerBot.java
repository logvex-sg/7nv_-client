package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.TargetUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Attacks whatever the crosshair is pointing at. Unlike Kill Aura this never
 * searches for a target: it only swings at the entity already under the cursor,
 * so it behaves like a held-down mouse button.
 */
public final class TriggerBot extends Module {
    public TriggerBot() {
        super("Trigger Bot", "Attacks the entity under your crosshair", Category.COMBAT);
        range = number("Range", "Maximum attack distance", 3.0, 2.0, 6.0, 0.1);
        delay = integer("Delay", "Milliseconds between swings", 100, 0, 1000);
        respectCooldown = bool("Respect cooldown", "Wait for the weapon to charge", true);
        weaponOnly = bool("Weapon only", "Only attack while holding a weapon", true);
        swing = bool("Swing", "Swing the arm on each hit", true);
    }

    private final DoubleSetting range;
    private final IntSetting delay;
    private final BooleanSetting respectCooldown;
    private final BooleanSetting weaponOnly;
    private final BooleanSetting swing;

    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }
        LivingEntity target = TargetUtils.crosshairTarget();
        if (target == null || mc.player.distanceTo(target) > range.get()) {
            return;
        }
        if (weaponOnly.get() && dev.sevennv.client.utils.InventoryUtils.weaponScore(mc.player.getMainHandItem()) <= 0.0) {
            return;
        }
        if (respectCooldown.get() && mc.player.getAttackStrengthScale(0f) < 1.0f) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        mc.gameMode.attack(mc.player, target);
        if (swing.get()) {
            mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        }
    }
}
