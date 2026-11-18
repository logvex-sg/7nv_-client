package dev.sevennv.client.gui.widget;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/** A checkbox row: label on the left, a small square tick box on the right. */
public final class BooleanWidget extends SettingWidget {
    private static final int BOX = 8;

    private final BooleanSetting setting;
    private final Theme theme;

    public BooleanWidget(BooleanSetting setting, Theme theme, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.setting = setting;
        this.theme = theme;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        boolean hovered = hovered(mouseX, mouseY);
        RenderUtils.rect(graphics, x, y, width, height, hovered ? theme.hover : theme.panel);
        RenderUtils.text(graphics, setting.getName(), x + 3, y + (height - 8) / 2 + 1, theme.text);

        int boxX = x + width - BOX - 4;
        int boxY = y + (height - BOX) / 2;
        RenderUtils.rect(graphics, boxX, boxY, BOX, BOX, theme.sliderTrack);
        RenderUtils.outline(graphics, boxX, boxY, BOX, BOX, theme.border);
        if (setting.get()) {
            RenderUtils.rect(graphics, boxX + 2, boxY + 2, BOX - 4, BOX - 4, theme.enabled);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered(mouseX, mouseY) && button == 0) {
            setting.toggle();
            return true;
        }
        return false;
    }
}
