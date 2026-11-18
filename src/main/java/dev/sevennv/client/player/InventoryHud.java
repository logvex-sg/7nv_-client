package dev.sevennv.client.player;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/**
 * Shows the first few hotbar and inventory slots as a compact text list, useful
 * when the vanilla hotbar is hidden.
 */
public final class InventoryHud extends Module {
    public InventoryHud() {
        super("Inventory HUD", "Lists your hotbar contents", Category.PLAYER);
        rows = integer("Rows", "How many hotbar slots to show", 9, 1, 9);
        showCounts = bool("Counts", "Show stack sizes", true);
        compact = bool("Compact", "Use short item names", false);
    }

    private final dev.sevennv.client.config.IntSetting rows;
    private final BooleanSetting showCounts;
    private final BooleanSetting compact;

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        if (mc.player == null) {
            return;
        }
        Theme theme = theme();
        int width = 120;
        int height = 4 + rows.get() * 10;
        int x = 4;
        int y = graphics.guiHeight() - height - 4;

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);

        int rowY = y + 2;
        for (int slot = 0; slot < rows.get(); slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            String text = stack.isEmpty() ? "-" : (compact.get() ? shortName(stack) : stack.getHoverName().getString());
            if (showCounts.get() && stack.getCount() > 1) {
                text += " x" + stack.getCount();
            }
            int color = slot == mc.player.getInventory().getSelectedSlot() ? theme.accent : theme.text;
            RenderUtils.text(graphics, trim(text, 18), x + 3, rowY + 1, color);
            rowY += 10;
        }
    }

    private static String shortName(ItemStack stack) {
        String name = stack.getHoverName().getString();
        return name.length() <= 6 ? name : name.substring(0, 6);
    }

    private static String trim(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 1) + "\u2026";
    }

    private Theme theme() {
        var config = dev.sevennv.client.SevennvClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
