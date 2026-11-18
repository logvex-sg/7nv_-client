package dev.sevennv.client.hud;

import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.config.ConfigManager;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The classic array list: one right-aligned row per enabled module, sorted by
 * rendered width so the stack forms a clean triangle.
 */
public final class ArraylistHud extends HudElement {
    private static final Comparator<Module> BY_WIDTH = Comparator.comparingInt(
            (Module module) -> RenderUtils.font().width(label(module))).reversed();

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
        int max = 0;
        for (Module module : visibleModules()) {
            max = Math.max(max, RenderUtils.font().width(label(module)));
        }
        return max;
    }

    @Override
    public int getHeight() {
        return visibleModules().size() * 10;
    }

    private List<Module> visibleModules() {
        List<Module> result = new ArrayList<>();
        if (SevennvClient.getModuleManager() == null) {
            return result;
        }
        for (Module module : SevennvClient.getModuleManager().getEnabled()) {
            if (module.isVisibleInArraylist()) {
                result.add(module);
            }
        }
        result.sort(BY_WIDTH);
        return result;
    }

    @Override
    public void render(GuiGraphics graphics) {
        ConfigManager config = SevennvClient.getConfigManager();
        if (config == null) {
            return;
        }
        // The array list draws itself relative to the stored anchor corner.
        int screenWidth = HudData.mc().getWindow().getGuiScaledWidth();
        boolean rightAligned = config.getArraylistX() >= screenWidth / 2;
        List<Module> modules = visibleModules();
        int row = 0;
        for (Module module : modules) {
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
