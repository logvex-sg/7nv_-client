package dev.logvex.client.gui.widget;

import dev.logvex.client.config.Theme;
import dev.logvex.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/**
 * A numeric slider row: label and value on top, a draggable track below.
 * Used for both double and integer settings by way of a normalised getter and
 * setter, so the drag maths lives in exactly one place.
 */
public final class SliderWidget extends SettingWidget {
    private static final int TRACK = 4;

    private final String label;
    private final DoubleSupplier getter;
    private final DoubleConsumer setter;
    private final double min;
    private final double max;
    private final String suffix;
    private final Theme theme;
    private boolean dragging;

    public SliderWidget(String label, DoubleSupplier getter, DoubleConsumer setter,
                        double min, double max, String suffix, Theme theme,
                        int x, int y, int width, int height) {
        super(x, y, width, height);
        this.label = label;
        this.getter = getter;
        this.setter = setter;
        this.min = min;
        this.max = max;
        this.suffix = suffix == null ? "" : suffix;
        this.theme = theme;
    }

    private double fraction() {
        return max - min <= 0 ? 0 : (getter.getAsDouble() - min) / (max - min);
    }

    private String valueText() {
        double value = getter.getAsDouble();
        String formatted = Math.abs(value - Math.rint(value)) < 1e-6
                ? String.valueOf((long) Math.rint(value))
                : String.format(Locale.ROOT, "%.2f", value);
        return formatted + suffix;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        RenderUtils.rect(graphics, x, y, width, height, hovered(mouseX, mouseY) || dragging ? theme.hover : theme.panel);
        RenderUtils.text(graphics, label, x + 3, y + 1, theme.text);
        RenderUtils.textRight(graphics, valueText(), x + width - 4, y + 1, theme.accent);

        int trackY = y + height - TRACK - 3;
        int trackX = x + 3;
        int trackWidth = width - 6;
        RenderUtils.rect(graphics, trackX, trackY, trackWidth, TRACK, theme.sliderTrack);
        int filled = (int) (trackWidth * fraction());
        RenderUtils.rect(graphics, trackX, trackY, filled, TRACK, theme.accent);
        RenderUtils.outline(graphics, trackX, trackY, trackWidth, TRACK, theme.border);

        int knobX = trackX + Math.max(0, Math.min(trackWidth - 2, filled - 1));
        RenderUtils.rect(graphics, knobX, trackY - 1, 3, TRACK + 2, theme.text);
    }

    private void applyFromMouse(double mouseX) {
        int trackX = x + 3;
        int trackWidth = width - 6;
        double ratio = (mouseX - trackX) / trackWidth;
        ratio = Math.max(0.0, Math.min(1.0, ratio));
        setter.accept(min + (max - min) * ratio);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered(mouseX, mouseY) && button == 0) {
            dragging = true;
            applyFromMouse(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging) {
            applyFromMouse(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging) {
            dragging = false;
            return true;
        }
        return false;
    }
}
