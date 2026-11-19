package dev.sevennv.client.diagnostics;

import dev.sevennv.client.config.Theme;
import dev.sevennv.client.config.IntSetting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;

/**
 * Counts the entities around the player by kind. The count is a read of the
 * client's own loaded world; nothing is requested from the server.
 */
public final class EntityDiagnostics extends DiagModule {
    public EntityDiagnostics() {
        super("Entity Diagnostics", "Counts nearby entities by kind");
        range = integer("Range", "Count radius in blocks", 48, 4, 128);
    }

    private final IntSetting range;

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Theme theme = theme();
        DiagPanel panel = new DiagPanel().title("Entity Diagnostics");
        if (mc.level == null || mc.player == null) {
            panel.row("State", "No world");
            drawPanel(graphics, panel);
            return;
        }
        double rangeSq = range.get() * range.get();
        int players = 0;
        int hostile = 0;
        int passive = 0;
        int projectiles = 0;
        int items = 0;
        int orbs = 0;
        int other = 0;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (mc.player.distanceToSqr(entity) > rangeSq) {
                continue;
            }
            if (entity instanceof Player) {
                players++;
            } else if (entity instanceof Projectile) {
                projectiles++;
            } else if (entity instanceof ItemEntity) {
                items++;
            } else if (entity instanceof ExperienceOrb) {
                orbs++;
            } else if (entity instanceof Mob mob) {
                if (mob.getType().getCategory() == MobCategory.MONSTER) {
                    hostile++;
                } else {
                    passive++;
                }
            } else {
                other++;
            }
        }
        panel.row("Players", String.valueOf(players));
        panel.row("Hostile", String.valueOf(hostile));
        panel.row("Passive", String.valueOf(passive));
        panel.row("Projectiles", String.valueOf(projectiles));
        panel.row("Items", String.valueOf(items));
        panel.row("XP orbs", String.valueOf(orbs));
        panel.row("Other", String.valueOf(other));
        panel.row("Radius", range.get() + " blocks");
        drawPanel(graphics, panel);
    }
}
