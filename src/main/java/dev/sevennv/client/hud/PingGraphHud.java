package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayDeque;
import java.util.Deque;

/** Sparkline of the last few seconds of ping. */
public final class PingGraphHud extends HudElement {
    private static final int SAMPLES = 40;
    private static final int GRAPH_WIDTH = 60;
    private static final int GRAPH_HEIGHT = 14;

    private final Deque<Integer> samples = new ArrayDeque<>();
    private long lastSample;

    public PingGraphHud() {
        super("pinggraph", "Ping Graph", 2, 66);
    }

    @Override
    public int getWidth() {
        return GRAPH_WIDTH + 4;
    }

    @Override
    public int getHeight() {
        return GRAPH_HEIGHT + 12;
    }

    @Override
    public void render(GuiGraphics graphics) {
        long now = System.currentTimeMillis();
        if (now - lastSample >= 250L) {
            lastSample = now;
            samples.addLast(HudData.ping());
            while (samples.size() > SAMPLES) {
                samples.removeFirst();
            }
        }
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        RenderUtils.text(graphics, "Ping", x + 2, y + 1, 0xFFE0E0E0);

        int top = y + 11;
        RenderUtils.rect(graphics, x + 2, top, GRAPH_WIDTH, GRAPH_HEIGHT, 0x40000000);
        if (samples.size() < 2) {
            return;
        }
        int max = 1;
        for (int sample : samples) {
            max = Math.max(max, sample);
        }
        int index = 0;
        int count = samples.size();
        for (int sample : samples) {
            int columnHeight = (int) Math.round((sample / (double) max) * (GRAPH_HEIGHT - 1));
            int columnX = x + 2 + index * GRAPH_WIDTH / Math.max(1, count - 1);
            RenderUtils.rect(graphics, columnX, top + GRAPH_HEIGHT - 1 - columnHeight, 1, columnHeight + 1,
                    sample > 200 ? 0xFFFF6060 : 0xFF55FF55);
            index++;
        }
        RenderUtils.text(graphics, max + "ms", x + 2, top + GRAPH_HEIGHT - 9, 0xFF909090);
    }
}
