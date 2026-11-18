package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Free inventory slots and the currently held item. */
public final class InventoryHud extends HudElement {
    public InventoryHud() {
        super("inventory", "Inventory HUD", 220, 88);
    }

    @Override
    public int getWidth() {
        return 118;
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
        int free = 0;
        for (int i = 0; i < 36; i++) {
            if (player.getInventory().getItem(i).isEmpty()) {
                free++;
            }
        }
        ItemStack held = player.getInventory().getSelectedItem();
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        RenderUtils.text(graphics, "Free slots " + free + "/36", x + 1, y + 1, 0xFFE0E0E0);
        RenderUtils.text(graphics, "Held " + HudFormat.format(held), x + 1, y + 11, 0xFFB0C0E0);
    }
}
