package dev.logvex.client.render;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.ColorSetting;
import dev.logvex.client.config.DoubleSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * Boxes around dropped items. Grouping by rarity is not attempted; the colour
 * is fixed so the list stays cheap to draw.
 */
public final class ItemEsp extends Module {
    public ItemEsp() {
        super("Item ESP", "Outlines dropped items", Category.RENDER);
        range = number("Range", "Maximum draw distance", 32.0, 4.0, 128.0, 4.0);
        color = color("Color", "Outline colour", 0xFF60FFD0);
        lineWidth = number("Line width", "Stroke thickness", 1.0, 0.5, 4.0, 0.5);
        count = bool("Show count", "Include the stack size in the label", false);
    }

    private final DoubleSetting range;
    private final ColorSetting color;
    private final DoubleSetting lineWidth;
    private final BooleanSetting count;

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        double rangeSq = range.get() * range.get();
        for (ItemEntity item : mc.level.getEntitiesOfClass(ItemEntity.class,
                mc.player.getBoundingBox().inflate(range.get()))) {
            if (mc.player.distanceToSqr(item) > rangeSq) {
                continue;
            }
            WorldRenderUtils.box(WorldRenderUtils.entityBox(item), color.get(), (float) lineWidth.get());
            if (count.get()) {
                WorldRenderUtils.label(item.position().add(0, 0.6, 0),
                        "x" + item.getItem().getCount(), color.get());
            }
        }
    }
}
