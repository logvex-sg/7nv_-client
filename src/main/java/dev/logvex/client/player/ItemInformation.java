package dev.logvex.client.player;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.Theme;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.render.RenderUtils;
import dev.logvex.client.utils.ItemUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/**
 * Shows a detail panel for the item in hand: name, count, enchantments and
 * durability. Reads the held stack each frame; nothing is sent to the server.
 */
public final class ItemInformation extends Module {
    public ItemInformation() {
        super("Item Information", "Shows details for the item in your hand", Category.PLAYER);
        showEnchants = bool("Enchantments", "List enchantments", true);
        showDurability = bool("Durability", "Show remaining durability", true);
        showCount = bool("Count", "Show the stack size", true);
    }

    private final BooleanSetting showEnchants;
    private final BooleanSetting showDurability;
    private final BooleanSetting showCount;

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        if (mc.player == null) {
            return;
        }
        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }
        Theme theme = theme();
        int width = 150;
        int x = graphics.guiWidth() - width - 4;
        int y = 4;
        int height = 16;

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);

        String title = stack.getHoverName().getString();
        if (showCount.get() && stack.getCount() > 1) {
            title += " x" + stack.getCount();
        }
        RenderUtils.text(graphics, title, x + 4, y + 4, theme.accent);

        if (showDurability.get() && stack.isDamageableItem()) {
            int remaining = ItemUtils.remainingDurability(stack);
            int barX = x + width - 54;
            RenderUtils.rect(graphics, barX, y + 5, 48, 5, theme.sliderTrack);
            float fraction = ItemUtils.durabilityPercent(stack);
            RenderUtils.rect(graphics, barX, y + 5, (int) (48 * fraction), 5,
                    fraction < 0.2f ? 0xFFFF5050 : 0xFF5FBF5F);
            RenderUtils.textRight(graphics, String.valueOf(remaining), x + width - 4, y + 12, theme.textDim);
        }
    }

    private Theme theme() {
        var config = dev.logvex.client.LogvexClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
