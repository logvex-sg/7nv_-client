package dev.logvex.client.hud;

import dev.logvex.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/**
 * One movable HUD element.
 *
 * <p>Positions are stored in GUI-scaled pixels and are relative to the
 * top-left corner of the screen. {@link #getWidth()} and {@link #getHeight()}
 * must be stable for a given frame, because the editor uses them for hit
 * testing and the drag bounds.
 */
public abstract class HudElement {
    private final String id;
    private final String name;
    protected int x;
    protected int y;
    private boolean enabled = true;

    protected HudElement(String id, String name, int defaultX, int defaultY) {
        this.id = id;
        this.name = name;
        this.x = defaultX;
        this.y = defaultY;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** Width in GUI-scaled pixels, recomputed from the current data. */
    public abstract int getWidth();

    /** Height in GUI-scaled pixels. */
    public abstract int getHeight();

    /** Draws the element at its current position. */
    public abstract void render(GuiGraphics graphics);

    /** Background colour of the flat panel behind the text. */
    protected int background() {
        return 0x90000000;
    }

    /**
     * Convenience for text elements: a flat panel with a left-aligned line of
     * text, sized to the text. Returns the drawn width so callers that stack
     * several lines can align them.
     */
    protected int drawLine(GuiGraphics graphics, String text, int color) {
        int width = RenderUtils.font().width(text);
        RenderUtils.rect(graphics, x, y, width + 2, 10, background());
        RenderUtils.text(graphics, text, x + 1, y + 1, color);
        return width + 2;
    }

    /** Outlined placeholder drawn while the HUD editor is open. */
    public void renderEditor(GuiGraphics graphics, int themeText, int themeBorder, boolean selected) {
        int width = Math.max(getWidth(), 12);
        int height = Math.max(getHeight(), 10);
        RenderUtils.rect(graphics, x - 1, y - 1, width + 2, height + 2, 0x40000000);
        RenderUtils.outline(graphics, x - 1, y - 1, width + 2, height + 2, selected ? 0xFFFFFFFF : themeBorder);
        RenderUtils.text(graphics, name, x, y + height + 2, themeText);
    }
}
