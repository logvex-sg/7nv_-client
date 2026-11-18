package dev.sevennv.client.render;

import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.StringListSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Set;

/**
 * Highlights blocks whose registry name contains one of the configured search
 * terms. The scan is limited to a small radius around the player and the block
 * list is fixed per frame, so it never becomes a full chunk search.
 */
public final class SearchEsp extends Module {
    public SearchEsp() {
        super("Search ESP", "Highlights blocks matching search terms", Category.RENDER);
        terms = list("Terms", "Block id fragments to match, e.g. diamond, ancient_debris",
                "diamond_ore", "ancient_debris", "netherite_block", "spawner", "chest", "shulker");
        range = integer("Range", "Search radius in blocks", 16, 4, 64);
        color = color("Color", "Highlight colour", 0xFF40FF40);
        lineWidth = number("Line width", "Stroke thickness", 1.5, 0.5, 4.0, 0.5);
        maxBlocks = integer("Max blocks", "Maximum highlights drawn", 256, 8, 2048);
    }

    private final StringListSetting terms;
    private final dev.sevennv.client.config.IntSetting range;
    private final ColorSetting color;
    private final DoubleSetting lineWidth;
    private final dev.sevennv.client.config.IntSetting maxBlocks;

    private final Set<BlockPos> matches = new HashSet<>();

    @Override
    public void onWorldChange() {
        matches.clear();
    }

    @Override
    public void onDisable() {
        matches.clear();
    }

    @Override
    public void onTick() {
        matches.clear();
        if (mc.level == null || mc.player == null) {
            return;
        }
        int radius = range.get();
        BlockPos centre = mc.player.blockPosition();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = centre.offset(dx, dy, dz);
                    if (!mc.level.isLoaded(pos)) {
                        continue;
                    }
                    var state = mc.level.getBlockState(pos);
                    if (state.isAir()) {
                        continue;
                    }
                    if (matches(state, pos)) {
                        matches.add(pos.immutable());
                        if (matches.size() >= maxBlocks.get()) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean matches(net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
        String id = state.getBlock().getDescriptionId().toLowerCase(java.util.Locale.ROOT);
        for (String term : terms.get()) {
            String needle = term.trim().toLowerCase(java.util.Locale.ROOT);
            if (!needle.isEmpty() && id.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onWorldRender() {
        for (BlockPos pos : matches) {
            WorldRenderUtils.box(dev.sevennv.client.utils.BlockUtils.box(pos), color.get(), (float) lineWidth.get());
        }
    }
}
