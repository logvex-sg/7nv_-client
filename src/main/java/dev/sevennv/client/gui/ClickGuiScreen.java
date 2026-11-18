package dev.sevennv.client.gui;

import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.config.ConfigManager;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The ClickGUI: one draggable panel per category, a search box and a theme
 * selector. Deliberately flat — solid panels, 1px borders, no shadows.
 */
public final class ClickGuiScreen extends Screen {
    private static final int SEARCH_HEIGHT = 14;

    private final List<CategoryPanel> panels = new ArrayList<>();
    private final List<ThemeButton> themeButtons = new ArrayList<>();
    private String search = "";
    private boolean searchFocused;
    private int searchX;
    private int searchY;
    private int searchWidth = 150;
    private double scale = 1.0;

    public ClickGuiScreen() {
        super(Component.literal("7nv_ ClickGUI"));
    }

    @Override
    protected void init() {
        panels.clear();
        themeButtons.clear();

        ConfigManager config = SevennvClient.getConfigManager();
        scale = config == null ? 1.0 : config.getGuiScaleFactor();

        int columnWidth = 120;
        int columnGap = 8;
        int perRow = Math.max(1, (int) ((width / scale) - 20) / (columnWidth + columnGap));
        int index = 0;
        for (Category category : Category.values()) {
            int column = index % perRow;
            int row = index / perRow;
            int panelX = 10 + column * (columnWidth + columnGap);
            int panelY = 30 + row * 130;
            List<dev.sevennv.client.module.Module> modules =
                    SevennvClient.getModuleManager().getByCategory(category);
            panels.add(new CategoryPanel(category, modules, panelX, panelY));
            index++;
        }

        searchWidth = Math.min(180, (int) (width / scale) - 20);
        searchX = 10;
        searchY = 10;

        int buttonX = searchX + searchWidth + 8;
        for (String preset : Theme.PRESETS) {
            themeButtons.add(new ThemeButton(preset, buttonX, 10, RenderUtils.font().width(preset) + 8, SEARCH_HEIGHT));
            buttonX += RenderUtils.font().width(preset) + 12;
        }
        themeButtons.add(new ThemeButton("Scale -", buttonX, 10, RenderUtils.font().width("Scale -") + 8, SEARCH_HEIGHT));
        buttonX += RenderUtils.font().width("Scale -") + 12;
        themeButtons.add(new ThemeButton("Scale +", buttonX, 10, RenderUtils.font().width("Scale +") + 8, SEARCH_HEIGHT));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Theme theme = theme();
        RenderUtils.rect(graphics, 0, 0, width, height, RenderUtils.withAlpha(theme.background, 0xB0));

        var pose = graphics.pose();
        pose.pushMatrix();
        pose.scale((float) scale, (float) scale);

        int scaledMouseX = (int) (mouseX / scale);
        int scaledMouseY = (int) (mouseY / scale);

        renderSearch(graphics, theme, scaledMouseX, scaledMouseY);
        for (ThemeButton button : themeButtons) {
            button.render(graphics, theme, scaledMouseX, scaledMouseY);
        }
        for (CategoryPanel panel : panels) {
            panel.render(graphics, scaledMouseX, scaledMouseY, search);
        }
        RenderUtils.text(graphics, "7nv_ Client  \u2022  Right Shift to close  \u2022  Scale "
                + Math.round(scale * 100) + "%", 10, (int) (height / scale) - 12, theme.textDim);

        pose.popMatrix();
    }

    private void renderSearch(GuiGraphics graphics, Theme theme, int mouseX, int mouseY) {
        boolean hovered = RenderUtils.hovered(mouseX, mouseY, searchX, searchY, searchWidth, SEARCH_HEIGHT);
        RenderUtils.rect(graphics, searchX, searchY, searchWidth, SEARCH_HEIGHT, theme.panel);
        RenderUtils.outline(graphics, searchX, searchY, searchWidth, SEARCH_HEIGHT,
                searchFocused ? theme.accent : theme.border);
        String display = search.isEmpty() && !searchFocused ? "\u00a78Search modules..." : search;
        RenderUtils.text(graphics, display, searchX + 4, searchY + 3, search.isEmpty() && !searchFocused ? theme.textDim : theme.text);
        if (searchFocused && (System.currentTimeMillis() / 500L) % 2 == 0) {
            int caretX = searchX + 4 + RenderUtils.font().width(search);
            RenderUtils.rect(graphics, caretX, searchY + 3, 1, 8, theme.text);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x() / scale;
        double mouseY = event.y() / scale;
        int button = event.button();

        if (RenderUtils.hovered(mouseX, mouseY, searchX, searchY, searchWidth, SEARCH_HEIGHT) && button == 0) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        for (ThemeButton themeButton : themeButtons) {
            if (themeButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        for (CategoryPanel panel : panels) {
            if (panel.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        for (CategoryPanel panel : panels) {
            panel.mouseReleased(event.x() / scale, event.y() / scale, event.button());
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        for (CategoryPanel panel : panels) {
            if (panel.mouseDragged(event.x() / scale, event.y() / scale, event.button())) {
                return true;
            }
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (CategoryPanel panel : panels) {
            if (panel.mouseScrolled(mouseX / scale, mouseY / scale, scrollY)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();

        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchFocused = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
                search = search.substring(0, search.length() - 1);
                dev.sevennv.client.misc.ModuleSearch.setSharedQuery(search);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }
            return true;
        }

        for (CategoryPanel panel : panels) {
            if (panel.keyPressed(keyCode)) {
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (searchFocused) {
            char character = (char) event.codepoint();
            if (character >= ' ' && search.length() < 32) {
                search += character;
                dev.sevennv.client.misc.ModuleSearch.setSharedQuery(search);
            }
            return true;
        }
        for (CategoryPanel panel : panels) {
            if (panel.charTyped((char) event.codepoint())) {
                return true;
            }
        }
        return super.charTyped(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    private Theme theme() {
        ConfigManager config = SevennvClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }

    private record ThemeButton(String preset, int x, int y, int width, int height) {
        void render(GuiGraphics graphics, Theme theme, int mouseX, int mouseY) {
            boolean selected = !preset.startsWith("Scale") && preset.equalsIgnoreCase(currentPresetName(theme));
            boolean hovered = RenderUtils.hovered(mouseX, mouseY, x, y, width, height);
            RenderUtils.rect(graphics, x, y, width, height, selected ? theme.highlight : hovered ? theme.hover : theme.panel);
            RenderUtils.outline(graphics, x, y, width, height, selected ? theme.accent : theme.border);
            RenderUtils.text(graphics, preset, x + 4, y + 3, selected ? theme.accent : theme.text);
        }

        boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0 || !RenderUtils.hovered(mouseX, mouseY, x, y, width, height)) {
                return false;
            }
            ConfigManager config = SevennvClient.getConfigManager();
            if (config == null) {
                return true;
            }
            if (preset.equals("Scale -")) {
                config.setGuiScaleFactor(config.getGuiScaleFactor() - 0.25);
            } else if (preset.equals("Scale +")) {
                config.setGuiScaleFactor(config.getGuiScaleFactor() + 0.25);
            } else {
                config.applyTheme(preset);
            }
            return true;
        }

        private static String currentPresetName(Theme theme) {
            ConfigManager config = SevennvClient.getConfigManager();
            return config == null ? Theme.PRESETS[0] : config.getThemePreset();
        }
    }
}
