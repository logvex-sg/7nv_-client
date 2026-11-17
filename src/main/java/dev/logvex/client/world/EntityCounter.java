package dev.logvex.client.world;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Counts the loaded entities by type and exposes the totals as an array-list
 * suffix. The scan is a single pass over the render entity list, so the cost is
 * proportional to the number of loaded entities, not the world size.
 */
public final class EntityCounter extends Module {
    public EntityCounter() {
        super("Entity Counter", "Counts loaded entities by type", Category.WORLD);
        countPlayers = bool("Players", "Count players", true);
        countMobs = bool("Mobs", "Count living mobs", true);
        countItems = bool("Items", "Count dropped items", true);
    }

    private final BooleanSetting countPlayers;
    private final BooleanSetting countMobs;
    private final BooleanSetting countItems;

    private int players;
    private int mobs;
    private int items;
    private int total;

    @Override
    public void onWorldChange() {
        players = 0;
        mobs = 0;
        items = 0;
        total = 0;
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        players = 0;
        mobs = 0;
        items = 0;
        total = 0;
        for (Entity entity : mc.level.entitiesForRendering()) {
            total++;
            if (entity instanceof Player) {
                players++;
            } else if (entity instanceof LivingEntity) {
                mobs++;
            } else if (entity instanceof net.minecraft.world.entity.item.ItemEntity) {
                items++;
            }
        }
    }

    @Override
    public String getArraylistSuffix() {
        StringBuilder builder = new StringBuilder();
        if (countPlayers.get()) {
            builder.append('p').append(players);
        }
        if (countMobs.get()) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append('m').append(mobs);
        }
        if (countItems.get()) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append('i').append(items);
        }
        return builder.length() == 0 ? String.valueOf(total) : builder.toString();
    }

    /** Counts the loaded entities grouped by type, for the HUD or diagnostics. */
    public Map<EntityType<?>, Integer> byType() {
        Map<EntityType<?>, Integer> counts = new HashMap<>();
        if (mc.level == null) {
            return counts;
        }
        for (Entity entity : mc.level.entitiesForRendering()) {
            counts.merge(entity.getType(), 1, Integer::sum);
        }
        return counts;
    }
}
