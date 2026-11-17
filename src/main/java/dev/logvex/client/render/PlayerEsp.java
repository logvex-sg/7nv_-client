package dev.logvex.client.render;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.ColorSetting;
import dev.logvex.client.config.DoubleSetting;
import dev.logvex.client.config.EnumSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.utils.TargetUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/**
 * Class-filtered entity boxes. Shares its drawing path with {@link EntityEsp}
 * but exposes a single target group plus a colour.
 */
public final class PlayerEsp extends Module {
    public PlayerEsp() {
        super("Player ESP", "Outlines nearby players", Category.RENDER);
        range = number("Range", "Maximum draw distance", 64.0, 4.0, 256.0, 4.0);
        color = color("Color", "Outline colour", 0xFFFF5050);
        friendColor = color("Friend color", "Outline colour for friends", 0xFF50FF50);
        lineWidth = number("Line width", "Stroke thickness", 1.5, 0.5, 4.0, 0.5);
        healthBar = bool("Health bar", "Tint the box by health", false);
    }

    private final DoubleSetting range;
    private final ColorSetting color;
    private final ColorSetting friendColor;
    private final DoubleSetting lineWidth;
    private final BooleanSetting healthBar;

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        double rangeSq = range.get() * range.get();
        for (Player player : mc.level.players()) {
            if (player == mc.player || !player.isAlive() || mc.player.distanceToSqr(player) > rangeSq) {
                continue;
            }
            int outline = dev.logvex.client.utils.FriendManager.isFriend(player.getGameProfile().name())
                    ? friendColor.get() : color.get();
            if (healthBar.get()) {
                float fraction = player.getMaxHealth() <= 0 ? 1f : player.getHealth() / player.getMaxHealth();
                outline = RenderUtils.mix(0xFFFF5050, 0xFF50FF50, fraction);
            }
            WorldRenderUtils.box(WorldRenderUtils.entityBox(player), outline, (float) lineWidth.get());
        }
    }
}
