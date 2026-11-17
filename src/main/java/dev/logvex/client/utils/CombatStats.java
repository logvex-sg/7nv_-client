package dev.logvex.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;

/**
 * Lightweight combat counters shared by the HUD elements and the counter
 * modules. Everything is derived from the currently loaded level each time it
 * is queried, so there is no state to leak across worlds.
 */
public final class CombatStats {
    /** How long a pop is attributed to a player, in milliseconds. */
    private static final long POP_MEMORY_MS = 5000L;

    private CombatStats() {
    }

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    /** End crystals currently visible within {@code range} blocks. */
    public static int crystalCount(double range) {
        Minecraft mc = mc();
        if (mc.level == null || mc.player == null) {
            return 0;
        }
        double rangeSq = range * range;
        int count = 0;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof EndCrystal && mc.player.distanceToSqr(entity) <= rangeSq) {
                count++;
            }
        }
        return count;
    }

    /** Total crystals anywhere in the loaded level. */
    public static int totalCrystals() {
        Minecraft mc = mc();
        if (mc.level == null) {
            return 0;
        }
        int count = 0;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof EndCrystal) {
                count++;
            }
        }
        return count;
    }

    /** Living players (excluding the local player) within {@code range}. */
    public static int nearbyPlayers(double range) {
        Minecraft mc = mc();
        if (mc.level == null || mc.player == null) {
            return 0;
        }
        double rangeSq = range * range;
        int count = 0;
        for (Player player : mc.level.players()) {
            if (player != mc.player && mc.player.distanceToSqr(player) <= rangeSq) {
                count++;
            }
        }
        return count;
    }

    /** Totems the local player is holding (main hand plus offhand). */
    public static int heldTotems() {
        Minecraft mc = mc();
        if (mc.player == null) {
            return 0;
        }
        int count = 0;
        if (mc.player.getMainHandItem().is(net.minecraft.world.item.Items.TOTEM_OF_UNDYING)) {
            count++;
        }
        if (mc.player.getOffhandItem().is(net.minecraft.world.item.Items.TOTEM_OF_UNDYING)) {
            count++;
        }
        return count;
    }

    /** Totems anywhere in the player's inventory. */
    public static int inventoryTotems() {
        return InventoryUtils.countItem(net.minecraft.world.item.Items.TOTEM_OF_UNDYING);
    }
}
