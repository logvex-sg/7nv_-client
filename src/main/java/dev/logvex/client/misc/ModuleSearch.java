package dev.logvex.client.misc;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.Theme;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Live module search overlay: type into the ClickGUI search box, or hold this
 * module's keybind, to see matching modules without opening the full GUI. The
 * query is shared with the ClickGUI search so both stay in sync.
 */
public final class ModuleSearch extends Module {
    private static String sharedQuery = "";

    public ModuleSearch() {
        super("Module Search", "Shows modules matching the search query", Category.MISC);
        maxResults = integer("Max results", "How many matches to list", 8, 1, 24);
        showCategory = bool("Category", "Show each match's category", true);
        showState = bool("State", "Show whether the module is enabled", true);
    }

    private final dev.logvex.client.config.IntSetting maxResults;
    private final BooleanSetting showCategory;
    private final BooleanSetting showState;

    public static String getSharedQuery() {
        return sharedQuery;
    }

    public static void setSharedQuery(String query) {
        sharedQuery = query == null ? "" : query;
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        if (sharedQuery.isBlank()) {
            return;
        }
        Theme theme = theme();
        String query = sharedQuery.toLowerCase(Locale.ROOT);

        List<dev.logvex.client.module.Module> matches = new ArrayList<>();
        var manager = dev.logvex.client.LogvexClient.getModuleManager();
        if (manager == null) {
            return;
        }
        for (dev.logvex.client.module.Module module : manager.getAll()) {
            if (module.getName().toLowerCase(Locale.ROOT).contains(query)) {
                matches.add(module);
                if (matches.size() >= maxResults.get()) {
                    break;
                }
            }
        }

        int width = 168;
        int height = 14 + Math.max(1, matches.size()) * 9 + 2;
        int x = graphics.guiWidth() / 2 - width / 2;
        int y = 4;

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);
        RenderUtils.text(graphics, "Search: " + sharedQuery, x + 3, y + 3, theme.accent);

        int rowY = y + 14;
        if (matches.isEmpty()) {
            RenderUtils.text(graphics, "(no matches)", x + 3, rowY, theme.textDim);
            return;
        }
        for (dev.logvex.client.module.Module module : matches) {
            int color = !showState.get() || module.isEnabled() ? theme.text : theme.textDim;
            RenderUtils.text(graphics, module.getName(), x + 3, rowY, color);
            if (showCategory.get()) {
                RenderUtils.textRight(graphics, module.getCategory().getDisplayName(), x + width - 3, rowY, theme.textDim);
            }
            rowY += 9;
        }
    }

    private Theme theme() {
        var config = dev.logvex.client.LogvexClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
