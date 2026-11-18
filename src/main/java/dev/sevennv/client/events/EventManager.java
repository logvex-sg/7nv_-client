package dev.sevennv.client.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A tiny typed event bus. Listeners are keyed by the event class, so no
 * reflection is involved and dispatch stays allocation free on the hot path.
 */
public final class EventManager {
    private final Map<Class<?>, List<Consumer<?>>> listeners = new HashMap<>();

    public <T> void subscribe(Class<T> type, Consumer<T> listener) {
        listeners.computeIfAbsent(type, key -> new ArrayList<>()).add(listener);
    }

    @SuppressWarnings("unchecked")
    public <T> T post(T event) {
        List<Consumer<?>> entries = listeners.get(event.getClass());
        if (entries == null) {
            return event;
        }
        for (Consumer<?> entry : entries) {
            try {
                ((Consumer<T>) entry).accept(event);
            } catch (Exception exception) {
                dev.sevennv.client.SevennvClient.LOGGER.error("Event listener failed for {}", event.getClass().getSimpleName(), exception);
            }
        }
        return event;
    }

    /** Posts an event and reports whether a listener cancelled it. */
    public <T extends Cancellable> boolean postCancellable(T event) {
        post(event);
        return event.isCancelled();
    }

    public void clear() {
        listeners.clear();
    }
}
