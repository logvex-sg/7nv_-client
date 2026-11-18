package dev.sevennv.client.world;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.ChatUtils;
import dev.sevennv.client.utils.MathUtils;

/**
 * A simple in-game stopwatch. Toggle it on to start, off to stop; a lap marker
 * splits the current run. Nothing is sent to the server.
 */
public final class StopWatch extends Module {
    public StopWatch() {
        super("Stop Watch", "Times a run with lap support", Category.WORLD);
        announceLaps = bool("Announce laps", "Print each lap to chat", true);
    }

    private final BooleanSetting announceLaps;
    private long startedAt;
    private long lastLap;
    private boolean running;

    @Override
    public void onEnable() {
        startedAt = System.currentTimeMillis();
        lastLap = startedAt;
        running = true;
    }

    @Override
    public void onDisable() {
        running = false;
    }

    @Override
    public void onWorldChange() {
        running = false;
    }

    /** Splits the current run and returns the lap time in milliseconds. */
    public long lap() {
        long now = System.currentTimeMillis();
        long lap = now - lastLap;
        lastLap = now;
        if (announceLaps.get()) {
            ChatUtils.info("Lap " + MathUtils.formatDuration(lap));
        }
        return lap;
    }

    public long elapsed() {
        return running ? System.currentTimeMillis() - startedAt : 0L;
    }

    @Override
    public String getArraylistSuffix() {
        return MathUtils.formatDuration(elapsed());
    }
}
