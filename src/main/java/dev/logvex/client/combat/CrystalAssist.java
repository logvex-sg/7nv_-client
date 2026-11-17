package dev.logvex.client.combat;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.DoubleSetting;
import dev.logvex.client.config.IntSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.utils.CombatUtils;
import dev.logvex.client.utils.InventoryUtils;
import dev.logvex.client.utils.Rotation;
import dev.logvex.client.utils.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Manual crystal helper: while the attack key is held and the crosshair rests
 * on a legal base, it places a crystal and immediately pops the nearest one.
 */
public final class CrystalAssist extends Module {
    public CrystalAssist() {
        super("Crystal Assist", "Auto-places and pops crystals while attacking", Category.COMBAT);
        range = number("Range", "Maximum placement distance", 4.5, 1.0, 6.0, 0.1);
        placeDelay = integer("Place delay", "Milliseconds between placements", 100, 0, 1000);
        breakDelay = integer("Break delay", "Milliseconds between breaks", 60, 0, 1000);
        silent = bool("Silent rotate", "Rotate on the server only", true);
        autoSwitch = bool("Auto switch", "Select crystals automatically", true);
        onlyOnAttack = bool("Only while attacking", "Require the attack key to be held", true);
    }

    private final DoubleSetting range;
    private final IntSetting placeDelay;
    private final IntSetting breakDelay;
    private final BooleanSetting silent;
    private final BooleanSetting autoSwitch;
    private final BooleanSetting onlyOnAttack;

    private final Timer placeTimer = new Timer();
    private final Timer breakTimer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }
        if (onlyOnAttack.get() && !mc.mouseHandler.isLeftPressed()) {
            return;
        }
        if (breakTimer.tick(breakDelay.get())) {
            popNearestCrystal();
        }
        if (placeTimer.tick(placeDelay.get())) {
            placeAtCrosshair();
        }
    }

    private void popNearestCrystal() {
        List<EndCrystal> crystals = CombatUtils.nearbyCrystals(range.get());
        if (crystals.isEmpty()) {
            return;
        }
        crystals.sort((a, b) -> Double.compare(mc.player.distanceToSqr(a), mc.player.distanceToSqr(b)));
        EndCrystal crystal = crystals.get(0);
        Rotation.face(crystal.position(), silent.get(), 180f);
        mc.gameMode.attack(mc.player, crystal);
        mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        if (silent.get()) {
            Rotation.restore();
        }
    }

    private void placeAtCrosshair() {
        BlockPos base = null;
        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult blockHit) {
            BlockPos hitPos = blockHit.getBlockPos();
            if (CombatUtils.canPlaceCrystal(hitPos)) {
                base = hitPos;
            } else if (CombatUtils.canPlaceCrystal(hitPos.below())) {
                base = hitPos.below();
            }
        }
        if (base == null) {
            return;
        }
        if (mc.player.getEyePosition().distanceTo(Vec3.atCenterOf(base).add(0, 0.5, 0)) > range.get()) {
            return;
        }
        if (autoSwitch.get() && !InventoryUtils.hasInHotbar(Items.END_CRYSTAL)) {
            InventoryUtils.switchTo(Items.END_CRYSTAL);
        }
        if (!InventoryUtils.heldStack().is(Items.END_CRYSTAL)) {
            return;
        }
        Rotation.face(Vec3.atCenterOf(base).add(0, 1.0, 0), silent.get(), 180f);
        CombatUtils.placeOnTop(base);
        if (silent.get()) {
            Rotation.restore();
        }
    }

    @Override
    public String getArraylistSuffix() {
        return String.valueOf((int) range.get());
    }
}
