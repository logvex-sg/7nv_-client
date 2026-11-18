package dev.sevennv.client.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Persistent local player lists: friends, enemies and free-form notes with an
 * optional per-player colour. Lookups are case-insensitive on the bare name.
 */
public final class FriendManager {
    /** One managed player. Colour is a packed ARGB value. */
    public record Entry(String name, String note, int color) {
    }

    private static final Map<String, Entry> FRIENDS = new LinkedHashMap<>();
    private static final Map<String, Entry> ENEMIES = new LinkedHashMap<>();

    private FriendManager() {
    }

    private static String key(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }

    // ------------------------------------------------------------- friends

    public static void addFriend(String name) {
        add(FRIENDS, name, 0xFF5FBF5F);
    }

    public static void addFriend(String name, String note, int color) {
        FRIENDS.put(key(name), new Entry(name, note == null ? "" : note, color));
    }

    public static boolean isFriend(String name) {
        return FRIENDS.containsKey(key(name));
    }

    public static Collection<Entry> getFriends() {
        return FRIENDS.values();
    }

    // ------------------------------------------------------------- enemies

    public static void addEnemy(String name) {
        add(ENEMIES, name, 0xFFD05050);
    }

    public static void addEnemy(String name, String note, int color) {
        ENEMIES.put(key(name), new Entry(name, note == null ? "" : note, color));
    }

    public static boolean isEnemy(String name) {
        return ENEMIES.containsKey(key(name));
    }

    public static Collection<Entry> getEnemies() {
        return ENEMIES.values();
    }

    // -------------------------------------------------------------- shared

    private static void add(Map<String, Entry> map, String name, int color) {
        if (name == null || name.isBlank()) {
            return;
        }
        map.put(key(name), new Entry(name.trim(), "", color));
    }

    public static Entry get(String name) {
        Entry entry = FRIENDS.get(key(name));
        return entry != null ? entry : ENEMIES.get(key(name));
    }

    /** Colour for a name, or the supplied fallback when the player is unknown. */
    public static int colorOf(String name, int fallback) {
        Entry entry = get(name);
        return entry == null ? fallback : entry.color();
    }

    /** Removes a player from whichever list holds them. Returns true if removed. */
    public static boolean remove(String name) {
        String key = key(name);
        return FRIENDS.remove(key) != null | ENEMIES.remove(key) != null;
    }

    public static void clear() {
        FRIENDS.clear();
        ENEMIES.clear();
    }

    public static List<Entry> search(String query) {
        String needle = key(query);
        List<Entry> result = new ArrayList<>();
        for (Entry entry : FRIENDS.values()) {
            if (needle.isEmpty() || key(entry.name()).contains(needle)) {
                result.add(entry);
            }
        }
        for (Entry entry : ENEMIES.values()) {
            if (needle.isEmpty() || key(entry.name()).contains(needle)) {
                result.add(entry);
            }
        }
        return result;
    }

    // ------------------------------------------------------------ json io

    public static JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.add("friends", write(FRIENDS));
        root.add("enemies", write(ENEMIES));
        return root;
    }

    private static JsonArray write(Map<String, Entry> map) {
        JsonArray array = new JsonArray();
        for (Entry entry : map.values()) {
            JsonObject object = new JsonObject();
            object.addProperty("name", entry.name());
            object.addProperty("note", entry.note());
            object.addProperty("color", entry.color());
            array.add(object);
        }
        return array;
    }

    public static void fromJson(JsonObject root) {
        if (root == null) {
            return;
        }
        FRIENDS.clear();
        ENEMIES.clear();
        read(root, "friends", FRIENDS);
        read(root, "enemies", ENEMIES);
    }

    private static void read(JsonObject root, String key, Map<String, Entry> map) {
        JsonElement raw = root.get(key);
        if (raw == null || !raw.isJsonArray()) {
            return;
        }
        for (JsonElement element : raw.getAsJsonArray()) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject object = element.getAsJsonObject();
            if (!object.has("name")) {
                continue;
            }
            String name = object.get("name").getAsString();
            String note = object.has("note") ? object.get("note").getAsString() : "";
            int color = object.has("color") ? object.get("color").getAsInt() : 0xFF5FBF5F;
            map.put(key(name), new Entry(name, note, color));
        }
    }
}
