package dev.sevennv.client.utils;

import dev.sevennv.client.events.PacketEvent;
import net.minecraft.network.protocol.Packet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Thread-safe packet accounting for the diagnostics modules.
 *
 * <p>{@link #record} is called from the netty thread; everything else is read
 * from the client thread. Recent packets are buffered in a bounded queue so a
 * burst can never grow the heap, and the totals are plain atomics.
 */
public final class PacketLog {
    private static final int RECENT_CAPACITY = 256;

    private static final AtomicLong INBOUND = new AtomicLong();
    private static final AtomicLong OUTBOUND = new AtomicLong();
    private static final Map<String, LongAdder> INBOUND_TYPES = new ConcurrentHashMap<>();
    private static final Map<String, LongAdder> OUTBOUND_TYPES = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<PacketEvent> RECENT = new ConcurrentLinkedQueue<>();

    private PacketLog() {
    }

    /** Records one observed packet. Safe to call from the netty thread. */
    public static void record(PacketEvent.Direction direction, Packet<?> packet) {
        if (direction == PacketEvent.Direction.INBOUND) {
            INBOUND.incrementAndGet();
            INBOUND_TYPES.computeIfAbsent(packet.getClass().getSimpleName(), key -> new LongAdder()).increment();
        } else {
            OUTBOUND.incrementAndGet();
            OUTBOUND_TYPES.computeIfAbsent(packet.getClass().getSimpleName(), key -> new LongAdder()).increment();
        }
        RECENT.add(new PacketEvent(packet, direction));
        // Drop the oldest entries rather than letting the queue grow.
        while (RECENT.size() > RECENT_CAPACITY) {
            RECENT.poll();
        }
    }

    /** Returns and clears the packets recorded since the last drain. */
    public static List<PacketEvent> drain() {
        if (RECENT.isEmpty()) {
            return List.of();
        }
        List<PacketEvent> batch = new ArrayList<>(RECENT.size());
        PacketEvent event;
        while ((event = RECENT.poll()) != null) {
            batch.add(event);
        }
        return batch;
    }

    public static long inboundCount() {
        return INBOUND.get();
    }

    public static long outboundCount() {
        return OUTBOUND.get();
    }

    public static long total() {
        return INBOUND.get() + OUTBOUND.get();
    }

    /** Per-packet-type counters, sorted by descending count. */
    public static List<Map.Entry<String, Long>> topInbound(int limit) {
        return top(INBOUND_TYPES, limit);
    }

    public static List<Map.Entry<String, Long>> topOutbound(int limit) {
        return top(OUTBOUND_TYPES, limit);
    }

    private static List<Map.Entry<String, Long>> top(Map<String, LongAdder> source, int limit) {
        List<Map.Entry<String, Long>> entries = new ArrayList<>(source.size());
        source.forEach((name, adder) -> entries.add(Map.entry(name, adder.sum())));
        entries.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        return entries.size() > limit ? new ArrayList<>(entries.subList(0, limit)) : entries;
    }

    public static int distinctInbound() {
        return INBOUND_TYPES.size();
    }

    public static int distinctOutbound() {
        return OUTBOUND_TYPES.size();
    }

    public static void reset() {
        INBOUND.set(0);
        OUTBOUND.set(0);
        INBOUND_TYPES.clear();
        OUTBOUND_TYPES.clear();
        RECENT.clear();
    }

    /** Bounded view of recent packets for the inspector, newest last. */
    public static List<PacketEvent> recent(int limit) {
        List<PacketEvent> snapshot = new ArrayList<>(RECENT);
        int size = snapshot.size();
        if (size <= limit) {
            return snapshot;
        }
        return new ArrayList<>(snapshot.subList(size - limit, size));
    }

    /** Kept for callers that want a deque view. */
    public static Deque<PacketEvent> recentDeque() {
        return new ArrayDeque<>(RECENT);
    }
}
