package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.BlockUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Outlines storage blocks in the loaded world.
 *
 * <p>The scan walks the loaded chunks around the player and runs on a timer;
 * only the cached positions are drawn each frame, so the cost is bounded by the
 * scan interval rather than the frame rate.
 */
public final class StorageEsp extends Module {
    public StorageEsp() {
        super("Storage ESP", "Outlines chests, barrels and other storage", Category.RENDER);
        range = number("Range", "Scan radius in blocks", 32.0, 4.0, 128.0, 4.0);
        color = color("Color", "Outline colour", 0xFFFFC040);
        shulkerColor = color("Shulker color", "Outline colour for shulkers", 0xFFB060FF);
        lineWidth = number("Line width", "Stroke thickness", 1.5, 0.5, 4.0, 0.5);
        includeEnder = bool("Include ender chests", "Also outline ender chests", true);
        refresh = integer("Refresh", "Milliseconds between scans", 1000, 100, 5000);
    }

    private final DoubleSetting range;
    private final ColorSetting color;
    private final ColorSetting shulkerColor;
    private final DoubleSetting lineWidth;
    private final BooleanSetting includeEnder;
    private final IntSetting refresh;

    private final Timer timer = new Timer();
    private final List<BlockPos> shulkers = new ArrayList<>();
    private final List<BlockPos> storage = new ArrayList<>();

    @Override
    public void onWorldChange() {
        shulkers.clear();
        storage.clear();
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null || !timer.tick(refresh.get())) {
            return;
        }
        shulkers.clear();
        storage.clear();
        int radius = (int) Math.ceil(range.get());
        BlockPos centre = mc.player.blockPosition();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = centre.offset(dx, dy, dz);
                    if (!mc.level.isLoaded(pos)) {
                        continue;
                    }
                    BlockState state = mc.level.getBlockState(pos);
                    if (!BlockUtils.isStorage(state)) {
                        continue;
                    }
                    if (!includeEnder.get() && state.is(net.minecraft.world.level.block.Blocks.ENDER_CHEST)) {
                        continue;
                    }
                    if (BlockUtils.isShulker(state)) {
                        shulkers.add(pos.immutable());
                    } else {
                        storage.add(pos.immutable());
                    }
                }
            }
        }
    }

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        double rangeSq = range.get() * range.get();
        float width = (float) lineWidth.get();
        for (BlockPos pos : storage) {
            if (mc.player.position().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos)) <= rangeSq) {
                WorldRenderUtils.box(new AABB(pos), color.get(), width);
            }
        }
        for (BlockPos pos : shulkers) {
            if (mc.player.position().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos)) <= rangeSq) {
                WorldRenderUtils.box(new AABB(pos), shulkerColor.get(), width);
            }
        }
    }

    @Override
    public String getArraylistSuffix() {
        return String.valueOf(storage.size() + shulkers.size());
    }
}
