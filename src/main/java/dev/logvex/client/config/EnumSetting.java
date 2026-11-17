package dev.logvex.client.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class EnumSetting<E extends Enum<E>> extends Setting<E> {
    private final Class<E> type;

    public EnumSetting(String name, String description, E defaultValue) {
        super(name, description, defaultValue);
        @SuppressWarnings("unchecked")
        Class<E> resolved = (Class<E>) defaultValue.getDeclaringClass();
        this.type = resolved;
    }

    public E get() {
        return value;
    }

    public void set(E newValue) {
        value = newValue;
    }

    /** Advance to the next constant, wrapping around. */
    public void cycle(int direction) {
        E[] constants = type.getEnumConstants();
        int index = (value.ordinal() + direction) % constants.length;
        if (index < 0) {
            index += constants.length;
        }
        value = constants[index];
    }

    @Override
    public String displayValue() {
        return pretty(value.name());
    }

    private static String pretty(String raw) {
        String lower = raw.toLowerCase().replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        boolean upper = true;
        for (char c : lower.toCharArray()) {
            sb.append(upper ? Character.toUpperCase(c) : c);
            upper = c == ' ';
        }
        return sb.toString();
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value.name());
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            String name = element.getAsString();
            for (E constant : type.getEnumConstants()) {
                if (constant.name().equalsIgnoreCase(name)) {
                    value = constant;
                    return;
                }
            }
        }
    }
}
