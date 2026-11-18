package dev.sevennv.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.core.component.DataComponents;

import java.util.ArrayList;
import java.util.List;

/** Item, effect and entity formatting helpers used by the HUD elements. */
public final class HudFormat {
    private HudFormat() {
    }

    public static String format(ItemStack stack) {
        if (stack.isEmpty()) {
            return "-";
        }
        return stack.getHoverName().getString();
    }

    /** "3/12" style stack count, or "" for a single item. */
    public static String count(ItemStack stack) {
        return stack.getCount() > 1 ? "x" + stack.getCount() : "";
    }

    public static String effectName(MobEffectInstance effect) {
        return effect.getEffect().value().getDisplayName().getString();
    }

    public static String effectDuration(MobEffectInstance effect) {
        if (effect.isInfiniteDuration()) {
            return "\u221e";
        }
        return MobEffectUtil.formatDuration(effect, 1.0f, 20.0f).getString();
    }

    public static String effectAmplifier(MobEffectInstance effect) {
        int amplifier = effect.getAmplifier();
        if (amplifier <= 0) {
            return "";
        }
        return " " + toRoman(amplifier + 1);
    }

    public static List<MobEffectInstance> effects(Player player) {
        return new ArrayList<>(player.getActiveEffects());
    }

    public static String toRoman(int value) {
        String[] numerals = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return value >= 0 && value < numerals.length ? numerals[value] : String.valueOf(value);
    }

    /** Custom display name of an item, if the player renamed it. */
    public static String customName(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            Component name = stack.get(DataComponents.CUSTOM_NAME);
            return name == null ? "" : name.getString();
        }
        return "";
    }

    public static String equipmentLine(Minecraft mc, EquipmentSlot slot, String label) {
        Player player = mc.player;
        if (player == null) {
            return label + ": -";
        }
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty()) {
            return label + ": -";
        }
        return label + ": " + format(stack);
    }
}
