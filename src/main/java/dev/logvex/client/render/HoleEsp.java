package dev.logvex.client.render;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.ColorSetting;
import dev.logvex.client.config.DoubleSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.utils.HoleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

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
    }

    private final DoubleSetting range;
    private final ColorSetting safeColor;
    private final ColorSetting normalColor;
    private final BooleanSetting fill;
    private final DoubleSetting lineWidth;

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        int radius = (int) Math.ceil(range.get());
        for (HoleUtils.Hole hole : HoleUtils.scan(mc.level, mc.player.blockPosition(), radius)) {
            BlockPos pos = hole.pos();
            if (mc.player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos)) > range.get() * range.get()) {
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
