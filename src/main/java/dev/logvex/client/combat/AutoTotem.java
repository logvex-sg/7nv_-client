package dev.logvex.client.combat;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.DoubleSetting;
import dev.logvex.client.config.IntSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.utils.InventoryUtils;
import dev.logvex.client.utils.NotificationManager;
import dev.logvex.client.utils.Timer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Keeps a totem of undying in the offhand.
 *
 * <p>The offhand is refilled from the inventory with a swap click whenever the
 * health threshold is crossed (or always, when configured). A delay keeps the
 * client from issuing a burst of container clicks in a single tick.
 */
public final class AutoTotem extends Module {
    public AutoTotem() {
        super("Auto Totem", "Keeps a totem of undying in the offhand", Category.COMBAT);
        healthThreshold = number("Health", "Only refill when health is at or below this", 20.0, 1.0, 20.0, 0.5);
        alwaysHold = bool("Always hold", "Refill regardless of health", true);
        delay = integer("Delay", "Milliseconds between refills", 100, 0, 1000);
        notify = bool("Notify", "Show a notification on refill", false);
    }

    private final DoubleSetting healthThreshold;
    private final BooleanSetting alwaysHold;
    private final IntSetting delay;
    private final BooleanSetting notify;

    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }
        ItemStack offhand = mc.player.getOffhandItem();
        if (offhand.is(Items.TOTEM_OF_UNDYING)) {
            return;
        }
        if (!alwaysHold.get() && mc.player.getHealth() > healthThreshold.get()) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        int index = InventoryUtils.findItem(Items.TOTEM_OF_UNDYING);
        if (index == -1) {
            return;
        }
        InventoryUtils.swapWithOffhand(InventoryUtils.menuSlot(index));
        if (notify.get()) {
            NotificationManager.info("Auto Totem", "Refilled offhand totem");
        }
    }

    @Override
    public String getArraylistSuffix() {
        int count = InventoryUtils.countItem(Items.TOTEM_OF_UNDYING);
        return String.valueOf(count);
    }
}
