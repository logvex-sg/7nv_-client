package dev.logvex.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/** Entity classification used by the ESP and HUD modules. */
public final class EntityUtils {
    private EntityUtils() {
    }

    public static boolean isPlayer(Entity entity) {
        return entity instanceof Player;
    }

    public static boolean isHostile(Entity entity) {
        return entity instanceof Enemy;
    }

    public static boolean isPassive(Entity entity) {
        return entity instanceof Animal;
    }

    public static boolean isLiving(Entity entity) {
        return entity instanceof LivingEntity;
    }

    public static boolean isLocalPlayer(Entity entity) {
        return entity == Minecraft.getInstance().player;
    }

    /** A short readable label for the entity, e.g. "Creeper" or a player name. */
    public static String label(Entity entity) {
        if (entity instanceof Player player) {
            return player.getGameProfile().name();
        }
        return entity.getType().getDescription().getString();
    }

    public static String blockEntityLabel(BlockEntity blockEntity) {
        var key = net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
        if (key == null) {
            return "Unknown";
        }
        String path = key.getPath();
        return Character.toUpperCase(path.charAt(0)) + path.substring(1).replace('_', ' ');
    }
}
