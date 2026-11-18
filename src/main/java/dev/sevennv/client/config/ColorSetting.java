package dev.sevennv.client.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** Stores a colour as a packed ARGB integer, serialised as an #AARRGGBB string. */
public class ColorSetting extends Setting<Integer> {
    public ColorSetting(String name, String description, int defaultValue) {
        super(name, description, defaultValue);
    }

    public int get() {
        return value;
    }

    public void set(int newValue) {
        value = newValue;
    }

    public float alpha() {
        return ((value >> 24) & 0xFF) / 255f;
    }

    public int red() {
        return (value >> 16) & 0xFF;
    }

    public int green() {
        return (value >> 8) & 0xFF;
    }

    public int blue() {
        return value & 0xFF;
    }

    public void setChannels(int a, int r, int g, int b) {
        value = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    @Override
    public String displayValue() {
        return String.format("#%08X", value);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(String.format("#%08X", value));
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            String raw = element.getAsString().replace("#", "");
            try {
                value = (int) Long.parseLong(raw, 16);
            } catch (NumberFormatException ignored) {
                // keep the default colour when the stored value is malformed
            }
        }
    }
}
