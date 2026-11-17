package dev.logvex.client.misc;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.Theme;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.render.RenderUtils;
import dev.logvex.client.utils.MathUtils;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Session statistics panel: play time, blocks travelled, distance walked and
 * the number of modules enabled. Values are sampled once per tick and only
 * drawn, so the panel costs nothing on frames where it is hidden.
 */
public final class SessionStatistics extends Module {
    public SessionStatistics() {
        super("Session Statistics", "Shows play time and travel for this session", Category.MISC);
        showDistance = bool("Distance", "Show the distance travelled", true);
        showTicks = bool("Ticks", "Show the elapsed tick count", false);
        showModules = bool("Modules", "Show the enabled module count", true);
        showPlaytime = bool("Playtime", "Show the session duration", true);
    }

    private final BooleanSetting showDistance;
    private final BooleanSetting showTicks;
    private final BooleanSetting showModules;
    private final BooleanSetting showPlaytime;

    private long sessionStart = System.currentTimeMillis();
    private long ticks;
    private double lastX;
    private double lastY;
    private double lastZ;
    private boolean haveLast;
    private double distance;

    @Override
    public void onEnable() {
        sessionStart = System.currentTimeMillis();
        ticks = 0;
        distance = 0;
        haveLast = false;
    }

    @Override
    public void onWorldChange() {
        haveLast = false;
    }

    @Override
    public void onTick() {
        ticks++;
        if (mc.player == null) {
            return;
        }
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        if (haveLast) {
            distance += Math.sqrt((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY) + (z - lastZ) * (z - lastZ));
        }
        lastX = x;
        lastY = y;
        lastZ = z;
        haveLast = true;
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Theme theme = theme();
        int width = 138;
        int rows = (showPlaytime.get() ? 1 : 0) + (showDistance.get() ? 1 : 0)
                + (showTicks.get() ? 1 : 0) + (showModules.get() ? 1 : 0);
        int height = 4 + Math.max(1, rows) * 10;
        int x = graphics.guiWidth() - width - 4;
        int y = graphics.guiHeight() - height - 4;

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);

        int rowY = y + 2;
        if (showPlaytime.get()) {
            RenderUtils.text(graphics, "Playtime", x + 3, rowY, theme.textDim);
            RenderUtils.textRight(graphics, MathUtils.formatDuration(System.currentTimeMillis() - sessionStart),
                    x + width - 3, rowY, theme.text);
            rowY += 10;
        }
        if (showDistance.get()) {
            RenderUtils.text(graphics, "Distance", x + 3, rowY, theme.textDim);
            RenderUtils.textRight(graphics, (int) distance + "m", x + width - 3, rowY, theme.text);
            rowY += 10;
        }
        if (showTicks.get()) {
            RenderUtils.text(graphics, "Ticks", x + 3, rowY, theme.textDim);
            RenderUtils.textRight(graphics, String.valueOf(ticks), x + width - 3, rowY, theme.text);
            rowY += 10;
        }
        if (showModules.get()) {
            var modules = dev.logvex.client.LogvexClient.getModuleManager();
            int count = modules == null ? 0 : modules.getEnabledCount();
            RenderUtils.text(graphics, "Modules", x + 3, rowY, theme.textDim);
            RenderUtils.textRight(graphics, String.valueOf(count), x + width - 3, rowY, theme.text);
        }
    }

    public long sessionMillis() {
        return System.currentTimeMillis() - sessionStart;
    }

    public double distance() {
        return distance;
    }

    private Theme theme() {
        var config = dev.logvex.client.LogvexClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
