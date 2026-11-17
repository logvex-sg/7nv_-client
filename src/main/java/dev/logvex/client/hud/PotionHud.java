package dev.logvex.client.hud;

import dev.logvex.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/** Active potion effects with remaining durations. */
public final class PotionHud extends HudElement {
    public PotionHud() {
        super("potions", "Active Effects", 220, 2);
    }

    @Override
    public int getWidth() {
        int max = RenderUtils.font().width("No effects") + 4;
        Player player = HudData.mc().player;
        if (player != null) {
            for (MobEffectInstance effect : HudFormat.effects(player)) {
                max = Math.max(max, RenderUtils.font().width(line(effect)) + 4);
            }
        }
        return max;
    }

    @Override
    public int getHeight() {
        Player player = HudData.mc().player;
        if (player == null) {
            return 10;
        }
        int count = HudFormat.effects(player).size();
        return Math.max(count, 1) * 10;
    }

    private static String line(MobEffectInstance effect) {
        return HudFormat.effectName(effect) + HudFormat.effectAmplifier(effect) + "  " + HudFormat.effectDuration(effect);
    }

    @Override
    public void render(GuiGraphics graphics) {
        Player player = HudData.mc().player;
        if (player == null) {
            return;
        }
        List<MobEffectInstance> effects = HudFormat.effects(player);
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        if (effects.isEmpty()) {
            RenderUtils.text(graphics, "No effects", x + 1, y + 1, 0xFF808080);
            return;
        }
        int row = 0;
        for (MobEffectInstance effect : effects) {
            int color = effect.getEffect().value().isBeneficial() ? 0xFF80FF80 : 0xFFFF8080;
            RenderUtils.text(graphics, line(effect), x + 1, y + row * 10 + 1, color);
            row++;
        }
    }
}
