package dev.sevennv.client.player;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.render.RenderUtils;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.ItemUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Durability readout for every damageable stack in the inventory, sorted worst
 * first. Warns with a colour change below the configured threshold.
 */
public final class DurabilityHud extends Module {
    public DurabilityHud() {
        super("Durability HUD", "Lists durability for items in your inventory", Category.PLAYER);
        warnBelow = number("Warn below %", "Warn when durability is below this", 20.0, 1.0, 90.0, 5.0);
        maxRows = integer("Max rows", "Maximum number of items to list", 8, 1, 36);
        includeArmor = bool("Include armor", "Include the worn armor pieces", true);
    }

    private final DoubleSetting warnBelow;
    private final dev.sevennv.client.config.IntSetting maxRows;
    private final BooleanSetting includeArmor;

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        if (mc.player == null) {
            return;
        }
        List<ItemStack> damaged = collect();
        if (damaged.isEmpty()) {
            return;
        }
        Theme theme = theme();
        int width = 148;
        int height = 4 + damaged.size() * 11;
        int x = graphics.guiWidth() - width - 4;
        int y = graphics.guiHeight() - height - 4;

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);

        int rowY = y + 2;
        for (ItemStack stack : damaged) {
            float fraction = ItemUtils.durabilityPercent(stack);
            int color = fraction * 100f <= warnBelow.get() ? 0xFFFF5050 : theme.text;
            String name = stack.getHoverName().getString();
            if (name.length() > 16) {
                name = name.substring(0, 15) + "\u2026";
            }
            RenderUtils.text(graphics, name, x + 3, rowY + 1, color);
            RenderUtils.textRight(graphics, Math.round(fraction * 100f) + "%", x + width - 3, rowY + 1, color);
            RenderUtils.rect(graphics, x + 3, rowY + 9, width - 6, 1, theme.sliderTrack);
            RenderUtils.rect(graphics, x + 3, rowY + 9, (int) ((width - 6) * fraction), 1, color);
            rowY += 11;
        }
    }

    private List<ItemStack> collect() {
        List<ItemStack> damaged = new ArrayList<>();
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.isDamageableItem() && stack.isDamaged()) {
                damaged.add(stack);
            }
        }
        if (includeArmor.get()) {
            for (net.minecraft.world.entity.EquipmentSlot equipmentSlot : InventoryUtils.armorSlots()) {
                ItemStack stack = mc.player.getItemBySlot(equipmentSlot);
                if (!stack.isEmpty() && stack.isDamageableItem() && stack.isDamaged()) {
                    damaged.add(stack);
                }
            }
        }
        damaged.sort((a, b) -> Float.compare(ItemUtils.durabilityPercent(a), ItemUtils.durabilityPercent(b)));
        return damaged.size() > maxRows.get() ? damaged.subList(0, maxRows.get()) : damaged;
    }

    private Theme theme() {
        var config = dev.sevennv.client.SevennvClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
