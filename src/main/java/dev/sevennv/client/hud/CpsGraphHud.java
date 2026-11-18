package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import dev.sevennv.client.utils.CpsTracker;
import net.minecraft.client.gui.GuiGraphics;

/**
 * A rolling CPS bar graph. One sample is taken per tick and the history is a
 * fixed-size ring, so the element never allocates while rendering.
 */
public final class CpsGraphHud extends HudElement {
    private static final int SAMPLES = 40;
    private static final int BAR_WIDTH = 2;
    private static final int GRAPH_HEIGHT = 24;

    private final int[] history = new int[SAMPLES];
    private int cursor;
    private int tickCounter;

    public CpsGraphHud() {
        super("cpsgraph", "CPS Graph", 2, 156);
    }

    @Override
    public int getWidth() {
        return SAMPLES * BAR_WIDTH + 2;
    }

    @Override
    public int getHeight() {
        return GRAPH_HEIGHT + 12;
    }

    @Override
    public void render(GuiGraphics graphics) {
        if (++tickCounter >= 5) {
            tickCounter = 0;
            history[cursor] = CpsTracker.getCps();
            cursor = (cursor + 1) % SAMPLES;
        }
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        RenderUtils.text(graphics, "CPS " + CpsTracker.getCps(), x + 2, y + 1, 0xFFE0E0E0);
        int baseY = y + getHeight() - 1;
        int max = 1;
        for (int value : history) {
            max = Math.max(max, value);
        }
        for (int i = 0; i < SAMPLES; i++) {
            int value = history[(cursor + i) % SAMPLES];
            int barHeight = (int) ((value / (double) max) * (GRAPH_HEIGHT - 2));
            int barX = x + 1 + i * BAR_WIDTH;
            RenderUtils.rect(graphics, barX, baseY - barHeight, BAR_WIDTH - 1, barHeight, 0xFF55FF55);
        }
    }
}
