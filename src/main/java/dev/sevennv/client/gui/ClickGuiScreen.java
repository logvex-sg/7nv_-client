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
 * The ClickGUI: one compact, draggable panel per category, a search box and a
 * small toolbar. Deliberately flat and dense — solid panels, 1px borders, no
 * shadows, everything sized to fit a normal window without spilling off screen.
 */
public final class ClickGuiScreen extends Screen {
    private static final int TOOLBAR_HEIGHT = 14;
    private static final int PANEL_WIDTH = 106;
    private static final int PANEL_GAP = 6;
    private static final int PANEL_TOP = 24;
    private static final int BOTTOM_MARGIN = 18;
    private static final int SEARCH_WIDTH = 140;
    private static final int BUTTON_GAP = 4;

    private final List<CategoryPanel> panels = new ArrayList<>();
    private final List<ToolbarButton> toolbarButtons = new ArrayList<>();
    private String search = "";
    private boolean searchFocused;
    private int searchX;
    private int searchY;
    private int searchWidth = SEARCH_WIDTH;
    private double scale = 1.0;

    public ClickGuiScreen() {
        super(Component.literal("7nv_ ClickGUI"));
    }

    @Override
    protected void init() {
        panels.clear();
        toolbarButtons.clear();

        ConfigManager config = SevennvClient.getConfigManager();
        scale = config == null ? 1.0 : config.getGuiScaleFactor();
        int scaledWidth = (int) (width / scale);
        int scaledHeight = (int) (height / scale);

        layoutPanels(scaledWidth, scaledHeight);
        layoutToolbar(scaledWidth);
    }

    /** Places the category panels in a compact grid that fits the window. */
    private void layoutPanels(int scaledWidth, int scaledHeight) {
        int perRow = Math.max(1, (scaledWidth - 8) / (PANEL_WIDTH + PANEL_GAP));
        int available = scaledHeight - PANEL_TOP - BOTTOM_MARGIN;
        int rows = (Category.values().length + perRow - 1) / perRow;
        // Split the vertical space between the rows so the panels tile the screen.
        int rowHeight = Math.max(120, available / Math.max(1, rows));

        int index = 0;
        for (Category category : Category.values()) {
            int column = index % perRow;
            int row = index / perRow;
            int panelX = 4 + column * (PANEL_WIDTH + PANEL_GAP);
            int panelY = PANEL_TOP + row * rowHeight;
            List<dev.sevennv.client.module.Module> modules =
                    SevennvClient.getModuleManager().getByCategory(category);
            CategoryPanel panel = new CategoryPanel(category, modules, panelX, panelY);
            panel.setScreenSize(scaledWidth, scaledHeight);
            panel.setGuiScale(scale);
            panel.setMaxHeight(Math.max(60, rowHeight - 6));
            panels.add(panel);
            index++;
        }
    }

    private void layoutToolbar(int scaledWidth) {
        searchWidth = Math.min(SEARCH_WIDTH, Math.max(80, scaledWidth / 3));
        searchX = 4;
        searchY = 5;

        int buttonX = searchX + searchWidth + 6;
        int buttonY = 5;
        int buttonHeight = TOOLBAR_HEIGHT - 2;

        for (String preset : Theme.PRESETS) {
            int buttonWidth = RenderUtils.font().width(preset) + 8;
            if (buttonX + buttonWidth > scaledWidth - 60) {
                break;
            }
            toolbarButtons.add(new ToolbarButton(preset, preset, buttonX, buttonY, buttonWidth, buttonHeight));
            buttonX += buttonWidth + BUTTON_GAP;
        }
        int scaleMinus = RenderUtils.font().width("Scale -") + 8;
        int scalePlus = RenderUtils.font().width("Scale +") + 8;
        if (buttonX + scaleMinus + scalePlus + BUTTON_GAP <= scaledWidth - 4) {
            toolbarButtons.add(new ToolbarButton("Scale -", "Scale -", buttonX, buttonY, scaleMinus, buttonHeight));
            buttonX += scaleMinus + BUTTON_GAP;
            toolbarButtons.add(new ToolbarButton("Scale +", "Scale +", buttonX, buttonY, scalePlus, buttonHeight));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Theme theme = theme();
        RenderUtils.rect(graphics, 0, 0, width, height, RenderUtils.withAlpha(theme.background, 0xC0));

        var pose = graphics.pose();
        pose.pushMatrix();
        pose.scale((float) scale, (float) scale);

        int scaledMouseX = (int) (mouseX / scale);
        int scaledMouseY = (int) (mouseY / scale);

        renderToolbar(graphics, theme, scaledMouseX, scaledMouseY);
        for (CategoryPanel panel : panels) {
            panel.render(graphics, scaledMouseX, scaledMouseY, search);
        }

        RenderUtils.text(graphics, "7nv_ Client  \u2022  Right Shift to close  \u2022  Scale "
                + Math.round(scale * 100) + "%  \u2022  " + enabledCount() + " on",
                6, (int) (height / scale) - 11, theme.textDim);

        pose.popMatrix();
    }

    private int enabledCount() {
        return SevennvClient.getModuleManager() == null
                ? 0 : SevennvClient.getModuleManager().getEnabled().size();
    }

    private void renderToolbar(GuiGraphics graphics, Theme theme, int mouseX, int mouseY) {
        RenderUtils.rect(graphics, 0, 0, (int) (width / scale), TOOLBAR_HEIGHT, theme.header);
        RenderUtils.rect(graphics, 0, TOOLBAR_HEIGHT, (int) (width / scale), 1, theme.border);

        boolean hovered = RenderUtils.hovered(mouseX, mouseY, searchX, searchY, searchWidth, TOOLBAR_HEIGHT - 2);
        RenderUtils.rect(graphics, searchX, searchY, searchWidth, TOOLBAR_HEIGHT - 2, theme.panel);
        RenderUtils.outline(graphics, searchX, searchY, searchWidth, TOOLBAR_HEIGHT - 2,
                searchFocused ? theme.accent : theme.border);
        boolean empty = search.isEmpty() && !searchFocused;
        RenderUtils.text(graphics, empty ? "\u00a78Search..." : search, searchX + 4, searchY + 3,
                empty ? theme.textDim : theme.text);
        if (searchFocused && (System.currentTimeMillis() / 500L) % 2 == 0) {
            int caretX = searchX + 4 + RenderUtils.font().width(search);
            RenderUtils.rect(graphics, caretX, searchY + 3, 1, 8, theme.text);
        }

        for (ToolbarButton button : toolbarButtons) {
            button.render(graphics, theme, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x() / scale;
        double mouseY = event.y() / scale;
        int button = event.button();

        if (button == 0 && RenderUtils.hovered(mouseX, mouseY, searchX, searchY, searchWidth, TOOLBAR_HEIGHT - 2)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        if (mouseY <= TOOLBAR_HEIGHT) {
            for (ToolbarButton toolbarButton : toolbarButtons) {
                if (toolbarButton.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
            return true;
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

    /** A small toolbar button: a theme preset or a scale nudge. */
    private record ToolbarButton(String label, String action, int x, int y, int width, int height) {
        void render(GuiGraphics graphics, Theme theme, int mouseX, int mouseY) {
            ConfigManager config = SevennvClient.getConfigManager();
            boolean selected = !action.startsWith("Scale")
                    && config != null && action.equalsIgnoreCase(config.getThemePreset());
            boolean hovered = RenderUtils.hovered(mouseX, mouseY, x, y, width, height);
            RenderUtils.rect(graphics, x, y, width, height,
                    selected ? theme.highlight : hovered ? theme.hover : theme.panel);
            RenderUtils.outline(graphics, x, y, width, height, selected ? theme.accent : theme.border);
            RenderUtils.text(graphics, label, x + 4, y + 3, selected ? theme.accent : theme.text);
        }

        boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0 || !RenderUtils.hovered(mouseX, mouseY, x, y, width, height)) {
                return false;
            }
            ConfigManager config = SevennvClient.getConfigManager();
            if (config == null) {
                return true;
            }
            if (action.equals("Scale -")) {
                config.setGuiScaleFactor(config.getGuiScaleFactor() - 0.25);
            } else if (action.equals("Scale +")) {
                config.setGuiScaleFactor(config.getGuiScaleFactor() + 0.25);
            } else {
                config.applyTheme(action);
            }
            return true;
        }
    }
}
