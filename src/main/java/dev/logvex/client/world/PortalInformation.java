package dev.logvex.client.world;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.ColorSetting;
import dev.logvex.client.config.DoubleSetting;
import dev.logvex.client.config.IntSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.render.WorldRenderUtils;
import dev.logvex.client.utils.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Highlights nether portals and labels the coordinates they link to. The link
 * is the vanilla 1:8 overworld/nether ratio; no server data is queried.
 *
 * <p>The world scan runs on a timer rather than every frame, and only the
 * cached positions are drawn, so a large scan radius does not cost a full cube
 * walk per frame.
 */
public final class PortalInformation extends Module {
    public PortalInformation() {
        super("Portal Information", "Highlights portals and shows linked coordinates", Category.WORLD);
        range = number("Range", "Scan radius in blocks", 32.0, 4.0, 128.0, 4.0);
        color = color("Color", "Portal colour", 0xFFA882FF);
        labels = bool("Labels", "Label each portal with its linked coordinates", true);
        lineWidth = number("Line width", "Stroke thickness", 1.5, 0.5, 4.0, 0.5);
        refresh = integer("Refresh", "Milliseconds between scans", 1000, 100, 5000);
    }

    private final DoubleSetting range;
    private final ColorSetting color;
    private final BooleanSetting labels;
    private final DoubleSetting lineWidth;
    private final IntSetting refresh;

    private final Timer timer = new Timer();
    private final List<BlockPos> portals = new ArrayList<>();

    @Override
    public void onWorldChange() {
        portals.clear();
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null || !timer.tick(refresh.get())) {
            return;
        }
        portals.clear();
        int radius = (int) Math.ceil(range.get());
        BlockPos centre = mc.player.blockPosition();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = centre.offset(dx, dy, dz);
                    if (mc.level.isLoaded(pos) && mc.level.getBlockState(pos).is(Blocks.NETHER_PORTAL)) {
                        portals.add(pos.immutable());
                    }
                }
            }
        }
    }

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null || portals.isEmpty()) {
            return;
        }
        boolean nether = mc.level.dimension().identifier().getPath().contains("nether");
        for (BlockPos pos : portals) {
            if (mc.player.position().distanceToSqr(Vec3.atCenterOf(pos)) > range.get() * range.get()) {
                continue;
            }
            WorldRenderUtils.box(new AABB(pos).inflate(0.02), color.get(), (float) lineWidth.get());
            if (labels.get()) {
                int linkedX = nether ? pos.getX() * 8 : pos.getX() / 8;
                int linkedZ = nether ? pos.getZ() * 8 : pos.getZ() / 8;
                WorldRenderUtils.label(WorldRenderUtils.blockTop(pos),
                        String.format(java.util.Locale.ROOT, "%d, %d", linkedX, linkedZ), color.get());
            }
        }
    }
}
