package dev.sevennv.client.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Persistent named points of interest, grouped so a set of stashes or bases
 * can be shown and hidden together. Coordinates are stored as plain ints so the
 * manager stays usable before a world is loaded.
 */
public final class WaypointManager {
    public enum Kind {
        WAYPOINT(0xFF5FBF5F),
        DEATH(0xFFD05050),
        STASH(0xFFFFC040),
        PORTAL(0xFFA882FF),
        BASE(0xFF4A9FD5),
        MARKER(0xFFE0E0E0);

        private final int color;

        Kind(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }
    }

    /** One stored location. {@code dimension} is the level's path, e.g. "overworld". */
    public static final class Waypoint {
        private String name;
        private final String group;
        private final Kind kind;
        private int x;
        private int y;
        private int z;
        private String dimension;
        private boolean visible = true;
        private String note = "";

        Waypoint(String name, String group, Kind kind, int x, int y, int z, String dimension) {
            this.name = name;
            this.group = group;
            this.kind = kind;
            this.x = x;
            this.y = y;
            this.z = z;
            this.dimension = dimension;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getGroup() {
            return group;
        }

        public Kind getKind() {
            return kind;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }

        public void setPosition(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public String getDimension() {
            return dimension;
        }

        public void setDimension(String dimension) {
            this.dimension = dimension;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note == null ? "" : note;
        }

        public BlockPos pos() {
            return new BlockPos(x, y, z);
        }

        public double distanceSq(double px, double py, double pz) {
            double dx = px - x;
            double dy = py - y;
            double dz = pz - z;
            return dx * dx + dy * dy + dz * dz;
        }
    }

    private static final Map<String, Waypoint> WAYPOINTS = new LinkedHashMap<>();

    private WaypointManager() {
    }

    private static String key(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }

    public static Waypoint add(String name, String group, Kind kind, int x, int y, int z, String dimension) {
        Waypoint waypoint = new Waypoint(name, group == null ? "default" : group, kind, x, y, z, dimension);
        WAYPOINTS.put(key(name), waypoint);
        return waypoint;
    }

    public static boolean remove(String name) {
        return WAYPOINTS.remove(key(name)) != null;
    }

    public static Waypoint get(String name) {
        return WAYPOINTS.get(key(name));
    }

    public static List<Waypoint> all() {
        return new ArrayList<>(WAYPOINTS.values());
    }

    public static List<Waypoint> inDimension(String dimension) {
        List<Waypoint> result = new ArrayList<>();
        for (Waypoint waypoint : WAYPOINTS.values()) {
            if (waypoint.getDimension().equals(dimension)) {
                result.add(waypoint);
            }
        }
        return result;
    }

    public static List<Waypoint> inGroup(String group) {
        List<Waypoint> result = new ArrayList<>();
        for (Waypoint waypoint : WAYPOINTS.values()) {
            if (waypoint.getGroup().equalsIgnoreCase(group)) {
                result.add(waypoint);
            }
        }
        return result;
    }

    public static List<String> groups() {
        List<String> result = new ArrayList<>();
        for (Waypoint waypoint : WAYPOINTS.values()) {
            if (!result.contains(waypoint.getGroup())) {
                result.add(waypoint.getGroup());
            }
        }
        return result;
    }

    public static void clear() {
        WAYPOINTS.clear();
    }

    public static void clearGroup(String group) {
        WAYPOINTS.values().removeIf(waypoint -> waypoint.getGroup().equalsIgnoreCase(group));
    }

    // ------------------------------------------------------------ json io

    public static JsonArray toJson() {
        JsonArray array = new JsonArray();
        for (Waypoint waypoint : WAYPOINTS.values()) {
            JsonObject object = new JsonObject();
            object.addProperty("name", waypoint.getName());
            object.addProperty("group", waypoint.getGroup());
            object.addProperty("kind", waypoint.getKind().name());
            object.addProperty("x", waypoint.getX());
            object.addProperty("y", waypoint.getY());
            object.addProperty("z", waypoint.getZ());
            object.addProperty("dimension", waypoint.getDimension());
            object.addProperty("visible", waypoint.isVisible());
            object.addProperty("note", waypoint.getNote());
            array.add(object);
        }
        return array;
    }

    public static void fromJson(JsonArray array) {
        if (array == null) {
            return;
        }
        WAYPOINTS.clear();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject object = element.getAsJsonObject();
            if (!object.has("name")) {
                continue;
            }
            Kind kind = Kind.WAYPOINT;
            if (object.has("kind")) {
                try {
                    kind = Kind.valueOf(object.get("kind").getAsString());
                } catch (IllegalArgumentException ignored) {
                    // Unknown kind in a hand-edited config: fall back to WAYPOINT.
                }
            }
            Waypoint waypoint = add(
                    object.get("name").getAsString(),
                    object.has("group") ? object.get("group").getAsString() : "default",
                    kind,
                    object.has("x") ? object.get("x").getAsInt() : 0,
                    object.has("y") ? object.get("y").getAsInt() : 0,
                    object.has("z") ? object.get("z").getAsInt() : 0,
                    object.has("dimension") ? object.get("dimension").getAsString() : "overworld");
            waypoint.setVisible(!object.has("visible") || object.get("visible").getAsBoolean());
            waypoint.setNote(object.has("note") ? object.get("note").getAsString() : "");
        }
    }
}
