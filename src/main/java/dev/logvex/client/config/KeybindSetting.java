package dev.logvex.client.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;

public class KeybindSetting extends Setting<InputConstants.Key> {
    public KeybindSetting(String name, String description, InputConstants.Key defaultValue) {
        super(name, description, defaultValue);
    }

    public InputConstants.Key get() {
        return value;
    }

    public void set(InputConstants.Key key) {
        value = key;
    }

    public boolean isBound() {
        return value != null && value != InputConstants.UNKNOWN;
    }

    @Override
    public String displayValue() {
        if (value == null || value == InputConstants.UNKNOWN) {
            return "NONE";
        }
        return value.getDisplayName().getString().toUpperCase();
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        InputConstants.Key key = value == null ? InputConstants.UNKNOWN : value;
        object.addProperty("type", key.getType().name());
        object.addProperty("code", key.getValue());
        return object;
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("type") && object.has("code")) {
                try {
                    InputConstants.Type type = InputConstants.Type.valueOf(object.get("type").getAsString());
                    value = type.getOrCreate(object.get("code").getAsInt());
                } catch (IllegalArgumentException ignored) {
                    // unknown type name in the stored config, fall back to the default
                }
            }
        } else if (element != null && element.isJsonPrimitive()) {
            // tolerate configs written before keybinds stored their input type
            int code = element.getAsInt();
            value = code < 0 ? InputConstants.Type.MOUSE.getOrCreate(-code - 1) : InputConstants.Type.KEYSYM.getOrCreate(code);
        }
    }
}
