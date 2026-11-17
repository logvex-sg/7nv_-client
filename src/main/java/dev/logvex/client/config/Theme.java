package dev.logvex.client.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The ClickGUI colour palette. Defaults reproduce the flat, dark grey
 * 1.8-era hacked-client look: no gradients, no rounded corners, no shadows.
 */
public final class Theme {
    public static final String[] PRESETS = {"Classic Grey", "Midnight", "Blood", "Emerald", "Amethyst"};

    public int background = 0xFF202020;
    public int panel = 0xFF2B2B2B;
    public int header = 0xFF1A1A1A;
    public int border = 0xFF101010;
    public int text = 0xFFD0D0D0;
    public int textDim = 0xFF808080;
    public int accent = 0xFF5FBF5F;
    public int enabled = 0xFF4CAF50;
    public int disabled = 0xFF808080;
    public int hover = 0xFF3A3A3A;
    public int highlight = 0xFF353535;
    public int sliderTrack = 0xFF1A1A1A;

    /** Copies every palette entry from {@code other} into this theme. */
    public void copyFrom(Theme other) {
        if (other == null) {
            return;
        }
        background = other.background;
        panel = other.panel;
        header = other.header;
        border = other.border;
        text = other.text;
        textDim = other.textDim;
        accent = other.accent;
        enabled = other.enabled;
        disabled = other.disabled;
        hover = other.hover;
        highlight = other.highlight;
        sliderTrack = other.sliderTrack;
    }

    public static Theme preset(String name) {
        Theme theme = new Theme();
        switch (name == null ? "" : name) {
            case "Midnight" -> {
                theme.background = 0xFF14161C;
                theme.panel = 0xFF1B1F2A;
                theme.header = 0xFF11131A;
                theme.border = 0xFF080A0E;
                theme.text = 0xFFC8D0E0;
                theme.textDim = 0xFF6A7385;
                theme.accent = 0xFF4A7FBF;
                theme.enabled = 0xFF5B9BD5;
                theme.hover = 0xFF262C3A;
                theme.highlight = 0xFF232936;
                theme.sliderTrack = 0xFF11131A;
            }
            case "Blood" -> {
                theme.background = 0xFF1C1414;
                theme.panel = 0xFF261B1B;
                theme.header = 0xFF150F0F;
                theme.border = 0xFF0A0606;
                theme.text = 0xFFE0C8C8;
                theme.textDim = 0xFF8A6A6A;
                theme.accent = 0xFFBF4A4A;
                theme.enabled = 0xFFD05050;
                theme.hover = 0xFF332222;
                theme.highlight = 0xFF2E1F1F;
                theme.sliderTrack = 0xFF150F0F;
            }
            case "Emerald" -> {
                theme.background = 0xFF121A16;
                theme.panel = 0xFF1A2620;
                theme.header = 0xFF0D1310;
                theme.border = 0xFF060A08;
                theme.text = 0xFFC8E0D0;
                theme.textDim = 0xFF6A8A78;
                theme.accent = 0xFF3FAF6F;
                theme.enabled = 0xFF46C77E;
                theme.hover = 0xFF22322A;
                theme.highlight = 0xFF1E2C25;
                theme.sliderTrack = 0xFF0D1310;
            }
            case "Amethyst" -> {
                theme.background = 0xFF17121C;
                theme.panel = 0xFF221A2B;
                theme.header = 0xFF110D15;
                theme.border = 0xFF08060A;
                theme.text = 0xFFD8C8E8;
                theme.textDim = 0xFF8A7A9A;
                theme.accent = 0xFFA882FF;
                theme.enabled = 0xFFA882FF;
                theme.hover = 0xFF2E2438;
                theme.highlight = 0xFF291F33;
                theme.sliderTrack = 0xFF110D15;
            }
            default -> {
                // "Classic Grey" is the field defaults above.
            }
        }
        return theme;
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("background", background);
        object.addProperty("panel", panel);
        object.addProperty("header", header);
        object.addProperty("border", border);
        object.addProperty("text", text);
        object.addProperty("textDim", textDim);
        object.addProperty("accent", accent);
        object.addProperty("enabled", enabled);
        object.addProperty("disabled", disabled);
        object.addProperty("hover", hover);
        object.addProperty("highlight", highlight);
        object.addProperty("sliderTrack", sliderTrack);
        return object;
    }

    public void fromJson(JsonObject object) {
        if (object == null) {
            return;
        }
        background = read(object, "background", background);
        panel = read(object, "panel", panel);
        header = read(object, "header", header);
        border = read(object, "border", border);
        text = read(object, "text", text);
        textDim = read(object, "textDim", textDim);
        accent = read(object, "accent", accent);
        enabled = read(object, "enabled", enabled);
        disabled = read(object, "disabled", disabled);
        hover = read(object, "hover", hover);
        highlight = read(object, "highlight", highlight);
        sliderTrack = read(object, "sliderTrack", sliderTrack);
    }

    private static int read(JsonObject object, String key, int fallback) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsInt() : fallback;
    }
}
