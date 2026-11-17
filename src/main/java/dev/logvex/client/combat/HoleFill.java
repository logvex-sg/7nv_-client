package dev.logvex.client.combat;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.DoubleSetting;
import dev.logvex.client.config.IntSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.utils.CombatUtils;
import dev.logvex.client.utils.HoleUtils;
import dev.logvex.client.utils.InventoryUtils;
import dev.logvex.client.utils.Rotation;
import dev.logvex.client.utils.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * Fills nearby holes with obsidian so an opponent cannot use them. Only holes
 * the player is not standing in are targeted.
 */
public final class HoleFill extends Module {
    public HoleFill() {
        super("Hole Fill", "Fills nearby holes with obsidian", Category.COMBAT);
        range = number("Range", "Search radius in blocks", 5.0, 1.0, 8.0, 0.5);
        delay = integer("Delay", "Milliseconds between placements", 100, 0, 1000);
        blocksPerTick = integer("Blocks per tick", "Maximum placements per cycle", 1, 1, 3);
        rotate = bool("Rotate", "Face the placement block", true);
        silent = bool("Silent rotate", "Rotate on the server only", true);
        autoSwitch = bool("Auto switch", "Select obsidian automatically", true);
        ignoreOwnHole = bool("Ignore own hole", "Skip the hole the player occupies", true);
    }

    private final DoubleSetting range;
    private final IntSetting delay;
    private final IntSetting blocksPerTick;
    private final BooleanSetting rotate;
    private final BooleanSetting silent;
    private final BooleanSetting autoSwitch;
    private final BooleanSetting ignoreOwnHole;

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
        if (!timer.tick(delay.get())) {
            return;
        }
        int radius = (int) Math.ceil(range.get());
        List<HoleUtils.Hole> holes = HoleUtils.scan(mc.level, mc.player.blockPosition(), radius);
        BlockPos own = mc.player.blockPosition();
        holes.removeIf(hole -> {
            if (ignoreOwnHole.get() && hole.pos().equals(own)) {
                return true;
            }
            return mc.player.getEyePosition().distanceTo(Vec3.atCenterOf(hole.pos())) > range.get();
        });
        if (holes.isEmpty()) {
            return;
        }
        holes.sort(Comparator.comparingDouble(hole -> mc.player.distanceToSqr(Vec3.atCenterOf(hole.pos()))));

        if (autoSwitch.get()) {
            ensureHeld();
        }
        if (!InventoryUtils.heldStack().is(Items.OBSIDIAN)) {
            return;
        }
        int placed = 0;
        for (HoleUtils.Hole hole : holes) {
            if (placed >= blocksPerTick.get()) {
                break;
            }
            if (place(hole.pos())) {
                placed++;
            }
        }
    }

    private boolean place(BlockPos target) {
        if (rotate.get()) {
            Rotation.face(Vec3.atCenterOf(target), silent.get(), 180f);
        }
        BlockPos neighbour = target.below();
        if (mc.level.getBlockState(neighbour).isAir()) {
            neighbour = target.north();
        }
        CombatUtils.placeBlock(neighbour, net.minecraft.core.Direction.UP);
        if (rotate.get() && silent.get()) {
            Rotation.restore();
        }
        return true;
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
