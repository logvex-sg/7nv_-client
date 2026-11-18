package dev.sevennv.client.player;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Timer;

/**
 * Respawns automatically after death. The respawn is requested through the
 * vanilla client-command packet, which is exactly what pressing the button on
 * the death screen sends.
 */
public final class AutoRespawn extends Module {
    public AutoRespawn() {
        super("Auto Respawn", "Respawns you automatically after dying", Category.PLAYER);
        delay = integer("Delay", "Milliseconds to wait before respawning", 500, 0, 5000);
        notify = bool("Notify", "Report the respawn in chat", false);
    }

    private final IntSetting delay;
    private final BooleanSetting notify;
    private final Timer timer = new Timer();
    private boolean pending;

    @Override
    public void onWorldChange() {
        pending = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        boolean dead = !mc.player.isAlive() || mc.player.getHealth() <= 0f;
        if (!dead) {
            pending = false;
            return;
        }
        if (!pending) {
            pending = true;
            timer.reset();
            return;
        }
        if (!timer.hasElapsed(delay.get())) {
            return;
        }
        pending = false;
        dev.sevennv.client.utils.PacketUtils.send(
                new net.minecraft.network.protocol.game.ServerboundClientCommandPacket(
                        net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
        if (notify.get()) {
            dev.sevennv.client.utils.NotificationManager.info("Auto Respawn", "Requested respawn");
        }
    }
}
