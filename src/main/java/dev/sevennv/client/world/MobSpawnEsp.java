package dev.sevennv.client.world;

import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.render.WorldRenderUtils;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Marks loaded blocks where hostile mobs could spawn: a dark, solid floor with
 * two air blocks above it. This is a read-only lighting analysis of the client's
 * own world copy; it never asks the server for anything.
 */
public final class MobSpawnEsp extends Module {
    public MobSpawnEsp() {
        super("Spawn ESP", "Highlights blocks where mobs can spawn", Category.WORLD);
        range = integer("Range", "Search radius in blocks", 16, 4, 48);
        maxBlocks = integer("Max blocks", "Maximum highlights drawn", 128, 8, 1024);
        color = color("Color", "Highlight colour", 0xFFFF3030);
        lineWidth = number("Line width", "Stroke thickness", 1.5, 0.5, 4.0, 0.5);
        lightLevel = integer("Max light", "Maximum block light allowed", 0, 0, 15);
        refresh = integer("Refresh", "Milliseconds between scans", 1000, 200, 5000);
    }

    private final dev.sevennv.client.config.IntSetting range;
    private final dev.sevennv.client.config.IntSetting maxBlocks;
    private final ColorSetting color;
    private final DoubleSetting lineWidth;
    private final dev.sevennv.client.config.IntSetting lightLevel;
    private final dev.sevennv.client.config.IntSetting refresh;

    private final List<BlockPos> spots = new ArrayList<>();
    private final dev.sevennv.client.utils.Timer timer = new dev.sevennv.client.utils.Timer();

    @Override
    public void onWorldChange() {
        spots.clear();
    }

    @Override
    public void onDisable() {
        spots.clear();
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null || !timer.tick(refresh.get())) {
            return;
        }
        spots.clear();
        int radius = range.get();
        BlockPos centre = mc.player.blockPosition();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -4; dy <= 4; dy++) {
                    BlockPos pos = centre.offset(dx, dy, dz);
                    if (!mc.level.isLoaded(pos) || !isSpawnable(pos)) {
                        continue;
                    }
                    spots.add(pos.immutable());
                    if (spots.size() >= maxBlocks.get()) {
                        return;
                    }
                }
            }
        }
    }

    private boolean isSpawnable(BlockPos floor) {
        var below = mc.level.getBlockState(floor);
        if (below.isAir() || !below.isSolidRender()) {
            return false;
        }
        if (!mc.level.getBlockState(floor.above()).isAir() || !mc.level.getBlockState(floor.above(2)).isAir()) {
            return false;
        }
        return mc.level.getMaxLocalRawBrightness(floor.above()) <= lightLevel.get();
    }

    @Override
    public void onWorldRender() {
        for (BlockPos pos : spots) {
            WorldRenderUtils.box(dev.sevennv.client.utils.BlockUtils.box(pos), color.get(), (float) lineWidth.get());
        }
    }
}
