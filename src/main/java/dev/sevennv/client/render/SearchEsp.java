package dev.sevennv.client.render;

import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.config.StringListSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.BlockIds;
import dev.sevennv.client.utils.Timer;
import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Set;

/**
 * Highlights blocks whose registry name contains one of the configured search
 * terms. The scan is limited to a small radius around the player and is
 * throttled to a few times per second, so it never becomes a per-frame chunk
 * search.
 */
public final class SearchEsp extends Module {
    public SearchEsp() {
        super("Search ESP", "Highlights blocks matching search terms", Category.RENDER);
        terms = list("Terms", "Block id fragments to match, e.g. diamond, ancient_debris",
                "diamond_ore", "ancient_debris", "netherite_block", "spawner", "chest", "shulker");
        range = integer("Range", "Search radius in blocks", 16, 4, 48);
        color = color("Color", "Highlight colour", 0xFF40FF40);
        lineWidth = number("Line width", "Stroke thickness", 1.5, 0.5, 4.0, 0.5);
        maxBlocks = integer("Max blocks", "Maximum highlights drawn", 256, 8, 2048);
        refresh = integer("Refresh", "Milliseconds between scans", 250, 50, 2000);
    }

    private final StringListSetting terms;
    private final IntSetting range;
    private final ColorSetting color;
    private final DoubleSetting lineWidth;
    private final IntSetting maxBlocks;
    private final IntSetting refresh;

    private final Set<BlockPos> matches = new HashSet<>();
    private final Timer timer = new Timer();

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
        if (mc.level == null || mc.player == null) {
            return;
        }
        if (!timer.tick(refresh.get())) {
            return;
        }
        matches.clear();
        // BlockPos.MutableBlockPos avoids allocating a new position per sample.
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int radius = range.get();
        BlockPos centre = mc.player.blockPosition();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    cursor.set(centre.getX() + dx, centre.getY() + dy, centre.getZ() + dz);
                    if (!mc.level.isLoaded(cursor)) {
                        continue;
                    }
                    var state = mc.level.getBlockState(cursor);
                    if (state.isAir()) {
                        continue;
                    }
                    if (matches(state)) {
                        matches.add(cursor.immutable());
                        if (matches.size() >= maxBlocks.get()) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean matches(net.minecraft.world.level.block.state.BlockState state) {
        String id = BlockIds.of(state);
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
