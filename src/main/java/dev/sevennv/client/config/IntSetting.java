package dev.sevennv.client.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class IntSetting extends Setting<Integer> {
    private final int min;
    private final int max;

    public IntSetting(String name, String description, int defaultValue, int min, int max) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
    }

    public int get() {
        return value;
    }

    public void set(int newValue) {
        value = Math.max(min, Math.min(max, newValue));
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public double fraction() {
        return max - min <= 0 ? 0 : (double) (value - min) / (max - min);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            set(element.getAsInt());
        }
    }
}
