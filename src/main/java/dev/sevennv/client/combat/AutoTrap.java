package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.CombatUtils;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.Rotation;
import dev.sevennv.client.utils.TargetUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Boxes the current target in with obsidian so they cannot walk out. Only the
 * blocks the target is standing in are filled; placement uses the vanilla block
 * interaction path, so the server sees an ordinary place.
 */
public final class AutoTrap extends Module {
    public AutoTrap() {
        super("Auto Trap", "Traps the target in obsidian", Category.COMBAT);
        range = integer("Range", "Maximum target distance", 5, 1, 8);
        blocksPerTick = integer("Blocks per tick", "Maximum placements per cycle", 2, 1, 6);
        delay = integer("Delay", "Milliseconds between cycles", 100, 0, 1000);
        head = bool("Cover head", "Also place a block above the target", true);
        autoSwitch = bool("Auto switch", "Select obsidian automatically", true);
        rotate = bool("Rotate", "Face each placement", true);
    }

    private final IntSetting range;
    private final IntSetting blocksPerTick;
    private final IntSetting delay;
    private final BooleanSetting head;
    private final BooleanSetting autoSwitch;
    private final BooleanSetting rotate;

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
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }
        LivingEntity target = TargetUtils.best(range.get(), TargetUtils.Targets.PLAYERS, TargetUtils.Sort.DISTANCE);
        if (target == null) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        if (autoSwitch.get()) {
            ensureHeld();
        }
        if (!InventoryUtils.heldStack().is(Items.OBSIDIAN)) {
            return;
        }
        int placed = 0;
        for (BlockPos pos : targets(target.blockPosition())) {
            if (placed >= blocksPerTick.get()) {
                break;
            }
            if (placeAt(pos)) {
                placed++;
            }
        }
    }

    private List<BlockPos> targets(BlockPos feet) {
        List<BlockPos> list = new ArrayList<>();
        list.add(feet.north());
        list.add(feet.south());
        list.add(feet.east());
        list.add(feet.west());
        list.add(feet.above(2).north());
        list.add(feet.above(2).south());
        list.add(feet.above(2).east());
        list.add(feet.above(2).west());
        if (head.get()) {
            list.add(feet.above(2));
        }
        return list;
    }

    private boolean placeAt(BlockPos target) {
        if (!mc.level.getBlockState(target).isAir()) {
            return false;
        }
        if (mc.player.getEyePosition().distanceTo(Vec3.atCenterOf(target)) > 4.5) {
            return false;
        }
        if (rotate.get()) {
            Rotation.face(Vec3.atCenterOf(target), true, 180f);
        }
        Direction face = faceToward(target);
        CombatUtils.placeBlock(target.relative(face), face.getOpposite());
        if (rotate.get()) {
            Rotation.restore();
        }
        return true;
    }

    private Direction faceToward(BlockPos target) {
        Direction best = Direction.DOWN;
        double bestDistance = Double.MAX_VALUE;
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = target.relative(direction);
            if (mc.level.getBlockState(neighbour).isAir()) {
                continue;
            }
            double distance = mc.player.distanceToSqr(Vec3.atCenterOf(neighbour));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = direction;
            }
        }
        return best;
    }

    private void ensureHeld() {
        if (InventoryUtils.heldStack().is(Items.OBSIDIAN)) {
            return;
        }
        int hotbar = InventoryUtils.findHotbarSlot(Items.OBSIDIAN);
        if (hotbar != -1) {
            if (previousSlot == -1) {
                previousSlot = mc.player.getInventory().getSelectedSlot();
            }
            InventoryUtils.selectSlot(hotbar);
            return;
        }
        if (InventoryUtils.switchTo(Items.OBSIDIAN) && previousSlot == -1) {
            previousSlot = mc.player.getInventory().getSelectedSlot();
        }
    }

    private void restoreSlot() {
        if (previousSlot != -1 && mc.player != null) {
            InventoryUtils.selectSlot(previousSlot);
            previousSlot = -1;
        }
    }
}
