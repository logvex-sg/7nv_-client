package dev.sevennv.client.world;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.render.WorldRenderUtils;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * User-defined map markers with world-space labels. Markers are kept in memory
 * and can be added from the command line, so they act as temporary pins next to
 * the persistent {@link WaypointManager} entries.
 */
public final class MapMarkers extends Module {
    public MapMarkers() {
        super("Map Markers", "Draws temporary markers you place", Category.WORLD);
        range = number("Range", "Maximum marker distance", 256.0, 8.0, 4096.0, 8.0);
        color = color("Color", "Marker colour", 0xFFE0E0E0);
        maxMarkers = integer("Max markers", "Oldest markers are dropped past this count", 64, 1, 512);
        showDistance = bool("Distance", "Append the distance to each label", true);
    }

    private final DoubleSetting range;
    private final dev.sevennv.client.config.ColorSetting color;
    private final dev.sevennv.client.config.IntSetting maxMarkers;
    private final BooleanSetting showDistance;

    private final List<Marker> markers = new ArrayList<>();

    private record Marker(String name, Vec3 position) {
    }

    @Override
    public void onWorldChange() {
        markers.clear();
    }

    public void addMarker(String name, Vec3 position) {
        markers.add(new Marker(name, position));
        while (markers.size() > maxMarkers.get()) {
            markers.remove(0);
        }
    }

    public boolean removeMarker(String name) {
        return markers.removeIf(marker -> marker.name().equalsIgnoreCase(name));
    }

    public void clearMarkers() {
        markers.clear();
    }

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        double rangeSq = range.get() * range.get();
        for (Marker marker : markers) {
            double distanceSq = mc.player.position().distanceToSqr(marker.position());
            if (distanceSq > rangeSq) {
                continue;
            }
            String label = marker.name();
            if (showDistance.get()) {
                label += " " + (int) Math.sqrt(distanceSq) + "m";
            }
            WorldRenderUtils.label(marker.position(), label, color.get());
            WorldRenderUtils.line(marker.position(), marker.position().add(0, 1.0, 0), color.get());
        }
    }

    @Override
    public String getArraylistSuffix() {
        return String.valueOf(markers.size());
    }
}
