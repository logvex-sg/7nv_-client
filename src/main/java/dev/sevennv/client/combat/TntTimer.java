package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.NotificationManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;

import java.util.HashSet;
import java.util.Set;

/**
 * Warns when primed TNT is close enough to matter, showing the distance and the
 * remaining fuse. Purely a read of the client's own entity list; nothing is
 * sent and no fuse is changed.
 */
public final class TntTimer extends Module {
    public TntTimer() {
        super("TNT Timer", "Warns about nearby primed TNT", Category.COMBAT);
        range = number("Range", "Warn distance in blocks", 12.0, 2.0, 32.0, 1.0);
        notify = bool("Notify", "Notify the first time each TNT is seen", true);
        countdown = bool("Countdown", "Notify again when the fuse is nearly up", true);
        fuseTicks = integer("Fuse ticks", "Vanilla fuse length in ticks", 80, 10, 200);
    }

    private final DoubleSetting range;
    private final BooleanSetting notify;
    private final BooleanSetting countdown;
    private final IntSetting fuseTicks;

    private final Set<Integer> announced = new HashSet<>();
    private final Set<Integer> warned = new HashSet<>();

    @Override
    public void onWorldChange() {
        announced.clear();
        warned.clear();
    }

    @Override
    public void onDisable() {
        announced.clear();
        warned.clear();
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        double rangeSq = range.get() * range.get();
        Set<Integer> live = new HashSet<>();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof PrimedTnt tnt) || mc.player.distanceToSqr(tnt) > rangeSq) {
                continue;
            }
            live.add(tnt.getId());
            int distance = (int) Math.round(Math.sqrt(mc.player.distanceToSqr(tnt)));
            if (notify.get() && announced.add(tnt.getId())) {
                int remaining = Math.max(0, fuseTicks.get() - tnt.getFuse());
                NotificationManager.info("TNT", "Primed TNT " + distance + "m away, " + remaining + " ticks");
            }
            if (countdown.get() && tnt.getFuse() <= 20 && warned.add(tnt.getId())) {
                NotificationManager.push("TNT", "Blast in about a second (" + distance + "m)",
                        NotificationManager.WARN);
            }
        }
        announced.retainAll(live);
        warned.retainAll(live);
    }
}
