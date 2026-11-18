package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import dev.sevennv.client.utils.TpsTracker;
import net.minecraft.client.gui.GuiGraphics;

/** Milliseconds-per-tick estimate and a simple lag indicator. */
public final class LagHud extends HudElement {
    public LagHud() {
        super("lag", "Lag Meter", 2, 46);
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
        return String.format(java.util.Locale.ROOT, "MSPT %.0f", TpsTracker.getMspt());
    }
}
