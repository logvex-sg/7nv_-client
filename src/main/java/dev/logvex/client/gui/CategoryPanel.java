package dev.logvex.client.gui;

import dev.logvex.client.LogvexClient;
import dev.logvex.client.config.ConfigManager;
import dev.logvex.client.config.Theme;
import dev.logvex.client.gui.widget.SettingWidget;
import dev.logvex.client.gui.widget.WidgetFactory;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A draggable category window holding its modules. Modules expand in place to
 * reveal their settings, exactly like the classic 1.8 panels.
 */
public final class CategoryPanel {
    public static final int HEADER_HEIGHT = 14;
    private static final int MODULE_HEIGHT = 12;

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

    public CategoryPanel(Category category, List<Module> modules, int defaultX, int defaultY) {
        this.category = category;
        this.modules = modules;
        ConfigManager config = LogvexClient.getConfigManager();
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

    /** Total height for the current expansion state, used for hit testing. */
    public int getHeight() {
        if (collapsed) {
            return HEADER_HEIGHT;
        }
        int height = HEADER_HEIGHT + 2;
        for (Module module : visibleModules()) {
            height += MODULE_HEIGHT;
            if (module.isExpanded()) {
                height += settingsHeight(module);
            }
        }
        return height + 2;
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
        ConfigManager config = LogvexClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, String globalFilter) {
        setFilter(globalFilter);
        Theme theme = theme();
        int height = getHeight();

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);
        RenderUtils.rect(graphics, x, y, width, HEADER_HEIGHT, theme.header);
        RenderUtils.text(graphics, category.getDisplayName(), x + 4, y + 3, theme.text);
        RenderUtils.textRight(graphics, collapsed ? "+" : "-", x + width - 5, y + 3, theme.textDim);

        if (collapsed) {
            return;
        }

        int rowY = y + HEADER_HEIGHT + 2;
        for (Module module : visibleModules()) {
            boolean hovered = RenderUtils.hovered(mouseX, mouseY, x, rowY, width, MODULE_HEIGHT);
            RenderUtils.rect(graphics, x + 1, rowY, width - 2, MODULE_HEIGHT, hovered ? theme.hover : theme.panel);
            int indicator = module.isEnabled() ? theme.enabled : theme.disabled;
            RenderUtils.rect(graphics, x + 2, rowY + 2, 2, MODULE_HEIGHT - 4, indicator);
            RenderUtils.text(graphics, module.getName(), x + 7, rowY + 2, module.isEnabled() ? theme.text : theme.textDim);
            if (!module.getSettings().isEmpty()) {
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

    /** Returns true when the click was consumed. */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!RenderUtils.hovered(mouseX, mouseY, x, y, width, HEADER_HEIGHT)) {
            if (!RenderUtils.hovered(mouseX, mouseY, x, y, width, getHeight())) {
                return false;
            }
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

        int rowY = y + HEADER_HEIGHT + 2;
        for (Module module : visibleModules()) {
            if (RenderUtils.hovered(mouseX, mouseY, x, rowY, width, MODULE_HEIGHT)) {
                if (button == 0) {
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
                    if (widget.mouseClicked(mouseX, mouseY, button)) {
                        return true;
                    }
                    rowY += widget.getHeight() + 1;
                }
            }
        }
        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (dragging) {
            x = (int) mouseX - dragOffsetX;
            y = (int) mouseY - dragOffsetY;
            return true;
        }
        int rowY = y + HEADER_HEIGHT + 2;
        if (collapsed) {
            return false;
        }
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
            ConfigManager config = LogvexClient.getConfigManager();
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
        if (!RenderUtils.hovered(mouseX, mouseY, x, y, width, getHeight())) {
            return false;
        }
        int rowY = y + HEADER_HEIGHT + 2;
        if (collapsed) {
            return false;
        }
        for (Module module : visibleModules()) {
            rowY += MODULE_HEIGHT;
            if (module.isExpanded()) {
                for (SettingWidget widget : widgetsFor(module)) {
                    if (widget.mouseScrolled(mouseX, mouseY, amount)) {
                        return true;
                    }
                    rowY += widget.getHeight() + 1;
                }
            }
        }
        return false;
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
}
