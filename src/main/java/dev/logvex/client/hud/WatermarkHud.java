package dev.logvex.client.hud;

import dev.logvex.client.LogvexClient;
import dev.logvex.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/** The client name watermark, bottom-left by default. */
public final class WatermarkHud extends HudElement {
    public WatermarkHud() {
        super("watermark", "Watermark", 2, 194);
    }

    private String text() {
        return LogvexClient.MOD_NAME + " " + LogvexClient.VERSION;
    }

    @Override
    public int getWidth() {
        return RenderUtils.font().width(text()) + 4;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        int accent = LogvexClient.getConfigManager() == null
                ? 0xFF5FBF5F : LogvexClient.getConfigManager().getTheme().accent;
        int width = RenderUtils.font().width(text());
        RenderUtils.rect(graphics, x, y, width + 3, 10, background());
        RenderUtils.rect(graphics, x, y, 1, 10, accent);
        RenderUtils.text(graphics, text(), x + 2, y + 1, 0xFFE0E0E0);
    }
}
