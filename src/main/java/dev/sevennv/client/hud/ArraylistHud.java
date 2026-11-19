package dev.sevennv.client.hud;

import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.config.ConfigManager;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.module.ModuleManager;
import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The classic array list: one right-aligned row per enabled module, sorted by
 * rendered width so the stack forms a clean triangle.
 *
 * <p>The element is asked for its width and height several times per frame by
 * the editor and the renderer, so the visible rows are cached: they are rebuilt
 * only when the enabled set changes (signalled by
 * {@link ModuleManager#getEnabledVersion()}) or the suffix setting flips.
 */
public final class ArraylistHud extends HudElement {
    private static final Comparator<Module> BY_WIDTH = Comparator.comparingInt(
            (Module module) -> RenderUtils.font().width(label(module))).reversed();

    private final List<Module> visible = new ArrayList<>();
    private int cachedVersion = -1;
    private boolean cachedSuffix;
    private int cachedWidth;
    private int cachedHeight;

    public ArraylistHud() {
        super("arraylist", "Array List", 0, 0);
    }

    // The array list anchor lives in the config (so it can be positioned from
    // the old array list X/Y settings too); keep the editor in sync with it.
    @Override
    public int getX() {
        ConfigManager config = SevennvClient.getConfigManager();
        return config == null ? x : config.getArraylistX();
    }

    @Override
    public int getY() {
        ConfigManager config = SevennvClient.getConfigManager();
        return config == null ? y : config.getArraylistY();
    }

    @Override
    public void setPosition(int newX, int newY) {
        super.setPosition(newX, newY);
        ConfigManager config = SevennvClient.getConfigManager();
        if (config != null) {
            config.setArraylistPosition(newX, newY);
        }
    }

    private static String label(Module module) {
        ConfigManager config = SevennvClient.getConfigManager();
        if (config != null && config.isArraylistSuffix()) {
            return module.getArraylistLabel();
        }
        return module.getName();
    }

    @Override
    public int getWidth() {
        refresh();
        return cachedWidth;
    }

    @Override
    public int getHeight() {
        refresh();
        return cachedHeight;
    }

    /** Rebuilds the cached rows only when the enabled set or suffix mode changed. */
    private void refresh() {
        ConfigManager config = SevennvClient.getConfigManager();
        boolean suffix = config != null && config.isArraylistSuffix();
        ModuleManager manager = SevennvClient.getModuleManager();
        int version = manager == null ? -1 : manager.getEnabledVersion();
        if (version == cachedVersion && suffix == cachedSuffix) {
            return;
        }
        cachedVersion = version;
        cachedSuffix = suffix;
        visible.clear();
        int max = 0;
        if (manager != null) {
            for (Module module : manager.getEnabled()) {
                if (module.isVisibleInArraylist()) {
                    visible.add(module);
                    max = Math.max(max, RenderUtils.font().width(label(module)));
                }
            }
            visible.sort(BY_WIDTH);
        }
        cachedWidth = max;
        cachedHeight = visible.size() * 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        ConfigManager config = SevennvClient.getConfigManager();
        if (config == null) {
            return;
        }
        refresh();
        // The array list draws itself relative to the stored anchor corner.
        int screenWidth = HudData.mc().getWindow().getGuiScaledWidth();
        boolean rightAligned = config.getArraylistX() >= screenWidth / 2;
        int row = 0;
        for (Module module : visible) {
            String text = label(module);
            int width = RenderUtils.font().width(text);
            int drawX = rightAligned ? config.getArraylistX() - width - 2 : config.getArraylistX();
            int drawY = config.getArraylistY() + row * 10;
            int accent = config.isArraylistRainbow()
                    ? RenderUtils.rainbow(row, 4000, 255)
                    : config.getTheme().accent;
            RenderUtils.rect(graphics, drawX - 1, drawY, width + 2, 10, 0x90000000);
            RenderUtils.rect(graphics, rightAligned ? drawX + width + 1 : drawX - 1, drawY, 1, 10, accent);
            RenderUtils.text(graphics, text, drawX, drawY + 1, 0xFFE0E0E0);
            row++;
        }
    }

    @Override
    public void renderEditor(GuiGraphics graphics, int themeText, int themeBorder, boolean selected) {
        ConfigManager config = SevennvClient.getConfigManager();
        if (config == null) {
            return;
        }
        int screenWidth = HudData.mc().getWindow().getGuiScaledWidth();
        boolean rightAligned = config.getArraylistX() >= screenWidth / 2;
        int width = Math.max(getWidth(), 40);
        int drawX = rightAligned ? config.getArraylistX() - width : config.getArraylistX();
        RenderUtils.rect(graphics, drawX - 1, config.getArraylistY() - 1, width + 3, Math.max(getHeight(), 10) + 2, 0x40000000);
        RenderUtils.outline(graphics, drawX - 1, config.getArraylistY() - 1, width + 3, Math.max(getHeight(), 10) + 2,
                selected ? 0xFFFFFFFF : themeBorder);
        RenderUtils.text(graphics, "Array List", drawX, config.getArraylistY() + Math.max(getHeight(), 10) + 2, themeText);
    }
}
