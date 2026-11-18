package dev.sevennv.client.world;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.NotificationManager;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashSet;
import java.util.Set;

/**
 * Announces valuable dropped items near you. The scan is throttled to once a
 * second and each item entity is only announced once.
 */
public final class ItemNotifier extends Module {
    public ItemNotifier() {
        super("Item Notifier", "Announces valuable dropped items nearby", Category.WORLD);
        range = integer("Range", "Search radius in blocks", 16, 2, 48);
        diamonds = bool("Diamonds", "Announce diamonds and diamond blocks", true);
        netherite = bool("Netherite", "Announce netherite and ancient debris", true);
        elytra = bool("Elytra", "Announce elytra", true);
        totems = bool("Totems", "Announce totems", true);
        shulkers = bool("Shulkers", "Announce shulker boxes", true);
    }

    private final IntSetting range;
    private final BooleanSetting diamonds;
    private final BooleanSetting netherite;
    private final BooleanSetting elytra;
    private final BooleanSetting totems;
    private final BooleanSetting shulkers;
    private final Timer timer = new Timer();
    private final Set<Integer> seen = new HashSet<>();

    @Override
    public void onWorldChange() {
        seen.clear();
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null || !timer.tick(1000)) {
            return;
        }
        double rangeSq = range.get() * range.get();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity item) || seen.contains(entity.getId())) {
                continue;
            }
            if (mc.player.distanceToSqr(entity) > rangeSq) {
                continue;
            }
            ItemStack stack = item.getItem();
            if (stack.isEmpty() || !interesting(stack)) {
                continue;
            }
            seen.add(entity.getId());
            NotificationManager.info("Item", stack.getHoverName().getString() + " x" + stack.getCount());
        }
    }

    private boolean interesting(ItemStack stack) {
        var item = stack.getItem();
        if (diamonds.get() && (item == Items.DIAMOND || item == Items.DIAMOND_BLOCK)) {
            return true;
        }
        if (netherite.get() && (item == Items.NETHERITE_INGOT || item == Items.NETHERITE_SCRAP
                || item == Items.ANCIENT_DEBRIS || item == Items.NETHERITE_BLOCK)) {
            return true;
        }
        if (elytra.get() && item == Items.ELYTRA) {
            return true;
        }
        if (totems.get() && item == Items.TOTEM_OF_UNDYING) {
            return true;
        }
        return shulkers.get() && item.toString().contains("shulker_box");
    }
}
