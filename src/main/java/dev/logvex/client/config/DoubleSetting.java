package dev.logvex.client.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class DoubleSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;
    private final String suffix;

    public DoubleSetting(String name, String description, double defaultValue, double min, double max, double step) {
        this(name, description, defaultValue, min, max, step, "");
    }

    public DoubleSetting(String name, String description, double defaultValue, double min, double max, double step, String suffix) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
        this.suffix = suffix;
    }

    public double get() {
        return value;
    }

    public float getFloat() {
        return value.floatValue();
    }

    public int getInt() {
        return (int) Math.round(value);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    public String getSuffix() {
        return suffix;
    }

    public void set(double newValue) {
        value = clamp(newValue);
    }

    private double clamp(double raw) {
        double clamped = Math.max(min, Math.min(max, raw));
        if (step > 0) {
            clamped = Math.round(clamped / step) * step;
        }
        return clamped;
    }

    /** Fraction between min and max, used to place the slider knob. */
    public double fraction() {
        return max - min <= 0 ? 0 : (value - min) / (max - min);
    }

    @Override
    public String displayValue() {
        String text = step >= 1.0 ? String.valueOf(getInt()) : String.format("%.2f", value);
        return text + suffix;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            set(element.getAsDouble());
        }
    }
}
