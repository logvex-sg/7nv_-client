package dev.sevennv.client.misc;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.ChatUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

/**
 * Reconnects to the last server after a disconnect.
 *
 * <p>Only the server the client most recently joined is retried, and the module
 * waits a configurable delay so a server that is genuinely down is not hammered.
 * Retries are capped and each attempt is reported in chat.
 */
public final class AutoReconnect extends Module {
    public AutoReconnect() {
        super("Auto Reconnect", "Rejoins the last server after a disconnect", Category.MISC);
        delay = integer("Delay", "Seconds to wait before reconnecting", 5, 1, 120);
        maxAttempts = integer("Max attempts", "Give up after this many tries (0 = unlimited)", 5, 0, 50);
        notify = bool("Notify", "Report each attempt in chat", true);
    }

    private final IntSetting delay;
    private final IntSetting maxAttempts;
    private final BooleanSetting notify;

    private final Timer timer = new Timer();
    private boolean armed;
    private int attempts;

    @Override
    public boolean survivesDisconnect() {
        return true;
    }

    /** Resets the attempt counter, called once the client is back in a world. */
    public void resetAttempts() {
        attempts = 0;
        armed = false;
    }

    /** Called by the client when a disconnect is detected. */
    public void onDisconnected() {
        if (!LastServer.hasAddress()) {
            return;
        }
        if (maxAttempts.get() > 0 && attempts >= maxAttempts.get()) {
            if (notify.get()) {
                ChatUtils.info("[AutoReconnect] attempt limit reached, giving up");
            }
            return;
        }
        armed = true;
        timer.reset();
        attempts++;
        if (notify.get()) {
            ChatUtils.info("[AutoReconnect] reconnecting to " + LastServer.name() + " in " + delay.get() + "s");
        }
    }

    /** True while a reconnect is armed and waiting for its delay. */
    public boolean isPending() {
        return armed;
    }

    /** Cancels a pending reconnect, e.g. once the player is back in a world. */
    public void cancelPending() {
        armed = false;
    }

    @Override
    public void onTick() {
        if (!armed || mc.getConnection() != null || !LastServer.hasAddress()) {
            return;
        }
        if (!timer.hasElapsed(delay.get() * 1000L)) {
            return;
        }
        armed = false;
        reconnect();
    }

    private void reconnect() {
        ServerAddress parsed = ServerAddress.parseString(LastServer.address());
        ServerData data = new ServerData(LastServer.name(), LastServer.address(), ServerData.Type.OTHER);
        ConnectScreen.startConnecting(mc.screen, mc, parsed, data, false, null);
    }

    @Override
    public String getArraylistSuffix() {
        return attempts == 0 ? "" : "x" + attempts;
    }
}
