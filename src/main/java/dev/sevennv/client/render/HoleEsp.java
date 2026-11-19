package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.HoleUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Highlights safe holes in the surrounding terrain so a fight can be planned
 * around them. Only reads block state; nothing is modified.
 */
public final class HoleEsp extends Module {
    public HoleEsp() {
        super("Hole ESP", "Highlights safe holes around you", Category.RENDER);
        range = number("Range", "Scan radius in blocks", 8.0, 2.0, 32.0, 1.0);
        safeColor = color("Safe color", "Colour for bedrock holes", 0xFF50FF50);
        normalColor = color("Color", "Colour for obsidian holes", 0xFFFFC040);
        fill = bool("Fill", "Draw a translucent fill", true);
        lineWidth = number("Line width", "Stroke thickness", 1.5, 0.5, 4.0, 0.5);
        refresh = integer("Refresh", "Milliseconds between scans", 250, 50, 2000);
    }

    private final DoubleSetting range;
    private final ColorSetting safeColor;
    private final ColorSetting normalColor;
    private final BooleanSetting fill;
    private final DoubleSetting lineWidth;
    private final IntSetting refresh;

    // Hole positions change slowly (only when a block is placed or broken), so
    // the scan is throttled to a few times a second instead of running a full
    // 3D block sweep on every rendered frame.
    private final Timer timer = new Timer();
    private final List<HoleUtils.Hole> holes = new ArrayList<>();

    @Override
    public void onWorldChange() {
        holes.clear();
        timer.reset();
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null || !timer.tick(refresh.get())) {
            return;
        }
        int radius = (int) Math.ceil(range.get());
        holes.clear();
        holes.addAll(HoleUtils.scan(mc.level, mc.player.blockPosition(), radius));
    }

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        double rangeSq = range.get() * range.get();
        for (HoleUtils.Hole hole : holes) {
            BlockPos pos = hole.pos();
            if (mc.player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos)) > rangeSq) {
                continue;
            }
            boolean safe = isSafe(pos);
            int color = safe ? safeColor.get() : normalColor.get();
            AABB box = new AABB(pos).inflate(0.002);
            if (fill.get()) {
                WorldRenderUtils.filledBox(box, color, (float) lineWidth.get(),
                        RenderUtils.withAlpha(color, 0x30));
            } else {
                WorldRenderUtils.box(box, color, (float) lineWidth.get());
            }
        }
    }

    /** A hole is "safe" when all four walls and the floor are bedrock. */
    private boolean isSafe(BlockPos pos) {
        return isBedrock(pos.below())
                && isBedrock(pos.north()) && isBedrock(pos.south())
                && isBedrock(pos.east()) && isBedrock(pos.west());
    }

    private boolean isBedrock(BlockPos pos) {
        return mc.level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.BEDROCK);
    }
}
