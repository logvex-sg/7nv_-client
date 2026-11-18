package dev.sevennv.client.gui.widget;

import dev.sevennv.client.config.EnumSetting;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/** A dropdown-style row: label on the left, cycling value on the right. */
public final class EnumWidget extends SettingWidget {
    private final EnumSetting<?> setting;
    private final Theme theme;

    public EnumWidget(EnumSetting<?> setting, Theme theme, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.setting = setting;
        this.theme = theme;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        boolean hovered = hovered(mouseX, mouseY);
        RenderUtils.rect(graphics, x, y, width, height, hovered ? theme.hover : theme.panel);
        RenderUtils.text(graphics, setting.getName(), x + 3, y + (height - 8) / 2 + 1, theme.text);

        String value = setting.displayValue();
        int valueWidth = RenderUtils.font().width(value);
        int boxX = x + width - valueWidth - 10;
        RenderUtils.rect(graphics, boxX, y + 1, valueWidth + 8, height - 2, theme.sliderTrack);
        RenderUtils.text(graphics, value, boxX + 4, y + (height - 8) / 2 + 1, theme.accent);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!hovered(mouseX, mouseY)) {
            return false;
        }
        if (button == 0) {
            setting.cycle(1);
            return true;
        }
        if (button == 1) {
            setting.cycle(-1);
            return true;
        }
        return false;
    }
}
