package dev.sevennv.client.misc;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.InteractionHand;

/**
 * Keeps the player marked as active so an idle server does not kick them.
 * Actions are local and harmless: a hand swing and, optionally, a small jump.
 * Nothing is sent that a normal player could not do by pressing a key.
 */
public final class AntiAfk extends Module {
    public AntiAfk() {
        super("Anti AFK", "Periodically swings and jumps so idle servers keep you connected", Category.MISC);
        interval = number("Interval", "Seconds between actions", 20.0, 5.0, 120.0, 1.0);
        swing = bool("Swing hand", "Swing the main hand each interval", true);
        jump = bool("Jump", "Jump each interval", true);
        requireOnGround = bool("Only jump on ground", "Do not jump while airborne", true);
    }

    private final DoubleSetting interval;
    private final BooleanSetting swing;
    private final BooleanSetting jump;
    private final BooleanSetting requireOnGround;

    private final Timer timer = new Timer();

    @Override
    public void onEnable() {
        timer.reset();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }
        if (!timer.tick((long) (interval.get() * 1000.0))) {
            return;
        }
        if (swing.get()) {
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
        if (jump.get() && (!requireOnGround.get() || mc.player.onGround())) {
            mc.player.input.makeJump();
        }
    }
}
