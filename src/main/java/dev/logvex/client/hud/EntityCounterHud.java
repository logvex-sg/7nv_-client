package dev.logvex.client.hud;

import dev.logvex.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** A compact readout of nearby entity counts, grouped by kind. */
public final class EntityCounterHud extends HudElement {
    public EntityCounterHud() {
        super("entities", "Entity Counter", 110, 174);
    }

    private int players;
    private int hostiles;
    private int passives;
    private int total;

    /** Recomputes the counts; called once per tick by the owning module. */
    public void update() {
        players = 0;
        hostiles = 0;
        passives = 0;
        total = 0;
        if (HudData.mc().level == null) {
            return;
        }
        for (Entity entity : HudData.mc().level.entitiesForRendering()) {
            total++;
            if (entity == HudData.mc().player) {
                continue;
            }
            if (dev.logvex.client.utils.EntityUtils.isPlayer(entity)) {
                players++;
            } else if (dev.logvex.client.utils.EntityUtils.isHostile(entity)) {
                hostiles++;
            } else if (dev.logvex.client.utils.EntityUtils.isPassive(entity)) {
                passives++;
            }
        }
    }

    @Override
    public int getWidth() {
        return 96;
    }

    @Override
    public int getHeight() {
        return 30;
    }

    @Override
    public void render(GuiGraphics graphics) {
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        RenderUtils.text(graphics, "Players " + players, x + 1, y + 1, 0xFFE0E0E0);
        RenderUtils.text(graphics, "Hostile " + hostiles, x + 1, y + 11, 0xFFFF9090);
        RenderUtils.text(graphics, "Passive " + passives + "  Total " + total, x + 1, y + 21, 0xFFA0D0A0);
    }
}
