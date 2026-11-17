package dev.logvex.client.combat;

import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.utils.CombatUtils;
import dev.logvex.client.utils.InventoryUtils;
import dev.logvex.client.utils.NotificationManager;
import dev.logvex.client.utils.Rotation;
import dev.logvex.client.utils.TargetUtils;
import dev.logvex.client.utils.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * Places end crystals next to the selected target and detonates them.
 *
 * <p>Behaviour: pick a target, find a legal obsidian/bedrock base within range,
 * face it, place the crystal, then break any crystal that is close enough to the
 * target to be worth popping. All actions go through the ordinary game-mode
 * calls, so the server sees normal player input.
 */
public final class CrystalAura extends Module {
    public CrystalAura() {
        super("Crystal Aura", "Places and detonates end crystals around the target", Category.COMBAT);
        targetMode = option("Targets", "Which entities to attack", TargetUtils.Targets.PLAYERS);
        range = number("Range", "Maximum placement distance", 4.5, 1.0, 6.0, 0.1);
        breakRange = number("Break range", "Maximum crystal break distance", 4.5, 1.0, 6.0, 0.1);
        placeDelay = integer("Place delay", "Milliseconds between placements", 120, 0, 1000);
        breakDelay = integer("Break delay", "Milliseconds between breaks", 80, 0, 1000);
        minDamage = number("Min damage", "Skip placements below this estimated damage", 6.0, 0.0, 20.0, 0.5);
        silent = bool("Silent rotate", "Rotate on the server only, not visibly", true);
        autoSwitch = bool("Auto switch", "Switch to crystals in the hotbar automatically", true);
        antiSuicide = bool("Anti suicide", "Do not place a crystal that would kill you", true);
        notify = bool("Notifications", "Notify when a target is popped", true);
    }

    private final dev.logvex.client.config.EnumSetting<TargetUtils.Targets> targetMode;
    private final dev.logvex.client.config.DoubleSetting range;
    private final dev.logvex.client.config.DoubleSetting breakRange;
    private final dev.logvex.client.config.IntSetting placeDelay;
    private final dev.logvex.client.config.IntSetting breakDelay;
    private final dev.logvex.client.config.DoubleSetting minDamage;
    private final dev.logvex.client.config.BooleanSetting silent;
    private final dev.logvex.client.config.BooleanSetting autoSwitch;
    private final dev.logvex.client.config.BooleanSetting antiSuicide;
    private final dev.logvex.client.config.BooleanSetting notify;

    private final Timer placeTimer = new Timer();
    private final Timer breakTimer = new Timer();
    private int previousSlot = -1;

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @Override
    public void onWorldChange() {
        restoreSlot();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) {
            return;
        }
        LivingEntity target = TargetUtils.best(range.get(), targetMode.get(), TargetUtils.Sort.DISTANCE);
        if (target == null) {
            return;
        }
        if (breakTimer.tick(breakDelay.get())) {
            breakCrystals(target);
        }
        if (placeTimer.tick(placeDelay.get())) {
            placeCrystal(target);
        }
    }

    private void breakCrystals(LivingEntity target) {
        Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        List<EndCrystal> crystals = CombatUtils.nearbyCrystals(breakRange.get());
        crystals.sort(Comparator.comparingDouble(crystal -> crystal.distanceToSqr(target)));
        for (EndCrystal crystal : crystals) {
            if (mc.player.distanceToSqr(crystal) > breakRange.get() * breakRange.get()) {
                continue;
            }
            if (antiSuicide.get()) {
                float self = CombatUtils.estimateExplosionDamage(mc.player, crystal.position(), CombatUtils.CRYSTAL_POWER, true);
                if (self >= mc.player.getHealth()) {
                    continue;
                }
            }
            Rotation.face(crystal.position(), silent.get(), 180f);
            mc.gameMode.attack(mc.player, crystal);
            mc.player.swing(InteractionHand.MAIN_HAND);
            if (silent.get()) {
                Rotation.restore();
            }
            if (notify.get()) {
                NotificationManager.info("Crystal", "Popped crystal near " + dev.logvex.client.utils.EntityUtils.label(target));
            }
            break;
        }
    }

    private void placeCrystal(LivingEntity target) {
        int slot = InventoryUtils.findHotbarSlot(Items.END_CRYSTAL);
        if (slot == -1) {
            if (!autoSwitch.get()) {
                return;
            }
            if (!InventoryUtils.switchTo(Items.END_CRYSTAL)) {
                return;
            }
            slot = InventoryUtils.findHotbarSlot(Items.END_CRYSTAL);
            if (slot == -1) {
                return;
            }
        } else if (autoSwitch.get() && mc.player.getInventory().getSelectedSlot() != slot) {
            previousSlot = mc.player.getInventory().getSelectedSlot();
            InventoryUtils.selectSlot(slot);
        }

        BlockPos feet = target.blockPosition();
        List<BlockPos> bases = CombatUtils.crystalBases(feet, (int) Math.ceil(range.get()));
        BlockPos best = null;
        double bestDamage = 0.0;
        for (BlockPos base : bases) {
            Vec3 centre = new Vec3(base.getX() + 0.5, base.getY() + 1.0, base.getZ() + 0.5);
            if (mc.player.getEyePosition().distanceTo(centre) > range.get()) {
                continue;
            }
            float damage = CombatUtils.estimateExplosionDamage(target, centre, CombatUtils.CRYSTAL_POWER, true);
            if (damage < minDamage.get()) {
                continue;
            }
            if (antiSuicide.get()) {
                float self = CombatUtils.estimateExplosionDamage(mc.player, centre, CombatUtils.CRYSTAL_POWER, true);
                if (self >= mc.player.getHealth()) {
                    continue;
                }
            }
            if (damage > bestDamage) {
                bestDamage = damage;
                best = base;
            }
        }
        if (best == null) {
            return;
        }
        Rotation.face(new Vec3(best.getX() + 0.5, best.getY() + 1.0, best.getZ() + 0.5), silent.get(), 180f);
        CombatUtils.placeOnTop(best);
        if (silent.get()) {
            Rotation.restore();
        }
    }

    private void restoreSlot() {
        if (previousSlot != -1) {
            InventoryUtils.selectSlot(previousSlot);
            previousSlot = -1;
        }
    }

    @Override
    public String getArraylistSuffix() {
        return String.valueOf((int) range.get());
    }
}
