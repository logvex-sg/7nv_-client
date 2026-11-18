package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.FriendManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Replaces the vanilla nametag with a compact line: name, health and distance.
 * Friends are coloured green and enemies red.
 */
public final class Nametags extends Module {
    public Nametags() {
        super("Nametags", "Custom name tags with health and distance", Category.RENDER);
        range = number("Range", "Maximum draw distance", 64.0, 4.0, 256.0, 4.0);
        scale = number("Scale", "Text scale", 1.0, 0.4, 3.0, 0.1);
        showHealth = bool("Health", "Show the entity's health", true);
        showDistance = bool("Distance", "Show the distance", true);
        selfColor = color("Color", "Name colour", 0xFFFFFFFF);
        friendColor = color("Friend color", "Colour for friends", 0xFF50FF50);
        enemyColor = color("Enemy color", "Colour for enemies", 0xFFFF5050);
    }

    private final DoubleSetting range;
    private final DoubleSetting scale;
    private final BooleanSetting showHealth;
    private final BooleanSetting showDistance;
    private final ColorSetting selfColor;
    private final ColorSetting friendColor;
    private final ColorSetting enemyColor;

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        double rangeSq = range.get() * range.get();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || !living.isAlive()) {
                continue;
            }
            if (mc.player.distanceToSqr(entity) > rangeSq) {
                continue;
            }
            String name = entity instanceof Player player
                    ? player.getGameProfile().name()
                    : dev.sevennv.client.utils.EntityUtils.label(entity);
            StringBuilder builder = new StringBuilder(name);
            if (showHealth.get() && living.getMaxHealth() > 0) {
                builder.append(" ").append((int) Math.ceil(living.getHealth()));
            }
            if (showDistance.get()) {
                builder.append(" ").append((int) mc.player.distanceTo(entity)).append("m");
            }
            WorldRenderUtils.billboardText(builder.toString(),
                    entity.position().add(0, entity.getBbHeight() + 0.4, 0),
                    colorFor(entity), (float) scale.get());
        }
    }

    private int colorFor(Entity entity) {
        if (entity instanceof Player player) {
            String name = player.getGameProfile().name();
            if (FriendManager.isFriend(name)) {
                return friendColor.get();
            }
            if (FriendManager.isEnemy(name)) {
                return enemyColor.get();
            }
        }
        return selfColor.get();
    }
}
