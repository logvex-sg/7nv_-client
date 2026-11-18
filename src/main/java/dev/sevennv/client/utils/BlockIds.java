package dev.sevennv.client.utils;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lower-case registry id for a block, cached per {@link Block}.
 *
 * <p>Resolving the id means building a translation key and lower-casing it,
 * which is far too expensive to repeat for every sample of a block scan. The
 * result is stable for the lifetime of the game, so a plain map is enough.
 */
public final class BlockIds {
    private static final Map<Block, String> CACHE = new ConcurrentHashMap<>();

    private BlockIds() {
    }

    public static String of(BlockState state) {
        return of(state.getBlock());
    }

    public static String of(Block block) {
        return CACHE.computeIfAbsent(block,
                value -> value.getDescriptionId().toLowerCase(Locale.ROOT));
    }
}
