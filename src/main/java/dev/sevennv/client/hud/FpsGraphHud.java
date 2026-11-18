package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/**
 * A rolling frame-rate graph. One sample is taken every few frames and the
 * history is a fixed-size ring, so drawing never allocates and the element
 * stays cheap on the very frames it is meant to measure.
 */
public final class FpsGraphHud extends HudElement {
    private static final int SAMPLES = 60;
    private static final int BAR_WIDTH = 2;
    private static final int GRAPH_HEIGHT = 26;

    private final int[] history = new int[SAMPLES];
    private int cursor;
    private int tickCounter;

    public FpsGraphHud() {
        super("fpsgraph", "FPS Graph", 2, 156);
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
            history[cursor] = HudData.mc().getFps();
            cursor = (cursor + 1) % SAMPLES;
        }
        int fps = HudData.mc().getFps();
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        RenderUtils.text(graphics, "FPS " + fps, x + 2, y + 1, 0xFFE0E0E0);
        int baseY = y + getHeight() - 1;
        int max = 1;
        for (int value : history) {
            max = Math.max(max, value);
        }
        for (int i = 0; i < SAMPLES; i++) {
            int value = history[(cursor + i) % SAMPLES];
            int barHeight = (int) ((value / (double) max) * (GRAPH_HEIGHT - 2));
            int barX = x + 1 + i * BAR_WIDTH;
            int color = value >= 60 ? 0xFF55FF55 : value >= 30 ? 0xFFFFD040 : 0xFFFF5555;
            RenderUtils.rect(graphics, barX, baseY - barHeight, BAR_WIDTH - 1, barHeight, color);
        }
    }
}
