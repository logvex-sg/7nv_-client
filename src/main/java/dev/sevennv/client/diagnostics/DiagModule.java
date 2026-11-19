package dev.sevennv.client.diagnostics;

import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Base for the diagnostics overlays.
 *
 * <p>Every diagnostics module used to draw its panel at a hardcoded {@code 4,4},
 * so enabling two of them stacked the panels on top of each other and made both
 * unreadable. This base adds per-module {@code X} and {@code Y} settings and
 * renders through {@link #drawPanel}, so each overlay can be moved out of the
 * way independently.
 */
public abstract class DiagModule extends Module {
    private final IntSetting panelX;
    private final IntSetting panelY;

    protected DiagModule(String name, String description) {
        super(name, description, Category.DIAGNOSTICS);
        panelX = integer("X", "Panel distance from the left edge", DiagPanel.DEFAULT_X, 0, 2000);
        panelY = integer("Y", "Panel distance from the top edge", DiagPanel.DEFAULT_Y, 0, 2000);
    }

    protected final int panelX() {
        return panelX.get();
    }

    protected final int panelY() {
        return panelY.get();
    }

    /**
     * Delta from the default X. Panels that anchor to another corner (the right
     * edge, the vertical centre) add this so their default look is unchanged but
     * they can still be nudged.
     */
    protected final int offsetX() {
        return panelX.get() - DiagPanel.DEFAULT_X;
    }

    protected final int offsetY() {
        return panelY.get() - DiagPanel.DEFAULT_Y;
    }

    /** Draws a panel at this module's configured position. */
    protected final void drawPanel(GuiGraphics graphics, DiagPanel panel) {
        panel.render(graphics, theme(), panelX(), panelY());
    }

    protected final Theme theme() {
        var config = dev.sevennv.client.SevennvClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
