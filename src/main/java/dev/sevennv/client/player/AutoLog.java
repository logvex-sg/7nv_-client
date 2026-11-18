package dev.sevennv.client.player;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.TargetUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.network.chat.Component;

/**
 * Disconnects from the server when health drops below a threshold and a threat
 * is nearby. The disconnect uses the ordinary client disconnect path, exactly
 * as if the player quit, so nothing is done to the server.
 */
public final class AutoLog extends Module {
    public AutoLog() {
        super("Auto Log", "Disconnects when your health gets dangerously low", Category.PLAYER);
        healthThreshold = number("Health", "Disconnect below this health", 6.0, 1.0, 20.0, 0.5);
        requireEnemy = bool("Require enemy", "Only disconnect when a player is nearby", true);
        enemyRange = number("Enemy range", "Threat detection radius", 8.0, 2.0, 32.0, 1.0);
        delay = integer("Delay", "Milliseconds of sustained danger before logging", 1000, 0, 10000);
        message = text("Message", "Reason shown on the disconnect screen", "Auto Log");
    }

    private final DoubleSetting healthThreshold;
    private final BooleanSetting requireEnemy;
    private final DoubleSetting enemyRange;
    private final IntSetting delay;
    private final dev.sevennv.client.config.StringSetting message;
    private final Timer timer = new Timer();
    private boolean armed;

    @Override
    public void onWorldChange() {
        armed = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }
        boolean danger = mc.player.getHealth() <= healthThreshold.get();
        if (danger && requireEnemy.get()) {
            var enemy = TargetUtils.best(enemyRange.get(), TargetUtils.Targets.PLAYERS, TargetUtils.Sort.DISTANCE);
            danger = enemy != null;
        }
        if (!danger) {
            armed = false;
            return;
        }
        if (!armed) {
            armed = true;
            timer.reset();
            return;
        }
        if (!timer.hasElapsed(delay.get())) {
            return;
        }
        armed = false;
        setEnabled(false);
        mc.getConnection().getConnection().disconnect(Component.literal(message.get()));
    }
}
