package dev.sevennv.client.gui.widget;

import com.mojang.blaze3d.platform.InputConstants;
import dev.sevennv.client.config.KeybindSetting;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

/** A keybind row: click to capture the next key, right-click to clear. */
public final class KeybindWidget extends SettingWidget {
    private final KeybindSetting setting;
    private final Theme theme;
    private boolean listening;

    public KeybindWidget(KeybindSetting setting, Theme theme, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.setting = setting;
        this.theme = theme;
    }

    public boolean isListening() {
        return listening;
    }

    @Override
    public boolean isCapturingInput() {
        return listening;
    }

    public void stopListening() {
        listening = false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        RenderUtils.rect(graphics, x, y, width, height, hovered(mouseX, mouseY) ? theme.hover : theme.panel);
        RenderUtils.text(graphics, setting.getName(), x + 3, y + (height - 8) / 2 + 1, theme.text);
        String value = listening ? "PRESS KEY" : setting.displayValue();
        int color = listening ? theme.accent : (setting.isBound() ? theme.text : theme.textDim);
        RenderUtils.textRight(graphics, value, x + width - 4, y + (height - 8) / 2 + 1, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!hovered(mouseX, mouseY)) {
            return false;
        }
        if (button == 0) {
            listening = true;
            return true;
        }
        if (button == 1) {
            setting.set(InputConstants.UNKNOWN);
            listening = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode) {
        if (!listening) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            setting.set(InputConstants.UNKNOWN);
            listening = false;
            return true;
        }
        setting.set(InputConstants.Type.KEYSYM.getOrCreate(keyCode));
        listening = false;
        return true;
    }
}
