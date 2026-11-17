package dev.logvex.client.movement;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.Theme;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.render.RenderUtils;
import dev.logvex.client.utils.WorldUtils;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Movement readout: horizontal speed, vertical speed and the current
 * horizontal heading. All values are derived from the player's delta movement
 * each frame, so nothing is cached.
 */
public final class MovementHud extends Module {
    public MovementHud() {
        super("Movement HUD", "Shows your speed and vertical motion", Category.MOVEMENT);
        showHorizontal = bool("Horizontal", "Show horizontal speed", true);
        showVertical = bool("Vertical", "Show vertical speed", true);
        showHeading = bool("Heading", "Show the current heading", true);
    }

    private final BooleanSetting showHorizontal;
    private final BooleanSetting showVertical;
    private final BooleanSetting showHeading;

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        if (mc.player == null) {
            return;
        }
        Theme theme = theme();
        int width = 110;
        int height = 26;
        int x = 4;
        int y = graphics.guiHeight() - height - 30;

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);

        double dx = mc.player.getX() - mc.player.xOld;
        double dz = mc.player.getZ() - mc.player.zOld;
        double horizontal = Math.sqrt(dx * dx + dz * dz) * 20.0;
        double vertical = (mc.player.getY() - mc.player.yOld) * 20.0;

        int rowY = y + 3;
        if (showHorizontal.get()) {
            RenderUtils.text(graphics, String.format(java.util.Locale.ROOT, "H-Speed %.1f", horizontal),
                    x + 3, rowY, theme.text);
            rowY += 8;
        }
        if (showVertical.get()) {
            RenderUtils.text(graphics, String.format(java.util.Locale.ROOT, "V-Speed %.1f", vertical),
                    x + 3, rowY, theme.text);
            rowY += 8;
        }
        if (showHeading.get()) {
            RenderUtils.text(graphics, "Facing " + WorldUtils.compass(mc.player.getYRot()), x + 3, rowY, theme.textDim);
        }
    }

    private Theme theme() {
        var config = dev.logvex.client.LogvexClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
