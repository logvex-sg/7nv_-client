package dev.logvex.client.combat;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.DoubleSetting;
import dev.logvex.client.config.Theme;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.render.RenderUtils;
import dev.logvex.client.utils.ItemUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Compact armour durability readout: one row per worn piece with a coloured bar
 * and the remaining durability. Flat, text-only styling.
 */
public final class ArmorDurability extends Module {
    public ArmorDurability() {
        super("Armor Durability", "Shows the durability of your worn armour", Category.COMBAT);
        warnThreshold = number("Warn below %", "Colour the bar red below this durability", 20.0, 1.0, 90.0, 5.0);
        showValues = bool("Show values", "Print the remaining durability numbers", true);
        anchor = option("Anchor", "Corner to draw in", TargetHud.Anchor.TOP_RIGHT);
    }

    private final DoubleSetting warnThreshold;
    private final BooleanSetting showValues;
    private final dev.logvex.client.config.EnumSetting<TargetHud.Anchor> anchor;

    private static final EquipmentSlot[] ORDER =
            {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        if (mc.player == null) {
            return;
        }
        Theme theme = theme();
        int rowHeight = 11;
        int width = 118;
        int height = 4 + ORDER.length * rowHeight;
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        int x = switch (anchor.get()) {
            case TOP_LEFT, BOTTOM_LEFT -> 4;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - width - 4;
        };
        int y = switch (anchor.get()) {
            case TOP_LEFT, TOP_RIGHT -> 60;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - height - 4;
        };

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);

        int rowY = y + 2;
        for (EquipmentSlot slot : ORDER) {
            ItemStack stack = mc.player.getItemBySlot(slot);
            String label = slot.getName();
            RenderUtils.text(graphics, label, x + 3, rowY + 2, theme.textDim);
            int barX = x + 40;
            int barWidth = width - 44;
            RenderUtils.rect(graphics, barX, rowY + 2, barWidth, 6, theme.sliderTrack);
            if (!stack.isEmpty()) {
                float fraction = ItemUtils.durabilityPercent(stack);
                int barColor = fraction * 100f <= warnThreshold.get() ? 0xFFFF5050 : 0xFF5FBF5F;
                RenderUtils.rect(graphics, barX, rowY + 2, (int) (barWidth * fraction), 6, barColor);
                if (showValues.get()) {
                    int remaining = ItemUtils.remainingDurability(stack);
                    RenderUtils.textRight(graphics, String.valueOf(remaining), x + width - 3, rowY + 2,
                            fraction * 100f <= warnThreshold.get() ? 0xFFFF5050 : theme.textDim);
                }
            } else {
                RenderUtils.textRight(graphics, "-", x + width - 3, rowY + 2, theme.textDim);
            }
            rowY += rowHeight;
        }
    }

    private Theme theme() {
        var config = dev.logvex.client.LogvexClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
