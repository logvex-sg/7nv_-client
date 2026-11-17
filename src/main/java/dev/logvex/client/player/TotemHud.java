package dev.logvex.client.player;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.Theme;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.render.RenderUtils;
import dev.logvex.client.utils.InventoryUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Compact totem readout: how many totems are held and whether the offhand is
 * loaded. The combat {@code TotemCounter} module is the array-list variant.
 */
public final class TotemHud extends Module {
    public TotemHud() {
        super("Totem HUD", "Shows your totem count and offhand state", Category.PLAYER);
        showOffhand = bool("Offhand", "Show whether the offhand holds a totem", true);
        warnBelow = integer("Warn below", "Highlight when totems drop below this", 2, 0, 10);
    }

    private final BooleanSetting showOffhand;
    private final dev.logvex.client.config.IntSetting warnBelow;

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        if (mc.player == null) {
            return;
        }
        Theme theme = theme();
        int count = InventoryUtils.countItem(Items.TOTEM_OF_UNDYING);
        int width = 108;
        int height = 16;
        int x = 4;
        int y = 4;

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);

        int color = count <= warnBelow.get() ? 0xFFFF5050 : theme.text;
        RenderUtils.text(graphics, "Totems: " + count, x + 4, y + 4, color);
        if (showOffhand.get()) {
            ItemStack offhand = mc.player.getOffhandItem();
            boolean loaded = offhand.is(Items.TOTEM_OF_UNDYING);
            RenderUtils.textRight(graphics, loaded ? "OFF: OK" : "OFF: -", x + width - 4, y + 4,
                    loaded ? theme.enabled : theme.disabled);
        }
    }

    @Override
    public String getArraylistSuffix() {
        return String.valueOf(InventoryUtils.countItem(Items.TOTEM_OF_UNDYING));
    }

    private Theme theme() {
        var config = dev.logvex.client.LogvexClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
