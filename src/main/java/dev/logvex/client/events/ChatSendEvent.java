package dev.logvex.client.events;

/** Fired just before a chat message is sent to the server. */
public class ChatSendEvent implements Cancellable {
    private final String message;
    private boolean cancelled;

    public ChatSendEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
