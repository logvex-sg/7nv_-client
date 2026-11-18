package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Skips entity render-state extraction for entities that cannot be seen, before
 * the renderer builds their model. The client's own {@code shouldRender} already
 * frustum-culls, so this only adds the two checks it lacks: an explicit
 * distance cap and a dot-product test against the camera's view cone.
 *
 * <p>Anything that must always stay visible — the local player, players inside
 * the configured keep range, nametagged or glowing entities, mobs the player is
 * currently fighting and anything in the crosshair — is never culled, so a
 * target can never disappear.
 */
public final class EntityCulling extends Module {
    public EntityCulling() {
        super("Entity Culling", "Skips entities outside the view cone or too far away", Category.RENDER);
        setAlwaysOn();
        maxDistance = number("Max distance", "Do not render entities beyond this many blocks", 64.0, 8.0, 256.0, 4.0);
        keepPlayers = number("Keep players", "Players closer than this are always rendered", 48.0, 0.0, 256.0, 4.0);
        cullMobs = bool("Cull mobs", "Apply the distance and cone test to mobs", true);
        cullItems = bool("Cull items", "Apply it to dropped items and other non-living entities", true);
        cullOther = bool("Cull vehicles and misc", "Apply it to minecarts, boats and projectiles", true);
        alwaysNametags = bool("Keep nametagged", "Never cull an entity that has a custom name", true);
        alwaysGlowing = bool("Keep glowing", "Never cull an entity with the glowing effect", true);
        alwaysTargets = bool("Keep targets", "Never cull the entity you are attacking or looking at", true);
        angleMargin = number("Angle margin", "Extra degrees added to the view cone", 12.0, 0.0, 60.0, 2.0);
        showStats = bool("Show stats", "Count culled entities for the Performance HUD", true);
    }

    private final DoubleSetting maxDistance;
    private final DoubleSetting keepPlayers;
    private final BooleanSetting cullMobs;
    private final BooleanSetting cullItems;
    private final BooleanSetting cullOther;
    private final BooleanSetting alwaysNametags;
    private final BooleanSetting alwaysGlowing;
    private final BooleanSetting alwaysTargets;
    private final DoubleSetting angleMargin;
    private final BooleanSetting showStats;

    private static int culledThisFrame;
    private static long culledTotal;

    /** Returns true when the entity should be skipped for this frame. */
    public boolean shouldCull(Entity entity, Frustum frustum, double camX, double camY, double camZ) {
        if (!isEnabled() || mc.player == null || entity == null) {
            return false;
        }
        if (entity == mc.player || entity == mc.getCameraEntity()) {
            return false;
        }
        boolean player = entity instanceof Player;
        double distanceSq = entity.distanceToSqr(camX, camY, camZ);
        double maxSq = maxDistance.get() * maxDistance.get();
        if (player) {
            // Players are only culled past the hard cap, never by the cone.
            return distanceSq > maxSq;
        }
        if (alwaysTargets.get() && isTarget(entity)) {
            return false;
        }
        if (alwaysNametags.get() && entity.hasCustomName()) {
            return false;
        }
        if (alwaysGlowing.get() && entity.isCurrentlyGlowing()) {
            return false;
        }
        if (distanceSq > maxSq) {
            return true;
        }
        if (entity instanceof LivingEntity) {
            if (!cullMobs.get()) {
                return false;
            }
        } else if (entity instanceof net.minecraft.world.entity.item.ItemEntity
                || entity instanceof net.minecraft.world.entity.ExperienceOrb) {
            if (!cullItems.get()) {
                return false;
            }
        } else if (!cullOther.get()) {
            return false;
        }
        return !inViewCone(entity, camX, camY, camZ);
    }

    private boolean isTarget(Entity entity) {
        if (mc.crosshairPickEntity == entity) {
            return true;
        }
        if (mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit
                && hit.getEntity() == entity) {
            return true;
        }
        return false;
    }

    /** True when the entity's centre is inside the camera's field of view. */
    private boolean inViewCone(Entity entity, double camX, double camY, double camZ) {
        Vec3 forward = Vec3.directionFromRotation(mc.player.getXRot(), mc.player.getYRot());
        Vec3 toEntity = new Vec3(entity.getX() - camX,
                entity.getY() + entity.getBbHeight() * 0.5 - camY,
                entity.getZ() - camZ);
        double length = toEntity.length();
        if (length < 0.001) {
            return true;
        }
        Vec3 unit = toEntity.scale(1.0 / length);
        double dot = forward.dot(unit);
        double cosLimit = Math.cos(Math.toRadians(45.0 + angleMargin.get()));
        return dot >= cosLimit;
    }

    /** Resets the per-frame counter on the render thread. */
    public static void onFrameStart() {
        culledThisFrame = 0;
    }

    /** Records a culled entity; called by the mixin. */
    public void countCulled() {
        if (showStats.get()) {
            culledThisFrame++;
            culledTotal++;
        }
    }

    public static int culledThisFrame() {
        return culledThisFrame;
    }

    public static long culledTotal() {
        return culledTotal;
    }

    @Override
    public void onDisable() {
        culledThisFrame = 0;
    }

    @Override
    public String getArraylistSuffix() {
        return showStats.get() && culledThisFrame > 0 ? String.valueOf(culledThisFrame) : "";
    }
}
