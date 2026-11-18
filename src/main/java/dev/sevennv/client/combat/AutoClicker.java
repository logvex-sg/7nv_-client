package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.TargetUtils;
import dev.sevennv.client.utils.Timer;

import java.util.Random;

/**
 * Holds the attack button for you at a configured rate. Attacks are issued
 * through the vanilla {@code startAttack} path, so they are ordinary swings and
 * are subject to the same cooldown the game applies.
 */
public final class AutoClicker extends Module {
    public AutoClicker() {
        super("Auto Clicker", "Clicks the attack button for you", Category.COMBAT);
        cps = number("CPS", "Clicks per second", 10.0, 1.0, 20.0, 0.5);
        jitter = number("Jitter", "Random variation in CPS", 0.0, 0.0, 5.0, 0.5);
        onlyWhenHolding = bool("Only when holding", "Only click while the attack key is held", false);
        onlyOnTarget = bool("Only on target", "Only click while aiming at a valid target", true);
        targetRange = number("Target range", "Maximum target distance", 3.0, 1.0, 6.0, 0.5);
        weaponOnly = bool("Weapon only", "Only click while holding a weapon", false);
        respectCooldown = bool("Respect cooldown", "Do not attack before the weapon is charged", true);
    }

    private final DoubleSetting cps;
    private final DoubleSetting jitter;
    private final BooleanSetting onlyWhenHolding;
    private final BooleanSetting onlyOnTarget;
    private final DoubleSetting targetRange;
    private final BooleanSetting weaponOnly;
    private final BooleanSetting respectCooldown;

    private final Timer timer = new Timer();
    private final Random random = new Random();
    private long nextDelay = 100;

    @Override
    public void onEnable() {
        schedule();
    }

    @Override
    public void onWorldChange() {
        schedule();
    }

    private void schedule() {
        double base = cps.get();
        double spread = jitter.get();
        double actual = base + (spread > 0 ? (random.nextDouble() * 2 - 1) * spread : 0);
        actual = Math.max(1.0, actual);
        nextDelay = (long) (1000.0 / actual);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }
        if (onlyWhenHolding.get() && !mc.options.keyAttack.isDown()) {
            return;
        }
        if (weaponOnly.get() && dev.sevennv.client.utils.InventoryUtils.weaponScore(mc.player.getMainHandItem()) <= 0) {
            return;
        }
        if (respectCooldown.get() && mc.player.getAttackStrengthScale(0f) < 1.0f) {
            return;
        }
        if (onlyOnTarget.get()) {
            var target = TargetUtils.crosshairTarget();
            if (target == null || mc.player.distanceTo(target) > targetRange.get()) {
                return;
            }
        }
        if (!timer.hasElapsed(nextDelay)) {
            return;
        }
        var target = pickTarget();
        if (target == null) {
            return;
        }
        timer.reset();
        schedule();
        mc.gameMode.attack(mc.player, target);
        mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
    }

    private net.minecraft.world.entity.Entity pickTarget() {
        var crosshair = TargetUtils.crosshairTarget();
        if (crosshair != null) {
            return crosshair;
        }
        return TargetUtils.best(targetRange.get(), TargetUtils.Targets.BOTH, TargetUtils.Sort.DISTANCE);
    }

    @Override
    public String getArraylistSuffix() {
        return String.format(java.util.Locale.ROOT, "%.1f", cps.get());
    }
}
