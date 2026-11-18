package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.CombatStats;
import dev.sevennv.client.utils.InventoryUtils;
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
                    dev.sevennv.client.utils.NotificationManager.warn("Totems",
                            "Lost a totem (" + lastCount + " -> " + count + ")");
                } else {
                    dev.sevennv.client.utils.NotificationManager.info("Totems",
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
