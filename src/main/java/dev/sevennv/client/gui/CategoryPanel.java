package dev.sevennv.client.gui;

import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.config.ConfigManager;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.gui.widget.SettingWidget;
import dev.sevennv.client.gui.widget.WidgetFactory;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A draggable category window holding its modules. Modules expand in place to
 * reveal their settings, exactly like the classic 1.8 panels.
 *
 * <p>The module list scrolls when it is taller than the window, so a category
 * with dozens of modules never runs off the bottom of the screen.
 */
public final class CategoryPanel {
    public static final int HEADER_HEIGHT = 14;
    private static final int MODULE_HEIGHT = 12;
    private static final int DEFAULT_MAX_CONTENT_HEIGHT = 240;
    private static final int SCROLL_STEP = 14;
    private static final Comparator<Module> BY_NAME =
            Comparator.comparing(module -> module.getName().toLowerCase(Locale.ROOT));

    private final Category category;
    private final List<Module> modules;
    private final Map<Module, List<SettingWidget>> widgets = new HashMap<>();

    private int x;
    private int y;
    private int width = 110;
    private boolean collapsed;
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;
    private String filter = "";
    private double scroll;
    private int screenWidth = 320;
    private int screenHeight = 240;
    private double guiScale = 1.0;
    private int maxContentHeight = DEFAULT_MAX_CONTENT_HEIGHT;

    public CategoryPanel(Category category, List<Module> modules, int defaultX, int defaultY) {
        this.category = category;
        this.modules = new ArrayList<>(modules);
        this.modules.sort(BY_NAME);
        ConfigManager config = SevennvClient.getConfigManager();
        int[] position = config == null ? new int[]{defaultX, defaultY}
                : config.getWindowPosition(category.name(), defaultX, defaultY);
        this.x = position[0];
        this.y = position[1];
    }

    public Category getCategory() {
        return category;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public void setFilter(String filter) {
        this.filter = filter == null ? "" : filter.toLowerCase(Locale.ROOT);
    }

    private List<Module> visibleModules() {
        if (filter.isEmpty()) {
            return modules;
        }
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getName().toLowerCase(Locale.ROOT).contains(filter)) {
                result.add(module);
            }
        }
        return result;
    }

    /** Height of the module list only, before the scroll limit is applied. */
    private int contentHeight() {
        int height = 0;
        for (Module module : visibleModules()) {
            height += MODULE_HEIGHT;
            if (module.isExpanded()) {
                height += settingsHeight(module);
            }
        }
        return height;
    }

    /** Visible height of the module list. */
    private int viewportHeight() {
        return Math.max(MODULE_HEIGHT, Math.min(maxContentHeight, contentHeight()));
    }

    /** Total height for the current expansion state, used for hit testing. */
    public int getHeight() {
        if (collapsed) {
            return HEADER_HEIGHT;
        }
        return HEADER_HEIGHT + 2 + viewportHeight() + 2;
    }

    private int maxScroll() {
        return Math.max(0, contentHeight() - viewportHeight());
    }

    private int settingsHeight(Module module) {
        int height = 0;
        for (SettingWidget widget : widgetsFor(module)) {
            height += widget.getHeight() + 1;
        }
        return height;
    }

    private List<SettingWidget> widgetsFor(Module module) {
        return widgets.computeIfAbsent(module, key -> {
            List<SettingWidget> list = new ArrayList<>();
            for (var setting : module.getSettings()) {
                if (!setting.isVisible()) {
                    continue;
                }
                SettingWidget widget = WidgetFactory.create(setting, theme(), 0, 0, width - 6);
                if (widget != null) {
                    list.add(widget);
                }
            }
            return list;
        });
    }

    private Theme theme() {
        ConfigManager config = SevennvClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, String globalFilter) {
        setFilter(globalFilter);
        Theme theme = theme();
        int height = getHeight();
        clampToScreen();

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);

        if (!collapsed) {
            int top = y + HEADER_HEIGHT + 2;
            int bottom = top + viewportHeight();
            // Scissor rectangles are in real screen space, but the panel is drawn
            // inside a scaled pose, so the rect has to be scaled up to match.
            int left = (int) Math.floor((x + 1) * guiScale);
            int right = (int) Math.ceil((x + width - 1) * guiScale);
            int topPx = (int) Math.floor(top * guiScale);
            int bottomPx = (int) Math.ceil(bottom * guiScale);
            graphics.enableScissor(left, topPx, Math.max(left + 1, right), Math.max(topPx + 1, bottomPx));
            renderModules(graphics, theme, mouseX, mouseY, top);
            graphics.disableScissor();
        }

        // Header last so the scrolled content never bleeds over it.
        RenderUtils.rect(graphics, x, y, width, HEADER_HEIGHT, theme.header);
        RenderUtils.outline(graphics, x, y, width, HEADER_HEIGHT, theme.border);
        RenderUtils.text(graphics, category.getDisplayName(), x + 4, y + 3, theme.text);
        RenderUtils.textRight(graphics, collapsed ? "+" : "-", x + width - 5, y + 3, theme.textDim);
    }

    private void renderModules(GuiGraphics graphics, Theme theme, int mouseX, int mouseY, int top) {
        int rowY = top - (int) scroll;
        for (Module module : visibleModules()) {
            boolean hovered = RenderUtils.hovered(mouseX, mouseY, x, rowY, width, MODULE_HEIGHT);
            RenderUtils.rect(graphics, x + 1, rowY, width - 2, MODULE_HEIGHT, hovered ? theme.hover : theme.panel);
            int indicator = module.isEnabled() ? theme.enabled : theme.disabled;
            RenderUtils.rect(graphics, x + 2, rowY + 2, 2, MODULE_HEIGHT - 4, indicator);
            RenderUtils.text(graphics, module.getName(), x + 7, rowY + 2, module.isEnabled() ? theme.text : theme.textDim);
            if (module.isAlwaysOn()) {
                // Locked marker: a performance module cannot be switched off.
                // The expand chevron still shows when it has tuning options.
                int right = x + width - 5;
                if (!module.getSettings().isEmpty()) {
                    RenderUtils.textRight(graphics, module.isExpanded() ? "-" : "+", right - 7, rowY + 2, theme.textDim);
                }
                RenderUtils.textRight(graphics, "\u00a72\u2713", right, rowY + 2, theme.enabled);
            } else if (!module.getSettings().isEmpty()) {
                RenderUtils.textRight(graphics, module.isExpanded() ? "-" : "+", x + width - 5, rowY + 2, theme.textDim);
            }
            rowY += MODULE_HEIGHT;

            if (module.isExpanded()) {
                for (SettingWidget widget : widgetsFor(module)) {
                    widget.setBounds(x + 3, rowY, width - 6, widget.getHeight());
                    widget.render(graphics, mouseX, mouseY);
                    rowY += widget.getHeight() + 1;
                }
            }
        }
    }

    /** Keeps the panel header inside the screen so a drag can never lose it. */
    private void clampToScreen() {
        x = Math.max(0, Math.min(x, Math.max(0, screenWidth - width)));
        y = Math.max(0, Math.min(y, Math.max(0, screenHeight - HEADER_HEIGHT)));
    }

    /** Returns true when the click was consumed. */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!RenderUtils.hovered(mouseX, mouseY, x, y, width, getHeight())) {
            return false;
        }
        if (RenderUtils.hovered(mouseX, mouseY, x, y, width, HEADER_HEIGHT)) {
            if (button == 0) {
                dragging = true;
                dragOffsetX = (int) mouseX - x;
                dragOffsetY = (int) mouseY - y;
                return true;
            }
            if (button == 1) {
                collapsed = !collapsed;
                return true;
            }
            return false;
        }

        if (collapsed) {
            return false;
        }

        int rowY = y + HEADER_HEIGHT + 2 - (int) scroll;
        for (Module module : visibleModules()) {
            if (isInViewport(mouseY) && RenderUtils.hovered(mouseX, mouseY, x, rowY, width, MODULE_HEIGHT)) {
                if (module.isAlwaysOn()) {
                    // Locked: left-click does nothing, right-click still opens
                    // the settings so the tuning options remain reachable.
                    if (button == 1 && !module.getSettings().isEmpty()) {
                        module.setExpanded(!module.isExpanded());
                    }
                } else if (button == 0) {
                    module.toggle();
                } else if (button == 1 && !module.getSettings().isEmpty()) {
                    module.setExpanded(!module.isExpanded());
                }
                return true;
            }
            rowY += MODULE_HEIGHT;

            if (module.isExpanded()) {
                for (SettingWidget widget : widgetsFor(module)) {
                    widget.setBounds(x + 3, rowY, width - 6, widget.getHeight());
                    if (isInViewport(mouseY) && widget.mouseClicked(mouseX, mouseY, button)) {
                        return true;
                    }
                    rowY += widget.getHeight() + 1;
                }
            }
        }
        return true;
    }

    private boolean isInViewport(double mouseY) {
        int top = y + HEADER_HEIGHT + 2;
        return mouseY >= top && mouseY <= top + viewportHeight();
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (dragging) {
            x = (int) mouseX - dragOffsetX;
            y = (int) mouseY - dragOffsetY;
            clampToScreen();
            return true;
        }
        if (collapsed) {
            return false;
        }
        int rowY = y + HEADER_HEIGHT + 2 - (int) scroll;
        for (Module module : visibleModules()) {
            rowY += MODULE_HEIGHT;
            if (module.isExpanded()) {
                for (SettingWidget widget : widgetsFor(module)) {
                    if (widget.mouseDragged(mouseX, mouseY, button, 0, 0)) {
                        return true;
                    }
                    rowY += widget.getHeight() + 1;
                }
            }
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging) {
            dragging = false;
            ConfigManager config = SevennvClient.getConfigManager();
            if (config != null) {
                config.setWindowPosition(category.name(), x, y);
            }
            return true;
        }
        for (List<SettingWidget> list : widgets.values()) {
            for (SettingWidget widget : list) {
                if (widget.mouseReleased(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (collapsed || !RenderUtils.hovered(mouseX, mouseY, x, y, width, getHeight())) {
            return false;
        }
        scroll -= amount * SCROLL_STEP;
        scroll = Math.max(0, Math.min(maxScroll(), scroll));
        return true;
    }

    /** Routes a key press to any widget currently capturing input. */
    public boolean keyPressed(int keyCode) {
        for (List<SettingWidget> list : widgets.values()) {
            for (SettingWidget widget : list) {
                if (widget.isCapturingInput() && widget.keyPressed(keyCode)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean charTyped(char character) {
        for (List<SettingWidget> list : widgets.values()) {
            for (SettingWidget widget : list) {
                if (widget.isCapturingInput() && widget.charTyped(character)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setScreenSize(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void setGuiScale(double guiScale) {
        this.guiScale = guiScale <= 0 ? 1.0 : guiScale;
    }

    /** Caps the visible list height so panels fit the window they are placed in. */
    public void setMaxHeight(int maxContentHeight) {
        this.maxContentHeight = Math.max(MODULE_HEIGHT * 2, maxContentHeight);
    }
}
