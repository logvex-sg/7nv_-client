package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import dev.sevennv.client.utils.InventoryUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Items;

/** Counts the combat consumables you are carrying: pearls, gapples and XP. */
public final class ItemCounterHud extends HudElement {
    public ItemCounterHud() {
        super("itemcount", "Item Count", 2, 144);
    }

    @Override
    public int getWidth() {
        return RenderUtils.font().width(line()) + 4;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        RenderUtils.text(graphics, line(), x + 2, y + 1, 0xFFE0E0E0);
    }

    private String line() {
        return "Pearls " + InventoryUtils.countItem(Items.ENDER_PEARL)
                + " Gaps " + InventoryUtils.countItem(Items.GOLDEN_APPLE);
    }
}
