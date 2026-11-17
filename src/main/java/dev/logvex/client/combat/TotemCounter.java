package dev.logvex.client.combat;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.utils.CombatStats;
import dev.logvex.client.utils.InventoryUtils;
import net.minecraft.world.item.Items;

/**
 * Counts the totems the player is carrying and announces the total when it
 * changes. The count is derived on demand, so nothing is cached across worlds.
 */
public final class TotemCounter extends Module {
    public TotemCounter() {
        super("Totem Counter", "Tracks how many totems you are carrying", Category.COMBAT);
        includeInventory = bool("Include inventory", "Count totems anywhere in the inventory", true);
        announce = bool("Announce changes", "Show a notification when the count changes", true);
    }

    private final BooleanSetting includeInventory;
    private final BooleanSetting announce;

    private int lastCount = -1;

    @Override
    public void onEnable() {
        lastCount = -1;
    }

    @Override
    public void onWorldChange() {
        lastCount = -1;
    }

    @Override
    public void onTick() {
        int count = current();
        if (count != lastCount) {
            if (announce.get() && lastCount != -1) {
                if (count < lastCount) {
                    dev.logvex.client.utils.NotificationManager.warn("Totems",
                            "Lost a totem (" + lastCount + " -> " + count + ")");
                } else {
                    dev.logvex.client.utils.NotificationManager.info("Totems",
                            "Gained a totem (" + count + ")");
                }
            }
            lastCount = count;
        }
    }

    private int current() {
        return includeInventory.get()
                ? InventoryUtils.countItem(Items.TOTEM_OF_UNDYING)
                : CombatStats.heldTotems();
    }

    @Override
    public String getArraylistSuffix() {
        return String.valueOf(Math.max(0, lastCount));
    }
}
