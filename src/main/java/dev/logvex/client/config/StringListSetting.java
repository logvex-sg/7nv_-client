package dev.logvex.client.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** An ordered list of strings, used for message templates and macro definitions. */
public class StringListSetting extends Setting<List<String>> {
    public StringListSetting(String name, String description, List<String> defaultValue) {
        super(name, description, new ArrayList<>(defaultValue));
    }

    public StringListSetting(String name, String description, String... defaultValue) {
        this(name, description, Arrays.asList(defaultValue));
    }

    public List<String> get() {
        return value;
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    public void add(String entry) {
        value.add(entry);
    }

    public boolean remove(String entry) {
        return value.remove(entry);
    }

    public void set(int index, String entry) {
        if (index >= 0 && index < value.size()) {
            value.set(index, entry);
        }
    }

    /** Picks a random entry, or null when the list is empty. */
    public String random(java.util.Random random) {
        return value.isEmpty() ? null : value.get(random.nextInt(value.size()));
    }

    @Override
    public String displayValue() {
        return value.size() + " entr" + (value.size() == 1 ? "y" : "ies");
    }

    @Override
    public JsonElement toJson() {
        JsonArray array = new JsonArray();
        for (String entry : value) {
            array.add(entry);
        }
        return array;
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonArray()) {
            value.clear();
            for (JsonElement entry : element.getAsJsonArray()) {
                if (entry.isJsonPrimitive()) {
                    value.add(entry.getAsString());
                }
            }
        }
    }
}
