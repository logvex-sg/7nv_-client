package dev.logvex.client.combat;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.DoubleSetting;
import dev.logvex.client.config.IntSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.utils.BlockUtils;
import dev.logvex.client.utils.CombatUtils;
import dev.logvex.client.utils.HoleUtils;
import dev.logvex.client.utils.InventoryUtils;
import dev.logvex.client.utils.Rotation;
import dev.logvex.client.utils.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Surrounds the player with blast-resistant blocks when they are standing in a
 * hole. Placement is performed against the four horizontal neighbours plus the
 * block above the head.
 */
public final class Surround extends Module {
    public Surround() {
        super("Surround", "Places obsidian around you when standing in a hole", Category.COMBAT);
        onlyInHole = bool("Only in hole", "Require a detected hole before placing", true);
        delay = integer("Delay", "Milliseconds between placements", 50, 0, 500);
        blocksPerTick = integer("Blocks per tick", "Maximum placements per cycle", 1, 1, 4);
        rotate = bool("Rotate", "Face the placement block", true);
        silent = bool("Silent rotate", "Rotate on the server only", true);
        autoSwitch = bool("Auto switch", "Select obsidian automatically", true);
        block = option("Block", "Material to place", Material.OBSIDIAN);
    }

    public enum Material {
        OBSIDIAN(Items.OBSIDIAN),
        NETHERITE(Items.NETHERITE_BLOCK),
        CRYING_OBSIDIAN(Items.CRYING_OBSIDIAN),
        ENDER_CHEST(Items.ENDER_CHEST);

        private final net.minecraft.world.item.Item item;

        Material(net.minecraft.world.item.Item item) {
            this.item = item;
        }

        public net.minecraft.world.item.Item item() {
            return item;
        }
    }

    private final BooleanSetting onlyInHole;
    private final IntSetting delay;
    private final IntSetting blocksPerTick;
    private final BooleanSetting rotate;
    private final BooleanSetting silent;
    private final BooleanSetting autoSwitch;
    private final dev.logvex.client.config.EnumSetting<Material> block;

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
        BlockPos feet = mc.player.blockPosition();
        if (onlyInHole.get() && HoleUtils.findHole(mc.level, feet) == null) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        if (autoSwitch.get()) {
            ensureHeld();
        }
        if (!InventoryUtils.heldStack().is(block.get().item())) {
            return;
        }
        int placed = 0;
        for (BlockPos pos : placementTargets(feet)) {
            if (placed >= blocksPerTick.get()) {
                break;
            }
            if (placeAt(pos)) {
                placed++;
            }
        }
    }

    private List<BlockPos> placementTargets(BlockPos feet) {
        List<BlockPos> targets = new java.util.ArrayList<>(5);
        targets.add(feet.north());
        targets.add(feet.south());
        targets.add(feet.east());
        targets.add(feet.west());
        targets.add(feet.above(2));
        return targets;
    }

    private boolean placeAt(BlockPos target) {
        if (!mc.level.getBlockState(target).isAir()) {
            return false;
        }
        if (mc.player.getEyePosition().distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(target)) > 4.5) {
            return false;
        }
        if (rotate.get()) {
            Rotation.face(net.minecraft.world.phys.Vec3.atCenterOf(target), silent.get(), 180f);
        }
        // Aim at the block face nearest to the player so the click is valid.
        net.minecraft.core.Direction face = faceToward(target);
        BlockPos neighbour = target.relative(face);
        CombatUtils.placeBlock(neighbour, face.getOpposite());
        if (rotate.get() && silent.get()) {
            Rotation.restore();
        }
        return true;
    }

    private net.minecraft.core.Direction faceToward(BlockPos target) {
        net.minecraft.core.Direction best = net.minecraft.core.Direction.DOWN;
        double bestDistance = Double.MAX_VALUE;
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
            BlockPos neighbour = target.relative(direction);
            if (mc.level.getBlockState(neighbour).isAir()) {
                continue;
            }
            double distance = mc.player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(neighbour));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = direction;
            }
        }
        return best;
    }

    private void ensureHeld() {
        if (InventoryUtils.heldStack().is(block.get().item())) {
            return;
        }
        int hotbar = InventoryUtils.findHotbarSlot(block.get().item());
        if (hotbar != -1) {
            if (previousSlot == -1) {
                previousSlot = mc.player.getInventory().getSelectedSlot();
            }
            InventoryUtils.selectSlot(hotbar);
            return;
        }
        if (InventoryUtils.switchTo(block.get().item())) {
            if (previousSlot == -1) {
                previousSlot = mc.player.getInventory().getSelectedSlot();
            }
        }
    }

    private void restoreSlot() {
        if (previousSlot != -1 && mc.player != null) {
            InventoryUtils.selectSlot(previousSlot);
            previousSlot = -1;
        }
    }
}
