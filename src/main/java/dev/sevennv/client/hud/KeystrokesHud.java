package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

/**
 * Classic WASD keystroke display. Keys light up while held and the space bar is
 * drawn as a wide bar beneath them.
 */
public final class KeystrokesHud extends HudElement {
    private static final int CELL = 14;
    private static final int GAP = 2;

    public KeystrokesHud() {
        super("keystrokes", "Keystrokes", 110, 123);
    }

    @Override
    public int getWidth() {
        return CELL * 3 + GAP * 2 + 2;
    }

    @Override
    public int getHeight() {
        return CELL * 3 + GAP * 2 + 2;
    }

    @Override
    public void render(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options == null) {
            return;
        }
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        int colLeft = x + 1;
        int colMid = colLeft + CELL + GAP;
        int colRight = colMid + CELL + GAP;
        int rowTop = y + 1;
        int rowMid = rowTop + CELL + GAP;
        int rowBottom = rowMid + CELL + GAP;

        key(graphics, "W", colMid, rowTop, mc.options.keyUp.isDown());
        key(graphics, "A", colLeft, rowMid, mc.options.keyLeft.isDown());
        key(graphics, "S", colMid, rowMid, mc.options.keyDown.isDown());
        key(graphics, "D", colRight, rowMid, mc.options.keyRight.isDown());
        // Space bar spans all three columns on the bottom row.
        int spaceWidth = CELL * 3 + GAP * 2;
        boolean space = mc.options.keyJump.isDown();
        RenderUtils.rect(graphics, colLeft, rowBottom, spaceWidth, CELL, space ? 0x80FFFFFF : 0x80000000);
        RenderUtils.textCentered(graphics, "____", colLeft + spaceWidth / 2, rowBottom + 3, 0xFFE0E0E0);
    }

    private void key(GuiGraphics graphics, String label, int kx, int ky, boolean down) {
        RenderUtils.rect(graphics, kx, ky, CELL, CELL, down ? 0x80FFFFFF : 0x80000000);
        RenderUtils.textCentered(graphics, label, kx + CELL / 2, ky + 3, down ? 0xFF101010 : 0xFFE0E0E0);
    }
}
