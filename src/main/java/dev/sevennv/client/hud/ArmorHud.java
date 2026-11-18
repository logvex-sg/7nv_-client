package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Renders the four armour pieces and the held item as item icons. */
public final class ArmorHud extends HudElement {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public ArmorHud() {
        super("armor", "Armor", 2, 148);
    }

    @Override
    public int getWidth() {
        return 4 * 18 + 2;
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    public void render(GuiGraphics graphics) {
        Player player = HudData.mc().player;
        if (player == null) {
            return;
        }
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        for (int i = 0; i < SLOTS.length; i++) {
            ItemStack stack = player.getItemBySlot(SLOTS[i]);
            if (stack.isEmpty()) {
                continue;
            }
            int itemX = x + 1 + i * 18;
            graphics.renderItem(stack, itemX, y + 2);
            if (stack.isDamageableItem()) {
                float fraction = dev.sevennv.client.utils.ItemUtils.durabilityPercent(stack);
                int barColor = fraction > 0.5f ? 0xFF55FF55 : fraction > 0.2f ? 0xFFFFFF55 : 0xFFFF5555;
                RenderUtils.rect(graphics, itemX, y + 17, 16, 2, 0xFF000000);
                RenderUtils.rect(graphics, itemX, y + 17, (int) (16 * fraction), 2, barColor);
            }
        }
    }
}
