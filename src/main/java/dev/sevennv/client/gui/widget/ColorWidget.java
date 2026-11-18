package dev.sevennv.client.gui.widget;

import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/**
 * A colour row. Clicking the swatch expands a small RGB picker with three
 * sliders and an alpha slider; clicking again collapses it.
 */
public final class ColorWidget extends SettingWidget {
    private static final String[] CHANNELS = {"R", "G", "B", "A"};
    private static final int SWATCH = 12;

    private final ColorSetting setting;
    private final Theme theme;
    private boolean expanded;
    private int activeChannel = -1;

    public ColorWidget(ColorSetting setting, Theme theme, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.setting = setting;
        this.theme = theme;
    }

    @Override
    public boolean isCapturingInput() {
        return activeChannel >= 0;
    }

    @Override
    public int getHeight() {
        return expanded ? height + 4 * 12 + 4 : height;
    }

    private int[] channels() {
        return new int[]{setting.red(), setting.green(), setting.blue(), (int) (setting.alpha() * 255f)};
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        RenderUtils.rect(graphics, x, y, width, height, hovered(mouseX, mouseY) ? theme.hover : theme.panel);
        RenderUtils.text(graphics, setting.getName(), x + 3, y + (height - 8) / 2 + 1, theme.text);

        int swatchX = x + width - SWATCH - 4;
        int swatchY = y + (height - SWATCH) / 2;
        RenderUtils.rect(graphics, swatchX, swatchY, SWATCH, SWATCH, setting.get());
        RenderUtils.outline(graphics, swatchX, swatchY, SWATCH, SWATCH, theme.border);

        if (!expanded) {
            return;
        }
        int[] values = channels();
        int rowY = y + height + 2;
        for (int channel = 0; channel < 4; channel++) {
            int trackX = x + 14;
            int trackWidth = width - 18;
            RenderUtils.text(graphics, CHANNELS[channel], x + 3, rowY + 1, theme.textDim);
            RenderUtils.rect(graphics, trackX, rowY + 2, trackWidth, 6, theme.sliderTrack);
            RenderUtils.rect(graphics, trackX, rowY + 2, (int) (trackWidth * values[channel] / 255f), 6, channelColor(channel));
            RenderUtils.outline(graphics, trackX, rowY + 2, trackWidth, 6, theme.border);
            rowY += 12;
        }
    }

    private int channelColor(int channel) {
        int[] values = channels();
        return switch (channel) {
            case 0 -> RenderUtils.argb(255, values[0], 0, 0);
            case 1 -> RenderUtils.argb(255, 0, values[1], 0);
            case 2 -> RenderUtils.argb(255, 0, 0, values[2]);
            default -> RenderUtils.argb(255, values[0], values[1], values[2]);
        };
    }

    private void applyChannel(double mouseX) {
        int trackX = x + 14;
        int trackWidth = width - 18;
        double ratio = Math.max(0.0, Math.min(1.0, (mouseX - trackX) / trackWidth));
        int value = (int) Math.round(ratio * 255.0);
        int[] values = channels();
        values[activeChannel] = value;
        setting.setChannels(values[3], values[0], values[1], values[2]);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        if (hovered(mouseX, mouseY)) {
            expanded = !expanded;
            return true;
        }
        if (expanded) {
            int rowY = y + height + 2;
            for (int channel = 0; channel < 4; channel++) {
                if (mouseX >= x + 14 && mouseX <= x + width - 4 && mouseY >= rowY && mouseY < rowY + 10) {
                    activeChannel = channel;
                    applyChannel(mouseX);
                    return true;
                }
                rowY += 12;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (expanded && activeChannel >= 0) {
            applyChannel(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (activeChannel >= 0) {
            activeChannel = -1;
            return true;
        }
        return false;
    }
}
