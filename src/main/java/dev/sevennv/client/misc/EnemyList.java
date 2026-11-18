package dev.sevennv.client.misc;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.render.RenderUtils;
import dev.sevennv.client.utils.FriendManager;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Enemy list overlay: shows the players marked as enemies, which the ESP and
 * targeting modules treat as priority. Enemies are managed with
 * {@code !enemy add <name>}.
 */
public final class EnemyList extends Module {
    public EnemyList() {
        super("Enemy List", "Shows your enemy list on screen", Category.MISC);
        maxRows = integer("Max rows", "Maximum enemies to list", 8, 1, 24);
        showNotes = bool("Notes", "Show the note attached to each enemy", true);
        showCount = bool("Count", "Show the total enemy count", true);
    }

    private final IntSetting maxRows;
    private final BooleanSetting showNotes;
    private final BooleanSetting showCount;

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Theme theme = theme();
        List<FriendManager.Entry> enemies = new ArrayList<>(FriendManager.getEnemies());
        int shown = Math.min(enemies.size(), maxRows.get());
        int width = 150;
        int height = 14 + Math.max(1, shown) * 9 + 2;

        int x = 4;
        int y = 4;
        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);

        String title = showCount.get() ? "Enemies (" + enemies.size() + ")" : "Enemies";
        RenderUtils.text(graphics, title, x + 3, y + 3, 0xFFFF6060);

        int rowY = y + 14;
        if (enemies.isEmpty()) {
            RenderUtils.text(graphics, "(none added)", x + 3, rowY, theme.textDim);
            return;
        }
        for (int i = 0; i < shown; i++) {
            FriendManager.Entry entry = enemies.get(i);
            RenderUtils.text(graphics, entry.name(), x + 3, rowY, theme.text);
            if (showNotes.get() && !entry.note().isEmpty()) {
                RenderUtils.textRight(graphics, entry.note(), x + width - 3, rowY, theme.textDim);
            }
            rowY += 9;
        }
    }

    private Theme theme() {
        var config = dev.sevennv.client.SevennvClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
