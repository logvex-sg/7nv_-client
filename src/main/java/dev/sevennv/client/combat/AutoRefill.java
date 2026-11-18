package dev.sevennv.client.combat;

import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.ChatUtils;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Watches your combat supplies and prints a warning when one runs out. Nothing
 * is moved or refilled automatically; it is an awareness tool, not an
 * auto-swap.
 */
public final class AutoRefill extends Module {
    public AutoRefill() {
        super("Auto Refill", "Warns when your combat supplies run low", Category.COMBAT);
        watchTotems = bool("Totems", "Watch totems of undying", true);
        watchGapples = bool("Gapples", "Watch golden apples", true);
        watchPearls = bool("Pearls", "Watch ender pearls", true);
        watchCrystals = bool("Crystals", "Watch end crystals", true);
        watchObsidian = bool("Obsidian", "Watch obsidian", true);
        threshold = integer("Threshold", "Warn at or below this count", 1, 0, 16);
        cooldown = integer("Cooldown", "Milliseconds between warnings", 4000, 500, 60000);
    }

    private final dev.sevennv.client.config.BooleanSetting watchTotems;
    private final dev.sevennv.client.config.BooleanSetting watchGapples;
    private final dev.sevennv.client.config.BooleanSetting watchPearls;
    private final dev.sevennv.client.config.BooleanSetting watchCrystals;
    private final dev.sevennv.client.config.BooleanSetting watchObsidian;
    private final IntSetting threshold;
    private final IntSetting cooldown;
    private final Timer timer = new Timer();

    private boolean totemWarned;
    private boolean gappleWarned;
    private boolean pearlWarned;
    private boolean crystalWarned;
    private boolean obsidianWarned;

    @Override
    public void onWorldChange() {
        totemWarned = false;
        gappleWarned = false;
        pearlWarned = false;
        crystalWarned = false;
        obsidianWarned = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        if (watchTotems.get()) {
            totemWarned = check(Items.TOTEM_OF_UNDYING, "Totems", totemWarned);
        }
        if (watchGapples.get()) {
            gappleWarned = check(Items.GOLDEN_APPLE, "Gapples", gappleWarned);
        }
        if (watchPearls.get()) {
            pearlWarned = check(Items.ENDER_PEARL, "Pearls", pearlWarned);
        }
        if (watchCrystals.get()) {
            crystalWarned = check(Items.END_CRYSTAL, "Crystals", crystalWarned);
        }
        if (watchObsidian.get()) {
            obsidianWarned = check(Items.OBSIDIAN, "Obsidian", obsidianWarned);
        }
    }

    /** Returns the new warned state for this item. */
    private boolean check(Item item, String label, boolean warned) {
        int count = InventoryUtils.countItem(item);
        if (count > threshold.get()) {
            return false;
        }
        if (!warned && timer.tick(cooldown.get())) {
            ChatUtils.info(label + " low: " + count);
            return true;
        }
        return warned;
    }

    @Override
    public String getArraylistSuffix() {
        return String.valueOf(InventoryUtils.countItem(Items.TOTEM_OF_UNDYING));
    }
}
