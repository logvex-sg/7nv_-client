package dev.sevennv.client.hud;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/** Owns every HUD element, their layout and their persistence. */
public final class HudManager {
    private final List<HudElement> elements = new ArrayList<>();

    // Enabled subset, rebuilt only when an element is toggled so the per-frame
    // render loop does not test every element's flag on each frame.
    private final List<HudElement> enabled = new ArrayList<>();
    private boolean enabledDirty = true;

    public void register(HudElement element) {
        elements.add(element);
        enabledDirty = true;
    }

    /** Marks the enabled cache stale after an element's state changes. */
    public void invalidate() {
        enabledDirty = true;
    }

    private void ensureEnabled() {
        if (!enabledDirty) {
            return;
        }
        enabledDirty = false;
        enabled.clear();
        for (HudElement element : elements) {
            if (element.isEnabled()) {
                enabled.add(element);
            }
        }
    }

    public List<HudElement> getElements() {
        return elements;
    }

    public HudElement getById(String id) {
        for (HudElement element : elements) {
            if (element.getId().equals(id)) {
                return element;
            }
        }
        return null;
    }

    /** Draws every enabled element. Called from the HUD render callback. */
    public void render(GuiGraphics graphics) {
        ensureEnabled();
        for (HudElement element : enabled) {
            try {
                element.render(graphics);
            } catch (Exception exception) {
                dev.sevennv.client.SevennvClient.LOGGER.error("HUD element {} failed to render", element.getId(), exception);
            }
        }
    }

    /** Turns every element on or off at once. */
    public void setAllEnabled(boolean enabledState) {
        for (HudElement element : elements) {
            element.setEnabled(enabledState);
        }
        enabledDirty = true;
    }

    /** Number of currently enabled elements. */
    public int enabledCount() {
        ensureEnabled();
        return enabled.size();
    }

    /** Draws every element as an outlined box for the layout editor. */
    public void renderEditor(GuiGraphics graphics, int textColor, int borderColor, HudElement selected) {
        for (HudElement element : elements) {
            element.renderEditor(graphics, textColor, borderColor, element == selected);
        }
    }

    /** Topmost element whose bounds contain the point, or null. */
    public HudElement elementAt(double mouseX, double mouseY) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            HudElement element = elements.get(i);
            int width = Math.max(element.getWidth(), 12);
            int height = Math.max(element.getHeight(), 10);
            if (mouseX >= element.getX() - 1 && mouseX <= element.getX() + width + 1
                    && mouseY >= element.getY() - 1 && mouseY <= element.getY() + height + 1) {
                return element;
            }
        }
        return null;
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        for (HudElement element : elements) {
            JsonObject entry = new JsonObject();
            entry.addProperty("x", element.getX());
            entry.addProperty("y", element.getY());
            entry.addProperty("enabled", element.isEnabled());
            object.add(element.getId(), entry);
        }
        return object;
    }

    public void fromJson(JsonObject object) {
        if (object == null) {
            return;
        }
        for (HudElement element : elements) {
            JsonElement raw = object.get(element.getId());
            if (raw == null || !raw.isJsonObject()) {
                continue;
            }
            JsonObject entry = raw.getAsJsonObject();
            if (entry.has("x")) {
                element.setPosition(entry.get("x").getAsInt(), entry.get("y").getAsInt());
            }
            if (entry.has("enabled")) {
                element.setEnabled(entry.get("enabled").getAsBoolean());
            }
        }
    }

    /** Snaps an element inside the screen bounds. */
    public static void clamp(HudElement element, int screenWidth, int screenHeight) {
        int width = Math.max(element.getWidth(), 12);
        int height = Math.max(element.getHeight(), 10);
        int x = Math.max(0, Math.min(element.getX(), screenWidth - width - 1));
        int y = Math.max(0, Math.min(element.getY(), screenHeight - height - 1));
        element.setPosition(x, y);
    }
}
