package dev.sevennv.client.hud;

import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/** The client name watermark, bottom-left by default. */
public final class WatermarkHud extends HudElement {
    public WatermarkHud() {
        super("watermark", "Watermark", 110, 112);
    }

    private String text() {
        return SevennvClient.MOD_NAME + " " + SevennvClient.VERSION;
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
        int accent = SevennvClient.getConfigManager() == null
                ? 0xFF5FBF5F : SevennvClient.getConfigManager().getTheme().accent;
        int width = RenderUtils.font().width(text());
        RenderUtils.rect(graphics, x, y, width + 3, 10, background());
        RenderUtils.rect(graphics, x, y, 1, 10, accent);
        RenderUtils.text(graphics, text(), x + 2, y + 1, 0xFFE0E0E0);
    }
}
