package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.world.phys.AABB;

/**
 * Highlights the block the crosshair is pointing at with a wireframe box. This
 * is the client-side replacement for the vanilla block outline.
 */
public final class BlockHighlight extends Module {
    public BlockHighlight() {
        super("Block Highlight", "Outlines the block you are looking at", Category.RENDER);
        color = color("Color", "Outline colour", 0xFFFFFFFF);
        fill = bool("Fill", "Draw a translucent fill", true);
        fillAlpha = number("Fill alpha", "Fill opacity 0-255", 40.0, 0.0, 255.0, 5.0);
        lineWidth = number("Line width", "Stroke thickness", 2.0, 0.5, 4.0, 0.5);
    }

    private final ColorSetting color;
    private final BooleanSetting fill;
    private final DoubleSetting fillAlpha;
    private final DoubleSetting lineWidth;

    @Override
    public void onWorldRender() {
        if (mc.hitResult == null || !(mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit)) {
            return;
        }
        AABB box = new AABB(hit.getBlockPos()).inflate(0.002);
        if (fill.get()) {
            WorldRenderUtils.filledBox(box, color.get(), (float) lineWidth.get(),
                    RenderUtils.withAlpha(color.get(), (int) fillAlpha.get()));
        } else {
            WorldRenderUtils.box(box, color.get(), (float) lineWidth.get());
        }
    }
}
