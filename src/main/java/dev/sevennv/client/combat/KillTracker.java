package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.CombatTracker;
import dev.sevennv.client.utils.NotificationManager;
import net.minecraft.world.entity.player.Player;

/**
 * Warns when a nearby player is low enough to finish. The readout is derived
 * from data the client already has (health and distance), so nothing is sent.
 */
public final class KillTracker extends Module {
    public KillTracker() {
        super("Kill Tracker", "Flags nearby players who are low on health", Category.COMBAT);
        range = number("Range", "Search radius in blocks", 24.0, 4.0, 128.0, 2.0);
        threshold = number("Health", "Health percent considered killable", 30.0, 5.0, 100.0, 5.0);
        creditOnly = bool("Kill credit only", "Only flag players you have damaged", true);
        notify = bool("Notify", "Show a notification when a player becomes killable", true);
        cooldown = integer("Cooldown", "Milliseconds between notifications per player", 3000, 500, 30000);
    }

    private final DoubleSetting range;
    private final DoubleSetting threshold;
    private final BooleanSetting creditOnly;
    private final BooleanSetting notify;
    private final IntSetting cooldown;

    private final java.util.Map<String, Long> lastNotified = new java.util.HashMap<>();

    @Override
    public void onWorldChange() {
        lastNotified.clear();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) {
            return;
        }
        long now = System.currentTimeMillis();
        double rangeSq = range.get() * range.get();
        for (Player player : mc.level.players()) {
            if (player == mc.player || !player.isAlive() || player.getMaxHealth() <= 0) {
                continue;
            }
            if (mc.player.distanceToSqr(player) > rangeSq) {
                continue;
            }
            if (creditOnly.get() && !CombatTracker.hasKillCredit(player)) {
                continue;
            }
            float fraction = player.getHealth() / player.getMaxHealth() * 100f;
            if (fraction > threshold.get()) {
                continue;
            }
            String name = player.getGameProfile().name();
            Long last = lastNotified.get(name);
            if (last != null && now - last < cooldown.get()) {
                continue;
            }
            lastNotified.put(name, now);
            if (notify.get()) {
                NotificationManager.warn("Kill Tracker", name + " is at "
                        + Math.round(fraction) + "% (" + (int) player.getHealth() + " hp)");
            }
        }
    }

    @Override
    public String getArraylistSuffix() {
        if (mc.level == null || mc.player == null) {
            return "";
        }
        double rangeSq = range.get() * range.get();
        int count = 0;
        for (Player player : mc.level.players()) {
            if (player == mc.player || !player.isAlive() || player.getMaxHealth() <= 0) {
                continue;
            }
            if (mc.player.distanceToSqr(player) > rangeSq) {
                continue;
            }
            if (player.getHealth() / player.getMaxHealth() * 100f <= threshold.get()) {
                count++;
            }
        }
        return String.valueOf(count);
    }
}
