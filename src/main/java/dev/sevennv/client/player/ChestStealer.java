package dev.sevennv.client.player;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ChestMenu;

/**
 * Moves items from the currently open chest into your inventory using the
 * vanilla quick-move click. Only a normal container screen is affected.
 */
public final class ChestStealer extends Module {
    public ChestStealer() {
        super("Chest Stealer", "Empties the open chest into your inventory", Category.PLAYER);
        delay = integer("Delay", "Milliseconds between moves", 100, 20, 1000);
        closeWhenEmpty = bool("Close when empty", "Close the screen once the chest is empty", true);
        skipHotbar = bool("Skip hotbar", "Do not move items into the hotbar", false);
    }

    private final IntSetting delay;
    private final BooleanSetting closeWhenEmpty;
    private final BooleanSetting skipHotbar;
    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        if (!(screen.getMenu() instanceof ChestMenu chest)) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        int containerSlots = chest.getRowCount() * 9;
        boolean moved = false;
        for (int slot = 0; slot < containerSlots; slot++) {
            if (chest.getSlot(slot).getItem().isEmpty()) {
                continue;
            }
            InventoryUtils.quickMove(slot);
            moved = true;
            break;
        }
        if (!moved && closeWhenEmpty.get() && chest.getContainer().isEmpty()) {
            mc.player.closeContainer();
        }
    }
}
