package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/** Session uptime, formatted as H:MM:SS. */
public final class SessionHud extends HudElement {
    private final long started = System.currentTimeMillis();

    public SessionHud() {
        super("session", "Session Timer", 110, 90);
    }

    @Override
    public int getWidth() {
        return RenderUtils.font().width(text()) + 4;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    private String text() {
        return "Session " + dev.sevennv.client.utils.MathUtils.formatDuration(System.currentTimeMillis() - started);
    }

    @Override
    public void render(GuiGraphics graphics) {
        drawLine(graphics, text(), 0xFFE0E0E0);
    }
}
