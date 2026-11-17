package dev.logvex.client.events;

/** Fired when the client joins a world or changes dimension. */
public class WorldChangeEvent {
    private final boolean joined;

    public WorldChangeEvent(boolean joined) {
        this.joined = joined;
    }

    /** True when joining a world, false when leaving one. */
    public boolean isJoined() {
        return joined;
    }
}
