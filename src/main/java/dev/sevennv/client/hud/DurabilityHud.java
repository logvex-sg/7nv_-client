package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Durability percentages for armour and the held tool. */
public final class DurabilityHud extends HudElement {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND
    };

    public DurabilityHud() {
        super("durability", "Durability", 110, 118);
    }

    @Override
    public int getWidth() {
        return 108;
    }

    @Override
    public int getHeight() {
        return SLOTS.length * 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        Player player = HudData.mc().player;
        if (player == null) {
            return;
        }
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        int row = 0;
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty() || !stack.isDamageableItem()) {
                continue;
            }
            int percent = Math.round(dev.sevennv.client.utils.ItemUtils.durabilityPercent(stack) * 100f);
            int color = percent > 50 ? 0xFF55FF55 : percent > 20 ? 0xFFFFFF55 : 0xFFFF5555;
            RenderUtils.text(graphics, slotLabel(slot), x + 1, y + row * 10 + 1, 0xFFA0A0A0);
            RenderUtils.textRight(graphics, percent + "%", x + getWidth() - 2, y + row * 10 + 1, color);
            row++;
        }
    }

    private static String slotLabel(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> "Helmet";
            case CHEST -> "Chestplate";
            case LEGS -> "Leggings";
            case FEET -> "Boots";
            case MAINHAND -> "Hand";
            default -> slot.getName();
        };
    }
}
