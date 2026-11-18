package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.item.Items;

/**
 * Keeps a golden apple in the offhand when health drops, falling back to a
 * totem. The offhand is refilled with an ordinary swap click.
 */
public final class GappleOffhand extends Module {
    public GappleOffhand() {
        super("Gapple Offhand", "Puts a golden apple in your offhand when low", Category.COMBAT);
        healthThreshold = number("Health", "Switch below this health", 12.0, 1.0, 20.0, 0.5);
        delay = integer("Delay", "Milliseconds between swaps", 200, 0, 2000);
        enchanted = bool("Enchanted", "Prefer enchanted golden apples", true);
        keepTotem = bool("Keep totem", "Restore the totem once healthy again", true);
        notify = bool("Notify", "Announce each swap", false);
    }

    private final DoubleSetting healthThreshold;
    private final IntSetting delay;
    private final BooleanSetting enchanted;
    private final BooleanSetting keepTotem;
    private final BooleanSetting notify;
    private final Timer timer = new Timer();

    private boolean swapped;

    @Override
    public void onWorldChange() {
        swapped = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null || !timer.tick(delay.get())) {
            return;
        }
        var offhand = mc.player.getOffhandItem();
        boolean low = mc.player.getHealth() <= healthThreshold.get();
        if (low && !offhand.is(Items.GOLDEN_APPLE) && !offhand.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            if (equip(enchanted.get() ? Items.ENCHANTED_GOLDEN_APPLE : Items.GOLDEN_APPLE)
                    || equip(Items.GOLDEN_APPLE)) {
                swapped = true;
                if (notify.get()) {
                    dev.sevennv.client.utils.NotificationManager.info("Gapple", "Swapped offhand gapple");
                }
            }
            return;
        }
        if (!low && swapped && keepTotem.get()) {
            if (equip(Items.TOTEM_OF_UNDYING)) {
                swapped = false;
            }
        }
    }

    private boolean equip(net.minecraft.world.item.Item item) {
        int index = InventoryUtils.findItem(item);
        if (index == -1) {
            return false;
        }
        InventoryUtils.swapWithOffhand(InventoryUtils.menuSlot(index));
        return true;
    }
}
