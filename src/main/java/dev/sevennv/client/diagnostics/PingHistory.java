package dev.sevennv.client.diagnostics;

import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.hud.HudData;
import dev.sevennv.client.utils.Timer;

/**
 * Tracks ping over time and reports the minimum, average and maximum seen this
 * session. Sampling runs once a second so it has no per-frame cost.
 */
public final class PingHistory extends DiagModule {
    public PingHistory() {
        super("Ping History", "Tracks your ping over the session");
        sampleInterval = integer("Sample", "Milliseconds between samples", 1000, 250, 10000);
    }

    private final IntSetting sampleInterval;
    private final Timer timer = new Timer();

    private int samples;
    private int min = Integer.MAX_VALUE;
    private int max;
    private long total;

    @Override
    public void onWorldChange() {
        samples = 0;
        min = Integer.MAX_VALUE;
        max = 0;
        total = 0;
    }

    @Override
    public void onTick() {
        if (!timer.tick(sampleInterval.get())) {
            return;
        }
        int ping = HudData.ping();
        if (ping < 0) {
            return;
        }
        samples++;
        total += ping;
        min = Math.min(min, ping);
        max = Math.max(max, ping);
    }

    public int minimum() {
        return min == Integer.MAX_VALUE ? 0 : min;
    }

    public int maximum() {
        return max;
    }

    public int average() {
        return samples == 0 ? 0 : (int) (total / samples);
    }

    @Override
    public String getArraylistSuffix() {
        return average() + "ms";
    }
}
