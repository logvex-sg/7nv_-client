package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import dev.sevennv.client.utils.ReachTracker;
import net.minecraft.client.gui.GuiGraphics;

/** Current effective block and entity interaction range. */
public final class ReachHud extends HudElement {
    public ReachHud() {
        super("reach", "Reach", 2, 68);
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
        return String.format(java.util.Locale.ROOT, "Reach %.1f / %.1f",
                ReachTracker.blockReach(), ReachTracker.entityReach());
    }
}
