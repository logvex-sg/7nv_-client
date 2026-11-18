package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.PacketUtils;
import dev.sevennv.client.utils.TargetUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.Comparator;
import java.util.List;

/**
 * Breaks the blocks holding a target in place, so the target drops into the
 * open. The break goes through the ordinary game-mode path, exactly like a
 * player holding down left-click, and only the four horizontal supports and the
 * floor are considered.
 */
public final class AutoCity extends Module {
    public AutoCity() {
        super("Auto City", "Breaks the blocks a target is standing on", Category.COMBAT);
        range = number("Range", "Maximum target distance", 4.5, 2.0, 6.0, 0.5);
        targetPlayers = bool("Players", "Consider other players as targets", true);
        targetMobs = bool("Mobs", "Consider hostile mobs as targets", false);
        delay = integer("Delay", "Milliseconds between block breaks", 120, 0, 1000);
        floorOnly = bool("Floor only", "Only break the block under the target", false);
        switchTool = bool("Switch tool", "Switch to the best hotbar pickaxe", true);
    }

    private final DoubleSetting range;
    private final BooleanSetting targetPlayers;
    private final BooleanSetting targetMobs;
    private final IntSetting delay;
    private final BooleanSetting floorOnly;
    private final BooleanSetting switchTool;
    private final Timer timer = new Timer();

    @Override
    public void onWorldChange() {
        timer.reset();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null || !timer.tick(delay.get())) {
            return;
        }
        LivingEntity target = bestTarget();
        if (target == null) {
            return;
        }
        BlockPos feet = target.blockPosition();
        List<BlockPos> supports = new java.util.ArrayList<>();
        supports.add(feet.below());
        if (!floorOnly.get()) {
            supports.add(feet.north());
            supports.add(feet.south());
            supports.add(feet.east());
            supports.add(feet.west());
        }
        supports.sort(Comparator.comparingDouble(pos -> mc.player.distanceToSqr(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)));
        for (BlockPos pos : supports) {
            if (!breakable(pos)) {
                continue;
            }
            if (switchTool.get()) {
                InventoryUtils.switchTo(Items.NETHERITE_PICKAXE);
            }
            mc.gameMode.destroyBlock(pos);
            PacketUtils.swing();
            return;
        }
    }

    private LivingEntity bestTarget() {
        LivingEntity best = null;
        if (targetPlayers.get()) {
            best = TargetUtils.best(range.get(), TargetUtils.Targets.PLAYERS, TargetUtils.Sort.DISTANCE);
        }
        if (best == null && targetMobs.get()) {
            best = TargetUtils.best(range.get(), TargetUtils.Targets.MOBS, TargetUtils.Sort.DISTANCE);
        }
        return best;
    }

    private boolean breakable(BlockPos pos) {
        if (!mc.level.isLoaded(pos)) {
            return false;
        }
        var state = mc.level.getBlockState(pos);
        if (state.isAir() || state.is(Blocks.BEDROCK) || state.is(Blocks.OBSIDIAN)
                || state.is(Blocks.CRYING_OBSIDIAN) || state.is(Blocks.NETHERITE_BLOCK)) {
            return false;
        }
        return mc.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 36.0;
    }
}
