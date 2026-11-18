package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Marks where players were last seen before they left the render distance or
 * disconnected. Records are kept in memory only and cleared on world change.
 */
public final class LogoutSpots extends Module {
    public LogoutSpots() {
        super("Logout Spots", "Marks where players were last seen", Category.RENDER);
        range = number("Range", "Distance at which a spot is forgotten", 128.0, 8.0, 512.0, 8.0);
        color = color("Color", "Marker colour", 0xFFFF8080);
        lineWidth = number("Line width", "Stroke thickness", 1.5, 0.5, 4.0, 0.5);
        showName = bool("Show name", "Label the spot with the player's name", true);
        removeOnReturn = bool("Remove on return", "Forget a spot when the player is seen again", true);
    }

    private final DoubleSetting range;
    private final ColorSetting color;
    private final DoubleSetting lineWidth;
    private final BooleanSetting showName;
    private final BooleanSetting removeOnReturn;

    private final Map<UUID, Spot> spots = new HashMap<>();

    private record Spot(String name, Vec3 position) {
    }

    @Override
    public void onWorldChange() {
        spots.clear();
    }

    @Override
    public void onDisable() {
        spots.clear();
    }

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        java.util.Set<UUID> online = new java.util.HashSet<>();
        for (Player player : mc.level.players()) {
            if (player == mc.player) {
                continue;
            }
            online.add(player.getUUID());
            if (removeOnReturn.get()) {
                spots.remove(player.getUUID());
            }
        }
        // A player who was tracked but is no longer visible becomes a spot.
        for (Map.Entry<UUID, Spot> entry : spots.entrySet()) {
            Spot spot = entry.getValue();
            if (mc.player.position().distanceTo(spot.position()) > range.get()) {
                continue;
            }
            AABB box = new AABB(spot.position().x - 0.4, spot.position().y, spot.position().z - 0.4,
                    spot.position().x + 0.4, spot.position().y + 1.8, spot.position().z + 0.4);
            WorldRenderUtils.box(box, color.get(), (float) lineWidth.get());
            if (showName.get()) {
                WorldRenderUtils.label(spot.position().add(0, 2.1, 0), spot.name(), color.get());
            }
        }
        Iterator<Map.Entry<UUID, Spot>> iterator = spots.entrySet().iterator();
        while (iterator.hasNext()) {
            if (mc.player.position().distanceTo(iterator.next().getValue().position()) > range.get()) {
                iterator.remove();
            }
        }
    }

    /** Records a spot. Called by the client when a player leaves the level. */
    public void record(Player player) {
        spots.put(player.getUUID(), new Spot(player.getGameProfile().name(), player.position()));
    }

    public int spotCount() {
        return spots.size();
    }
}
