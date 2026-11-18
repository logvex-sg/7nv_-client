package dev.sevennv.client.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * The on-screen notification stack plus a bounded history. Notifications fade
 * after a fixed lifetime and the live list is capped, so it can never grow
 * without bound.
 */
public final class NotificationManager {
    public static final int INFO = 0xFF5FBF5F;
    public static final int WARN = 0xFFFFC040;
    public static final int ERROR = 0xFFFF5050;

    private static final int MAX_LIVE = 8;
    private static final int MAX_HISTORY = 200;
    private static final long LIFETIME_MS = 4000L;

    private static final Deque<Entry> ENTRIES = new ArrayDeque<>();
    private static final Deque<Entry> HISTORY = new ArrayDeque<>();

    private static boolean enabled = true;
    private static boolean showHistory;

    private NotificationManager() {
    }

    public record Entry(String title, String detail, int color, long created) {
        float alpha() {
            long age = System.currentTimeMillis() - created;
            if (age > LIFETIME_MS) {
                return 0f;
            }
            float remaining = (LIFETIME_MS - age) / (float) LIFETIME_MS;
            return remaining > 0.2f ? 1f : remaining / 0.2f;
        }
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setShowHistory(boolean value) {
        showHistory = value;
    }

    public static void push(String title, String detail, int color) {
        if (!enabled) {
            return;
        }
        Entry entry = new Entry(title, detail, color, System.currentTimeMillis());
        ENTRIES.addFirst(entry);
        while (ENTRIES.size() > MAX_LIVE) {
            ENTRIES.removeLast();
        }
        HISTORY.addFirst(entry);
        while (HISTORY.size() > MAX_HISTORY) {
            HISTORY.removeLast();
        }
    }

    public static void info(String title, String detail) {
        push(title, detail, INFO);
    }

    public static void warn(String title, String detail) {
        push(title, detail, WARN);
    }

    public static void error(String title, String detail) {
        push(title, detail, ERROR);
    }

    public static void clear() {
        ENTRIES.clear();
    }

    public static void clearHistory() {
        HISTORY.clear();
    }

    public static Deque<Entry> history() {
        return HISTORY;
    }

    public static int liveCount() {
        return ENTRIES.size();
    }

    /** Draws and expires the live stack. */
    public static void render(GuiGraphics graphics) {
        long now = System.currentTimeMillis();
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int y = 2;
        Iterator<Entry> iterator = ENTRIES.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            if (now - entry.created() > LIFETIME_MS) {
                iterator.remove();
                continue;
            }
            int alpha = (int) (entry.alpha() * 255f);
            int width = Math.max(RenderUtils.font().width(entry.title()), RenderUtils.font().width(entry.detail())) + 8;
            int x = screenWidth - width - 2;
            RenderUtils.rect(graphics, x, y, width, 22, RenderUtils.withAlpha(0x90000000, Math.min(alpha, 0x90)));
            RenderUtils.rect(graphics, x, y, 2, 22, RenderUtils.withAlpha(entry.color(), alpha));
            RenderUtils.text(graphics, entry.title(), x + 5, y + 3, RenderUtils.withAlpha(0xFFFFFFFF, alpha));
            RenderUtils.text(graphics, entry.detail(), x + 5, y + 12, RenderUtils.withAlpha(0xFFC0C0C0, alpha));
            y += 24;
        }
    }

    public static JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("enabled", enabled);
        object.addProperty("showHistory", showHistory);
        return object;
    }

    public static void fromJson(JsonObject object) {
        if (object == null) {
            return;
        }
        if (object.has("enabled")) {
            enabled = object.get("enabled").getAsBoolean();
        }
        if (object.has("showHistory")) {
            showHistory = object.get("showHistory").getAsBoolean();
        }
    }
}
