package dev.sevennv.client.events;

import net.minecraft.network.chat.Component;

/** Fired when a chat or system message arrives from the server. */
public class ChatReceiveEvent implements Cancellable {
    private final Component message;
    private final boolean overlay;
    private boolean cancelled;

    public ChatReceiveEvent(Component message, boolean overlay) {
        this.message = message;
        this.overlay = overlay;
    }

    public Component getMessage() {
        return message;
    }

    public String getText() {
        return message.getString();
    }

    public boolean isOverlay() {
        return overlay;
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
