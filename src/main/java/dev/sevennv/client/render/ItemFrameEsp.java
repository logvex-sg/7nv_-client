package dev.sevennv.client.render;

import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;

/**
 * Draws a box around item frames that hold a non-empty item, and labels the
 * item. Useful for spotting maps, banners and stored gear at a distance. Purely
 * a client-side overlay.
 */
public final class ItemFrameEsp extends Module {
    public ItemFrameEsp() {
        super("Item Frame ESP", "Outlines item frames that hold an item", Category.RENDER);
        range = number("Range", "Maximum draw distance", 32.0, 4.0, 128.0, 4.0);
        color = color("Color", "Outline colour", 0xFFFFD050);
        lineWidth = number("Line width", "Stroke thickness", 1.5, 0.5, 4.0, 0.5);
        names = bool("Labels", "Show the framed item name", true);
    }

    private final DoubleSetting range;
    private final ColorSetting color;
    private final DoubleSetting lineWidth;
    private final dev.sevennv.client.config.BooleanSetting names;

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        double rangeSq = range.get() * range.get();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof ItemFrame frame) || mc.player.distanceToSqr(entity) > rangeSq) {
                continue;
            }
            if (frame.getItem().isEmpty()) {
                continue;
            }
            WorldRenderUtils.box(WorldRenderUtils.entityBox(frame), color.get(), (float) lineWidth.get());
            if (names.get()) {
                WorldRenderUtils.label(WorldRenderUtils.entityCenter(frame).add(0, 0.4, 0),
                        frame.getItem().getHoverName().getString(), color.get());
            }
        }
    }
}
