package dev.sevennv.client.utils;

/**
 * Estimates server tick rate from the periodic world-time update packet.
 *
 * <p>Every {@code ClientboundSetTimePacket} advances the world's game time by
 * one per tick, so the ratio of game-time delta to wall-clock delta over the
 * interval between two packets is a direct TPS estimate. The value is smoothed
 * because a single interval is noisy.
 */
public final class TpsTracker {
    private static final double SMOOTHING = 0.25;

    private static long lastGameTime = Long.MIN_VALUE;
    private static long lastWallClock;
    private static double tps = 20.0;
    private static long lastIntervalMs = 50L;

    private TpsTracker() {
    }

    public static void onTimePacket(long gameTime) {
        long now = System.currentTimeMillis();
        if (lastGameTime != Long.MIN_VALUE) {
            long gameDelta = gameTime - lastGameTime;
            long wallDelta = now - lastWallClock;
            if (wallDelta > 0 && gameDelta >= 0) {
                double instant = gameDelta * 1000.0 / wallDelta;
                instant = Math.max(0.0, Math.min(20.0, instant));
                tps = tps * (1.0 - SMOOTHING) + instant * SMOOTHING;
                lastIntervalMs = wallDelta;
            }
        }
        lastGameTime = gameTime;
        lastWallClock = now;
    }

    public static double getTps() {
        return tps;
    }

    /** Estimated milliseconds per tick, derived from the smoothed TPS. */
    public static double getMspt() {
        return tps <= 0.01 ? 1000.0 : 1000.0 / tps;
    }

    public static long getIntervalMs() {
        return lastIntervalMs;
    }

    public static String format() {
        return String.format(java.util.Locale.ROOT, "%.1f", tps);
    }

    public static void reset() {
        lastGameTime = Long.MIN_VALUE;
        lastWallClock = 0L;
        tps = 20.0;
        lastIntervalMs = 50L;
    }
}
