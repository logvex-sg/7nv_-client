package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.NotificationManager;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Tops up the offhand with the configured item when it runs out. This is the
 * generic companion to Auto Totem: it can hold a shield, a gap, a potion or a
 * totem, and only acts while the offhand is empty.
 */
public final class OffhandManager extends Module {
    public OffhandManager() {
        super("Offhand Manager", "Keeps a chosen item stocked in the offhand", Category.COMBAT);
        item = option("Item", "Item to keep in the offhand", Item.TOTEM);
        delay = integer("Delay", "Milliseconds between refills", 150, 0, 1000);
        onlyWhenEmpty = bool("Only when empty", "Do not replace a non-empty offhand", true);
        notify = bool("Notify", "Show a notification on refill", false);
        healthThreshold = number("Health", "Only refill at or below this health (0 = always)", 0.0, 0.0, 20.0, 0.5);
    }

    public enum Item {
        TOTEM(Items.TOTEM_OF_UNDYING),
        SHIELD(Items.SHIELD),
        GOLDEN_APPLE(Items.GOLDEN_APPLE),
        ENCHANTED_GAPPLE(Items.ENCHANTED_GOLDEN_APPLE);

        private final net.minecraft.world.item.Item item;

        Item(net.minecraft.world.item.Item item) {
            this.item = item;
        }

        public net.minecraft.world.item.Item item() {
            return item;
        }
    }

    private final dev.sevennv.client.config.EnumSetting<Item> item;
    private final IntSetting delay;
    private final BooleanSetting onlyWhenEmpty;
    private final BooleanSetting notify;
    private final DoubleSetting healthThreshold;

    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }
        ItemStack offhand = mc.player.getOffhandItem();
        if (onlyWhenEmpty.get() && !offhand.isEmpty()) {
            return;
        }
        if (offhand.is(item.get().item())) {
            return;
        }
        if (healthThreshold.get() > 0.0 && mc.player.getHealth() > healthThreshold.get()) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        int index = InventoryUtils.findItem(item.get().item());
        if (index == -1) {
            return;
        }
        InventoryUtils.swapWithOffhand(InventoryUtils.menuSlot(index));
        if (notify.get()) {
            NotificationManager.info("Offhand", "Stocked " + item.get().name().toLowerCase(java.util.Locale.ROOT));
        }
    }
}
