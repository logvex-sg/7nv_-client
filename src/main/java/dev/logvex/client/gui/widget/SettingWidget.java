package dev.logvex.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;

/**
 * A single row inside a module's settings block. Widgets are created per frame
 * from the module's settings list, so they hold no persistent state beyond the
 * transient drag/typing flags.
 */
public abstract class SettingWidget {
    protected int x;
    protected int y;
    protected int width;
    protected int height;

    protected SettingWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Widgets are built once per module and repositioned every frame, so that
     * transient state (text focus, slider drag) survives the layout pass.
     */
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    /** True when the widget currently owns the keyboard, so the panel keeps it. */
    public boolean isCapturingInput() {
        return false;
    }

    public abstract void render(GuiGraphics graphics, int mouseX, int mouseY);

    /** Returns true when the click was consumed. */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    public boolean keyPressed(int keyCode) {
        return false;
    }

    public boolean charTyped(char character) {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }

    protected boolean hovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
