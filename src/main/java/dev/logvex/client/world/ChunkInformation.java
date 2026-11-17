package dev.logvex.client.world;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.render.WorldRenderUtils;
import net.minecraft.core.BlockPos;

/**
 * Shows the loaded chunk the player is standing in, its coordinates and how
 * many chunks are currently loaded. Read-only.
 */
public final class ChunkInformation extends Module {
    public ChunkInformation() {
        super("Chunk Information", "Shows your current chunk and loaded chunk count", Category.WORLD);
        labels = bool("Labels", "Draw a marker on the current chunk", false);
        showLoadedCount = bool("Loaded count", "Include the number of loaded chunks", true);
        color = color("Color", "Label colour", 0xFF80D0FF);
    }

    private final BooleanSetting labels;
    private final BooleanSetting showLoadedCount;
    private final dev.logvex.client.config.ColorSetting color;

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null || !labels.get()) {
            return;
        }
        BlockPos pos = mc.player.blockPosition();
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        BlockPos centre = new BlockPos(chunkX * 16 + 8, pos.getY(), chunkZ * 16 + 8);
        WorldRenderUtils.label(WorldRenderUtils.blockTop(centre), chunkX + ", " + chunkZ, color.get());
    }

    @Override
    public String getArraylistSuffix() {
        if (mc.player == null) {
            return "";
        }
        BlockPos pos = mc.player.blockPosition();
        String base = (pos.getX() >> 4) + ", " + (pos.getZ() >> 4);
        if (showLoadedCount.get() && mc.level != null) {
            return base + " (" + loadedChunks() + ")";
        }
        return base;
    }

    private int loadedChunks() {
        if (mc.level == null) {
            return 0;
        }
        int radius = mc.options.renderDistance().get();
        int chunkX = mc.player.blockPosition().getX() >> 4;
        int chunkZ = mc.player.blockPosition().getZ() >> 4;
        int count = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (mc.level.getChunkSource().hasChunk(chunkX + dx, chunkZ + dz)) {
                    count++;
                }
            }
        }
        return count;
    }
}
