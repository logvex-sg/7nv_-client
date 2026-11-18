package dev.sevennv.client.movement;

import com.mojang.blaze3d.platform.InputConstants;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.EditBox;

/**
 * Keeps the player's movement inputs alive while a GUI screen is open. Vanilla
 * releases the movement keys when a screen opens; this re-arms them from the
 * real keyboard state each tick, so you keep walking while you browse.
 *
 * <p>Typing in a text field is left alone: while an edit box has focus the keys
 * are released so WASD can be used as normal characters.
 */
public final class GuiMove extends Module {
    public GuiMove() {
        super("Gui Move", "Lets you walk while a GUI is open", Category.MOVEMENT);
        sneak = bool("Sneak", "Also hold sneak", true);
        jump = bool("Jump", "Also hold jump", true);
    }

    private final dev.sevennv.client.config.BooleanSetting sneak;
    private final dev.sevennv.client.config.BooleanSetting jump;

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) {
            return;
        }
        boolean typing = mc.screen != null && mc.screen.getFocused() instanceof EditBox box && box.canConsumeInput();
        boolean open = mc.screen != null && !typing;
        press(mc.options.keyUp, InputConstants.KEY_W, open);
        press(mc.options.keyDown, InputConstants.KEY_S, open);
        press(mc.options.keyLeft, InputConstants.KEY_A, open);
        press(mc.options.keyRight, InputConstants.KEY_D, open);
        press(mc.options.keyJump, InputConstants.KEY_SPACE, open && jump.get());
        press(mc.options.keyShift, InputConstants.KEY_LSHIFT, open && sneak.get());
    }

    private void press(KeyMapping mapping, int keyCode, boolean allowed) {
        if (!allowed) {
            if (mapping.isDown()) {
                mapping.setDown(false);
            }
            return;
        }
        boolean physicallyDown = InputConstants.isKeyDown(mc.getWindow(), keyCode);
        if (mapping.isDown() != physicallyDown) {
            mapping.setDown(physicallyDown);
        }
    }

    @Override
    public void onDisable() {
        release();
    }

    @Override
    public void onWorldChange() {
        release();
    }

    private void release() {
        if (mc.options == null) {
            return;
        }
        mc.options.keyUp.setDown(false);
        mc.options.keyDown.setDown(false);
        mc.options.keyLeft.setDown(false);
        mc.options.keyRight.setDown(false);
        mc.options.keyJump.setDown(false);
        mc.options.keyShift.setDown(false);
    }
}
