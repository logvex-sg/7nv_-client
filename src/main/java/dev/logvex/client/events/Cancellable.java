package dev.logvex.client.events;

/** An event that listeners may cancel to suppress the default behaviour. */
public interface Cancellable {
    void setCancelled(boolean cancelled);

    boolean isCancelled();
}
