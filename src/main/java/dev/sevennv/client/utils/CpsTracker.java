package dev.sevennv.client.utils;

import net.minecraft.client.Minecraft;

/**
 * Tracks clicks per second for the HUD. Fed by the mouse mixin and polled by
 * the HUD element; the window is a simple one-second ring buffer.
 */
public final class CpsTracker {
    private static final long WINDOW_MS = 1000L;
    private static final int CAPACITY = 64;

    private static final long[] CLICKS = new long[CAPACITY];
    private static int index;
    private static int count;

    private CpsTracker() {
    }

    public static void onClick() {
        CLICKS[index] = System.currentTimeMillis();
        index = (index + 1) % CAPACITY;
        if (count < CAPACITY) {
            count++;
        }
    }

    public static int getCps() {
        long cutoff = System.currentTimeMillis() - WINDOW_MS;
        int total = 0;
        for (int i = 0; i < count; i++) {
            int slot = (index - 1 - i + CAPACITY * 2) % CAPACITY;
            if (CLICKS[slot] >= cutoff) {
                total++;
            }
        }
        return total;
    }

    public static void reset() {
        count = 0;
        index = 0;
    }

    /** True when the left mouse button is held, used by auto-clicking modules. */
    public static boolean leftDown() {
        return Minecraft.getInstance().mouseHandler.isLeftPressed();
    }
}
