package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.EnumSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Rotation;
import dev.sevennv.client.utils.TargetUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Comparator;
import java.util.List;

/**
 * Attacks the best nearby target automatically. Attacks are ordinary swings
 * issued through the vanilla game mode, so the server sees a normal hit and the
 * weapon cooldown still applies.
 */
public final class KillAura extends Module {
    public KillAura() {
        super("Kill Aura", "Attacks nearby targets for you", Category.COMBAT);
        range = number("Range", "Maximum attack distance", 3.0, 2.0, 6.0, 0.1);
        targets = option("Targets", "Which entities to attack", TargetUtils.Targets.PLAYERS);
        sort = option("Sort", "How the target is chosen", TargetUtils.Sort.DISTANCE);
        delay = integer("Delay", "Milliseconds between swings", 100, 0, 1000);
        respectCooldown = bool("Respect cooldown", "Wait for the weapon to charge", true);
        rotate = bool("Rotate", "Face the target before swinging", true);
        silent = bool("Silent rotate", "Rotate on the server only", true);
        swing = bool("Swing", "Swing the arm on each hit", true);
        weaponOnly = bool("Weapon only", "Only attack while holding a weapon", true);
        maxTargets = integer("Targets per tick", "Maximum entities hit per cycle", 1, 1, 4);
    }

    private final DoubleSetting range;
    private final EnumSetting<TargetUtils.Targets> targets;
    private final EnumSetting<TargetUtils.Sort> sort;
    private final IntSetting delay;
    private final BooleanSetting respectCooldown;
    private final BooleanSetting rotate;
    private final BooleanSetting silent;
    private final BooleanSetting swing;
    private final BooleanSetting weaponOnly;
    private final IntSetting maxTargets;

    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
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
        List<LivingEntity> candidates = TargetUtils.candidates(range.get(), targets.get());
        if (candidates.isEmpty()) {
            return;
        }
        candidates.sort(comparator());
        int hit = 0;
        for (LivingEntity target : candidates) {
            if (hit >= maxTargets.get()) {
                break;
            }
            attack(target);
            hit++;
        }
    }

    private Comparator<LivingEntity> comparator() {
        return switch (sort.get()) {
            case HEALTH -> Comparator.comparingDouble(LivingEntity::getHealth);
            case ANGLE -> Comparator.comparingDouble(TargetUtils::angleDelta);
            default -> Comparator.comparingDouble(mc.player::distanceTo);
        };
    }

    private void attack(Entity target) {
        if (rotate.get()) {
            Rotation.face(target, silent.get(), 180f);
        }
        mc.gameMode.attack(mc.player, target);
        if (swing.get()) {
            mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        }
        if (rotate.get() && silent.get()) {
            Rotation.restore();
        }
    }

    @Override
    public String getArraylistSuffix() {
        return String.format(java.util.Locale.ROOT, "%.1f", range.get());
    }
}
