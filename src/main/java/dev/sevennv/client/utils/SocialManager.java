package dev.sevennv.client.utils;

import com.google.gson.JsonObject;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Name-only facade over {@link FriendManager} for the social commands and the
 * ESP modules that only need a set membership test.
 */
public final class SocialManager {
    private SocialManager() {
    }

    public static void addFriend(String name) {
        FriendManager.addFriend(name);
    }

    public static void addEnemy(String name) {
        FriendManager.addEnemy(name);
    }

    public static void remove(String name) {
        FriendManager.remove(name);
    }

    public static boolean isFriend(String name) {
        return FriendManager.isFriend(name);
    }

    public static boolean isEnemy(String name) {
        return FriendManager.isEnemy(name);
    }

    public static Set<String> getFriends() {
        Set<String> names = new LinkedHashSet<>();
        FriendManager.getFriends().forEach(entry -> names.add(entry.name()));
        return names;
    }

    public static Set<String> getEnemies() {
        Set<String> names = new LinkedHashSet<>();
        FriendManager.getEnemies().forEach(entry -> names.add(entry.name()));
        return names;
    }

    public static JsonObject toJson() {
        return FriendManager.toJson();
    }

    public static void fromJson(JsonObject object) {
        FriendManager.fromJson(object);
    }
}
