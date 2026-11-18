package dev.sevennv.client.player;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.Timer;

/**
 * Throws a splash potion when health is low. The potion is selected from the
 * hotbar or swapped in, then used through the normal item-use path.
 */
public final class AutoPot extends Module {
    public AutoPot() {
        super("Auto Pot", "Throws a healing potion when your health is low", Category.PLAYER);
        healthThreshold = number("Health", "Throw below this health", 12.0, 1.0, 20.0, 0.5);
        delay = integer("Delay", "Milliseconds between throws", 500, 0, 5000);
        potion = text("Potion", "Item name fragment to match", "healing");
        onlyWhileMoving = bool("Only while moving", "Throw only while moving", false);
    }

    private final DoubleSetting healthThreshold;
    private final IntSetting delay;
    private final dev.sevennv.client.config.StringSetting potion;
    private final BooleanSetting onlyWhileMoving;
    private final Timer timer = new Timer();
    private int previousSlot = -1;

    @Override
    public void onDisable() {
        restore();
    }

    @Override
    public void onWorldChange() {
        previousSlot = -1;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }
        if (mc.player.getHealth() > healthThreshold.get()) {
            return;
        }
        if (onlyWhileMoving.get() && mc.player.zza == 0f && mc.player.xxa == 0f) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        int slot = findPotion();
        if (slot == -1) {
            return;
        }
        if (previousSlot == -1) {
            previousSlot = mc.player.getInventory().getSelectedSlot();
        }
        InventoryUtils.selectSlot(slot);
        mc.gameMode.useItem(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND);
    }

    private int findPotion() {
        var inventory = mc.player.getInventory();
        String needle = potion.get().toLowerCase(java.util.Locale.ROOT);
        for (int i = 0; i < 9; i++) {
            var stack = inventory.getItem(i);
            if (matches(stack, needle)) {
                return i;
            }
        }
        for (int i = 9; i < inventory.getContainerSize(); i++) {
            var stack = inventory.getItem(i);
            if (matches(stack, needle)) {
                InventoryUtils.switchTo(stack.getItem());
                return inventory.getSelectedSlot();
            }
        }
        return -1;
    }

    private boolean matches(net.minecraft.world.item.ItemStack stack, String needle) {
        if (stack.isEmpty()) {
            return false;
        }
        String itemName = stack.getItem().toString().toLowerCase(java.util.Locale.ROOT);
        if (!itemName.contains("potion")) {
            return false;
        }
        String display = stack.getHoverName().getString().toLowerCase(java.util.Locale.ROOT);
        return display.contains(needle) || itemName.contains(needle);
    }

    private void restore() {
        if (previousSlot != -1 && mc.player != null) {
            InventoryUtils.selectSlot(previousSlot);
            previousSlot = -1;
        }
    }
}
