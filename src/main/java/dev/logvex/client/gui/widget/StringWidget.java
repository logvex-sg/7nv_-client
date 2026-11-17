package dev.logvex.client.gui.widget;

import dev.logvex.client.config.StringSetting;
import dev.logvex.client.config.Theme;
import dev.logvex.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

/** A text input row. Click to focus, type to edit, Enter or click away to commit. */
public final class StringWidget extends SettingWidget {
    private final StringSetting setting;
    private final Theme theme;
    private boolean focused;
    private String draft;

    public StringWidget(StringSetting setting, Theme theme, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.setting = setting;
        this.theme = theme;
        this.draft = setting.get();
    }

    public boolean isFocused() {
        return focused;
    }

    public void unfocus() {
        if (focused) {
            setting.set(draft);
            focused = false;
        }
    }

    public boolean isCapturingInput() {
        return focused;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.text(graphics, setting.getName(), x + 3, y + 1, theme.textDim);
        String shown = focused ? draft : setting.get();
        int boxX = x + 3;
        int boxY = y + height - 12;
        RenderUtils.rect(graphics, boxX, boxY, width - 6, 11, theme.sliderTrack);
        RenderUtils.outline(graphics, boxX, boxY, width - 6, 11, focused ? theme.accent : theme.border);
        RenderUtils.text(graphics, trim(shown), boxX + 2, boxY + 2, theme.text);
        if (focused && (System.currentTimeMillis() / 500L) % 2 == 0) {
            int caretX = boxX + 2 + RenderUtils.font().width(trim(shown));
            RenderUtils.rect(graphics, caretX, boxY + 2, 1, 8, theme.text);
        }
    }

    private String trim(String value) {
        int max = width - 12;
        if (RenderUtils.font().width(value) <= max) {
            return value;
        }
        String result = value;
        while (!result.isEmpty() && RenderUtils.font().width(result) > max) {
            result = result.substring(1);
        }
        return result;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean inside = hovered(mouseX, mouseY);
        if (inside && button == 0) {
            focused = true;
            draft = setting.get();
            return true;
        }
        if (focused) {
            unfocus();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode) {
        if (!focused) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            unfocus();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            draft = setting.get();
            focused = false;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !draft.isEmpty()) {
            draft = draft.substring(0, draft.length() - 1);
            return true;
        }
        return true;
    }

    @Override
    public boolean charTyped(char character) {
        if (!focused) {
            return false;
        }
        if (character >= ' ' && draft.length() < 128) {
            draft += character;
        }
        return true;
    }
}
