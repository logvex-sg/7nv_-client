package dev.logvex.client.misc;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.DoubleSetting;
import dev.logvex.client.config.EnumSetting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Sends a configured reply when a player is killed, so the classic "ez" line
 * can be posted without typing it.
 *
 * <p>Triggers: a kill, or the victim dropping below a health threshold. Both
 * can be enabled independently, and every send is rate-limited by the
 * {@link AutoReply} cooldown and session cap.
 */
public final class AutoEz extends AutoReply {
    public AutoEz() {
        super("Auto EZ", "Sends a configurable message when you get a kill",
                new String[]{"ez", "gg", "e z", "too easy"});
        onKill = bool("On kill", "Trigger when a tracked player dies", true);
        onLowHealth = bool("On low health", "Trigger when the victim drops below the health threshold", false);
        healthThreshold = number("Health threshold", "Victim health percent for the low health trigger", 20.0, 1.0, 100.0, 1.0);
        killCreditOnly = bool("Kill credit only", "Only trigger for players you damaged", true);
        profile = option("Profile", "Message profile to use", Profile.DEFAULT);
    }

    public enum Profile {
        DEFAULT, SHORT, TAUNT, POLITE
    }

    private final BooleanSetting onKill;
    private final BooleanSetting onLowHealth;
    private final DoubleSetting healthThreshold;
    private final BooleanSetting killCreditOnly;
    private final EnumSetting<Profile> profile;

    /** Called by the combat bridge when a player you damaged dies. */
    public void onKill(LivingEntity victim) {
        if (victim == null) {
            return;
        }
        onKillName(victim instanceof Player player ? player.getGameProfile().name() : victim.getName().getString());
    }

    /** Called with the name of an entity credited to the local player. */
    public void onKillName(String victim) {
        if (!onKill.get()) {
            return;
        }
        queue(victim);
    }

    /** Called when a nearby player dies and no kill credit is tracked. */
    public void onObservedDeath(LivingEntity victim) {
        if (victim == null) {
            return;
        }
        onObservedDeathName(victim.getName().getString());
    }

    /** Called with the name of a death the local player was not credited for. */
    public void onObservedDeathName(String victim) {
        if (killCreditOnly.get() || !onKill.get()) {
            return;
        }
        queue(victim);
    }

    /** Called by the combat bridge when a tracked victim falls below the threshold. */
    public void onLowHealth(LivingEntity victim) {
        if (victim == null || !onLowHealth.get() || victim.getMaxHealth() <= 0) {
            return;
        }
        float fraction = victim.getHealth() / victim.getMaxHealth() * 100f;
        if (fraction <= healthThreshold.get()) {
            queue(victim.getName().getString());
        }
    }

    public Profile getProfile() {
        return profile.get();
    }
}
