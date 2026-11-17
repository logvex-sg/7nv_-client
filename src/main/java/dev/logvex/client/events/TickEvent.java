package dev.logvex.client.events;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;

/** Fired once per client tick. */
public class TickEvent {
    private final Minecraft client;
    private final DeltaTracker delta;

    public TickEvent(Minecraft client, DeltaTracker delta) {
        this.client = client;
        this.delta = delta;
    }

    public Minecraft getClient() {
        return client;
    }

    public DeltaTracker getDelta() {
        return delta;
    }
}
