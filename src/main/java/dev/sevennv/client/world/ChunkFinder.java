package dev.sevennv.client.world;

import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.events.PacketEvent;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.NotificationManager;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Reports bursts of newly loaded chunks. A large burst usually means someone is
 * flying or the server is streaming terrain. Read-only: it only counts packets
 * it observes.
 */
public final class ChunkFinder extends Module {
    public ChunkFinder() {
        super("Chunk Finder", "Reports bursts of newly loaded chunks", Category.WORLD);
        window = integer("Window", "Milliseconds in the burst window", 500, 100, 5000);
        threshold = integer("Threshold", "Chunks in the window that trigger a report", 30, 5, 400);
        cooldown = integer("Cooldown", "Milliseconds between reports", 5000, 500, 60000);
    }

    private final IntSetting window;
    private final IntSetting threshold;
    private final IntSetting cooldown;
    private final Deque<Long> timestamps = new ArrayDeque<>();
    private long lastReport;

    @Override
    public void onWorldChange() {
        timestamps.clear();
        lastReport = 0;
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getDirection() != PacketEvent.Direction.INBOUND) {
            return;
        }
        if (!(event.getPacket() instanceof ClientboundLevelChunkWithLightPacket)) {
            return;
        }
        long now = System.currentTimeMillis();
        timestamps.addLast(now);
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > window.get()) {
            timestamps.removeFirst();
        }
        if (timestamps.size() >= threshold.get() && now - lastReport > cooldown.get()) {
            lastReport = now;
            NotificationManager.info("Chunks", timestamps.size() + " chunks in " + window.get() + "ms");
        }
    }
}
