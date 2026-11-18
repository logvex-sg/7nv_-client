package dev.sevennv.client.module;

/**
 * The ClickGUI categories. Order here is the order the panels appear in.
 */
public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    WORLD("World"),
    PLAYER("Player"),
    MISC("Misc"),
    DIAGNOSTICS("Exploit Diagnostics"),
    HUD("HUD"),
    CLIENT("Client");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
