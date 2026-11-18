package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import dev.sevennv.client.utils.MathUtils;
import net.minecraft.client.gui.GuiGraphics;

/** How long the current session has been running. */
public final class SessionTimerHud extends HudElement {
    private final long started = System.currentTimeMillis();

    public SessionTimerHud() {
        super("sessiontimer", "Session Clock", 2, 145);
    }

    @Override
    public int getWidth() {
        return RenderUtils.font().width(line()) + 4;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        RenderUtils.text(graphics, line(), x + 2, y + 1, 0xFFE0E0E0);
    }

    private String line() {
        return "Session " + MathUtils.formatDuration(System.currentTimeMillis() - started);
    }
}
