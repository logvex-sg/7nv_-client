package dev.sevennv.client.config;

import com.google.gson.JsonElement;

import java.util.function.BooleanSupplier;

/**
 * Base class for every configurable value. Settings serialise themselves to and
 * from a {@link JsonElement} so the config manager stays agnostic of the type.
 */
public abstract class Setting<T> {
    private final String name;
    private final String description;
    protected T value;
    private BooleanSupplier visible = () -> true;

    protected Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T newValue) {
        this.value = newValue;
    }

    public Setting<T> visibleWhen(BooleanSupplier predicate) {
        this.visible = predicate;
        return this;
    }

    public boolean isVisible() {
        return visible.getAsBoolean();
    }

    /** Text shown on the right of the setting row in the ClickGUI. */
    public String displayValue() {
        return String.valueOf(value);
    }

    public abstract JsonElement toJson();

    public abstract void fromJson(JsonElement element);
}
