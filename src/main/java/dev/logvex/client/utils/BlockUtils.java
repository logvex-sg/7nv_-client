package dev.logvex.client.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Block classification used by the render and world modules. Every predicate
 * works on a {@link BlockState} so callers can feed either a live level lookup
 * or a stored snapshot.
 */
public final class BlockUtils {
    private BlockUtils() {
    }

    public static boolean isOre(BlockState state) {
        return state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE)
                || state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE)
                || state.is(Blocks.COPPER_ORE) || state.is(Blocks.DEEPSLATE_COPPER_ORE)
                || state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE)
                || state.is(Blocks.REDSTONE_ORE) || state.is(Blocks.DEEPSLATE_REDSTONE_ORE)
                || state.is(Blocks.LAPIS_ORE) || state.is(Blocks.DEEPSLATE_LAPIS_ORE)
                || state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)
                || state.is(Blocks.EMERALD_ORE) || state.is(Blocks.DEEPSLATE_EMERALD_ORE)
                || state.is(Blocks.NETHER_GOLD_ORE) || state.is(Blocks.NETHER_QUARTZ_ORE)
                || state.is(Blocks.ANCIENT_DEBRIS);
    }

    public static boolean isStorage(BlockState state) {
        return state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST) || state.is(Blocks.BARREL)
                || state.is(Blocks.SHULKER_BOX) || state.is(Blocks.ENDER_CHEST)
                || state.is(Blocks.HOPPER) || state.is(Blocks.DISPENSER) || state.is(Blocks.DROPPER)
                || state.is(Blocks.FURNACE) || state.is(Blocks.BLAST_FURNACE) || state.is(Blocks.SMOKER)
                || state.is(Blocks.BREWING_STAND) || state.is(Blocks.CRAFTER);
    }

    public static boolean isShulker(BlockState state) {
        return state.is(Blocks.SHULKER_BOX) || state.getBlock().getName().getString().toLowerCase(java.util.Locale.ROOT).contains("shulker");
    }

    /** Blocks that count as an "obsidian family" surround target. */
    public static boolean isHoleBlock(BlockState state) {
        return state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN)
                || state.is(Blocks.NETHERITE_BLOCK) || state.is(Blocks.ANVIL)
                || state.is(Blocks.ENDER_CHEST) || state.is(Blocks.RESPAWN_ANCHOR);
    }

    /** A solid block for surround placement checks (blast resistant by default). */
    public static boolean isBlastResistant(BlockState state) {
        return state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN)
                || state.is(Blocks.NETHERITE_BLOCK) || state.is(Blocks.ENDER_CHEST)
                || state.is(Blocks.ANCIENT_DEBRIS) || state.is(Blocks.ENCHANTING_TABLE)
                || state.is(Blocks.RESPAWN_ANCHOR);
    }

    public static AABB box(BlockPos pos) {
        return new AABB(pos);
    }

    /** The four horizontal directions plus up/down, for surround and hole fill. */
    public static List<BlockPos> neighbours(BlockPos origin, boolean includeVertical) {
        List<BlockPos> result = new ArrayList<>(6);
        result.add(origin.north());
        result.add(origin.south());
        result.add(origin.east());
        result.add(origin.west());
        if (includeVertical) {
            result.add(origin.above());
            result.add(origin.below());
        }
        return result;
    }

    public static BlockPos offset(BlockPos pos, Direction direction) {
        return pos.relative(direction);
    }
}
