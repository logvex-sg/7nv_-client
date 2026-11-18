package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.NotificationManager;
import dev.sevennv.client.utils.TargetUtils;
import net.minecraft.world.entity.LivingEntity;

/**
 * Announces combat events: a new target entering range, the target taking heavy
 * damage and the target dying. All notifications are rate-limited by the module
 * tick rate rather than a timer, so no extra state is retained.
 */
public final class CombatNotifications extends Module {
    public CombatNotifications() {
        super("Combat Notifications", "Notifies about target changes and kills", Category.COMBAT);
        range = number("Range", "Detection distance", 6.0, 1.0, 16.0, 0.5);
        targetMode = option("Targets", "Which entities to watch", TargetUtils.Targets.PLAYERS);
        notifyTarget = bool("Target acquired", "Notify when a new target enters range", true);
        notifyLowHealth = bool("Low target health", "Notify when the target is nearly dead", true);
        notifyKill = bool("Kill", "Notify when the target dies", true);
        lowHealthPercent = number("Low health %", "Threshold for the low health warning", 30.0, 5.0, 90.0, 5.0);
    }

    private final dev.sevennv.client.config.DoubleSetting range;
    private final dev.sevennv.client.config.EnumSetting<TargetUtils.Targets> targetMode;
    private final BooleanSetting notifyTarget;
    private final BooleanSetting notifyLowHealth;
    private final BooleanSetting notifyKill;
    private final dev.sevennv.client.config.DoubleSetting lowHealthPercent;

    private LivingEntity current;
    private boolean lowHealthAnnounced;
    private String lastName;

    @Override
    public void onEnable() {
        current = null;
        lowHealthAnnounced = false;
        lastName = null;
    }

    @Override
    public void onWorldChange() {
        current = null;
        lowHealthAnnounced = false;
        lastName = null;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) {
            return;
        }
        LivingEntity target = TargetUtils.best(range.get(), targetMode.get(), TargetUtils.Sort.DISTANCE);
        if (target == null) {
            if (current != null && notifyKill.get() && !current.isAlive()) {
                NotificationManager.info("Combat", lastName + " died");
            }
            current = null;
            lowHealthAnnounced = false;
            return;
        }
        String name = dev.sevennv.client.utils.EntityUtils.label(target);
        if (target != current) {
            if (notifyTarget.get()) {
                NotificationManager.info("Target", name + " (" + (int) mc.player.distanceTo(target) + "m)");
            }
            current = target;
            lastName = name;
            lowHealthAnnounced = false;
        }
        float fraction = target.getMaxHealth() <= 0 ? 1f : target.getHealth() / target.getMaxHealth();
        if (notifyLowHealth.get() && !lowHealthAnnounced && fraction <= lowHealthPercent.get() / 100.0) {
            lowHealthAnnounced = true;
            NotificationManager.warn("Low health", name + " at " + (int) target.getHealth() + " hp");
        }
    }
}
