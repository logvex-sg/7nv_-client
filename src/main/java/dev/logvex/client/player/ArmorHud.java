package dev.logvex.client.player;

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
 * Compact armour panel: one row per worn piece with the item name and a
 * durability bar. Complements the combat {@code ArmorDurability} module with
 * names and a different anchor.
 */
public final class ArmorHud extends Module {
    public ArmorHud() {
        super("Armor HUD", "Shows your worn armour and durability", Category.PLAYER);
        showNames = bool("Names", "Show the item names", true);
        showDurability = bool("Durability", "Show durability bars", true);
        warnBelow = number("Warn below %", "Colour bars red below this durability", 20.0, 1.0, 90.0, 5.0);
    }

    private final BooleanSetting showNames;
    private final BooleanSetting showDurability;
    private final DoubleSetting warnBelow;

    private static final EquipmentSlot[] ORDER =
            {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        if (mc.player == null) {
            return;
        }
        Theme theme = theme();
        int width = 132;
        int rowHeight = 11;
        int height = 4 + ORDER.length * rowHeight;
        int x = graphics.guiWidth() - width - 4;
        int y = 4;

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);

        int rowY = y + 2;
        for (EquipmentSlot slot : ORDER) {
            ItemStack stack = mc.player.getItemBySlot(slot);
            String name = stack.isEmpty() ? "-" : stack.getHoverName().getString();
            if (showNames.get()) {
                RenderUtils.text(graphics, trim(name, 16), x + 3, rowY + 2, theme.text);
            }
            if (showDurability.get() && !stack.isEmpty() && stack.isDamageableItem()) {
                float fraction = ItemUtils.durabilityPercent(stack);
                int barX = x + width - 46;
                RenderUtils.rect(graphics, barX, rowY + 3, 40, 5, theme.sliderTrack);
                int barColor = fraction * 100f <= warnBelow.get() ? 0xFFFF5050 : 0xFF5FBF5F;
                RenderUtils.rect(graphics, barX, rowY + 3, (int) (40 * fraction), 5, barColor);
            }
            rowY += rowHeight;
        }
    }

    private static String trim(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 1) + "\u2026";
    }

    private Theme theme() {
        var config = dev.logvex.client.LogvexClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
