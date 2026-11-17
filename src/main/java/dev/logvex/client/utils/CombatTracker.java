package dev.logvex.client.utils;

import dev.logvex.client.mixin.ServerboundInteractPacketAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Tracks combat events from the packets the client already receives, so the
 * kill-credit, death-coordinate and death-reply modules have one source of
 * truth.
 *
 * <p>Two signals are used: the attack interact packet the client sends (for
 * kill credit) and the vanilla entity death event. Both are read-only; nothing
 * is re-sent or modified.
 */
public final class CombatTracker {
    /** How long after being attacked an entity is still attributed to us. */
    private static final long CREDIT_WINDOW_MS = 8000L;

    private static final Map<Integer, Long> RECENT_ATTACKS = new HashMap<>();
    private static final Deque<Death> RECENT_DEATHS = new ArrayDeque<>();
    private static final int MAX_DEATHS = 32;

    private static LocalDeath pendingLocalDeath;

    private CombatTracker() {
    }

    public record Death(String name, boolean player, boolean credited, long time) {
    }

    /** The local player's own death, with the position it happened at. */
    public record LocalDeath(Vec3 position, String dimension, long time) {
    }

    /** Records that the local player attacked the entity with the given id. */
    public static void onAttackPacket(ServerboundInteractPacket packet) {
        boolean[] attack = {false};
        packet.dispatch(new ServerboundInteractPacket.Handler() {
            @Override
            public void onInteraction(net.minecraft.world.InteractionHand hand) {
            }

            @Override
            public void onInteraction(net.minecraft.world.InteractionHand hand, Vec3 position) {
            }

            @Override
            public void onAttack() {
                attack[0] = true;
            }
        });
        if (attack[0]) {
            RECENT_ATTACKS.put(((ServerboundInteractPacketAccessor) packet).logvex$getEntityId(),
                    System.currentTimeMillis());
        }
    }

    /** Handles a vanilla entity event, recording deaths. */
    public static void onEntityEvent(ClientboundEntityEventPacket packet, ClientLevel level) {
        if (level == null || packet.getEventId() != EntityEvent.DEATH) {
            return;
        }
        Entity entity = packet.getEntity(level);
        if (entity instanceof LivingEntity living) {
            record(living);
        }
    }

    /** Handles the player-combat-kill packet the server sends on a death screen. */
    public static void onCombatKill(ClientboundPlayerCombatKillPacket packet, ClientLevel level) {
        if (level == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && packet.playerId() == mc.player.getId()) {
            pendingLocalDeath = new LocalDeath(mc.player.position(), level.dimension().identifier().getPath(),
                    System.currentTimeMillis());
        }
        Entity entity = level.getEntity(packet.playerId());
        if (entity instanceof LivingEntity living) {
            record(living);
        }
    }

    /** The most recent local death, or null. Consumers keep their own time cursor. */
    public static LocalDeath lastLocalDeath() {
        return pendingLocalDeath;
    }

    /** Deaths recorded after the given timestamp, newest first. */
    public static java.util.List<Death> deathsSince(long since) {
        java.util.List<Death> result = new java.util.ArrayList<>();
        for (Death death : RECENT_DEATHS) {
            if (death.time() <= since) {
                break;
            }
            result.add(death);
        }
        return result;
    }

    private static void record(LivingEntity living) {
        boolean credited = false;
        Long attack = RECENT_ATTACKS.remove(living.getId());
        if (attack != null) {
            credited = System.currentTimeMillis() - attack <= CREDIT_WINDOW_MS;
        }
        String name = living instanceof Player player ? player.getGameProfile().name() : living.getName().getString();
        RECENT_DEATHS.addFirst(new Death(name, living instanceof Player, credited, System.currentTimeMillis()));
        while (RECENT_DEATHS.size() > MAX_DEATHS) {
            RECENT_DEATHS.removeLast();
        }
        expire();
    }

    private static void expire() {
        long cutoff = System.currentTimeMillis() - CREDIT_WINDOW_MS;
        RECENT_ATTACKS.entrySet().removeIf(entry -> entry.getValue() < cutoff);
    }

    /** True when the local player damaged the given entity recently. */
    public static boolean hasKillCredit(LivingEntity living) {
        Long attack = RECENT_ATTACKS.get(living.getId());
        return attack != null && System.currentTimeMillis() - attack <= CREDIT_WINDOW_MS;
    }

    public static Deque<Death> recentDeaths() {
        return RECENT_DEATHS;
    }

    public static void reset() {
        RECENT_ATTACKS.clear();
        RECENT_DEATHS.clear();
    }
}
