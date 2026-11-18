package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Draws the boundaries of loaded chunks. Only chunks the client currently has
 * are drawn, so the lines track the real render distance.
 */
public final class ChunkBorders extends Module {
    public ChunkBorders() {
        super("Chunk Borders", "Draws chunk boundary lines", Category.RENDER);
        range = number("Range", "Maximum chunk distance to draw", 8.0, 1.0, 32.0, 1.0);
        color = color("Color", "Line colour", 0x80606060);
        lineWidth = number("Line width", "Stroke thickness", 1.0, 0.5, 4.0, 0.5);
        verticals = bool("Verticals", "Draw the vertical edges", true);
    }

    private final DoubleSetting range;
    private final ColorSetting color;
    private final DoubleSetting lineWidth;
    private final BooleanSetting verticals;

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        int playerChunkX = mc.player.blockPosition().getX() >> 4;
        int playerChunkZ = mc.player.blockPosition().getZ() >> 4;
        int radius = (int) Math.ceil(range.get());
        double minY = mc.level.getMinY();
        double maxY = mc.level.getMaxY() + 1;
        for (int cx = playerChunkX - radius; cx <= playerChunkX + radius; cx++) {
            for (int cz = playerChunkZ - radius; cz <= playerChunkZ + radius; cz++) {
                if (!mc.level.getChunkSource().hasChunk(cx, cz)) {
                    continue;
                }
                double x = cx * 16.0;
                double z = cz * 16.0;
                drawSquare(x, z, minY, maxY);
            }
        }
    }

    private void drawSquare(double x, double z, double minY, double maxY) {
        int lineColor = color.get();
        float width = (float) lineWidth.get();
        WorldRenderUtils.line(new Vec3(x, minY, z), new Vec3(x + 16, minY, z), lineColor, width);
        WorldRenderUtils.line(new Vec3(x + 16, minY, z), new Vec3(x + 16, minY, z + 16), lineColor, width);
        WorldRenderUtils.line(new Vec3(x + 16, minY, z + 16), new Vec3(x, minY, z + 16), lineColor, width);
        WorldRenderUtils.line(new Vec3(x, minY, z + 16), new Vec3(x, minY, z), lineColor, width);
        if (verticals.get()) {
            WorldRenderUtils.line(new Vec3(x, minY, z), new Vec3(x, maxY, z), lineColor, width);
            WorldRenderUtils.line(new Vec3(x + 16, minY, z), new Vec3(x + 16, maxY, z), lineColor, width);
            WorldRenderUtils.line(new Vec3(x, minY, z + 16), new Vec3(x, maxY, z + 16), lineColor, width);
            WorldRenderUtils.line(new Vec3(x + 16, minY, z + 16), new Vec3(x + 16, maxY, z + 16), lineColor, width);
        }
    }
}
