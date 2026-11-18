package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.render.RenderUtils;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.ItemUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/**
 * Weapon readout: the held item, its attack damage and its remaining
 * durability. Damage is read from the item's attribute modifiers.
 */
public final class WeaponInformation extends Module {
    public WeaponInformation() {
        super("Weapon Information", "Shows details about the held weapon", Category.COMBAT);
        showDamage = bool("Damage", "Show the attack damage", true);
        showDurability = bool("Durability", "Show the remaining durability", true);
        showEnchants = bool("Enchants", "Show sharpness and unbreaking levels", true);
        anchor = option("Anchor", "Corner to draw in", TargetHud.Anchor.BOTTOM_RIGHT);
    }

    private final BooleanSetting showDamage;
    private final BooleanSetting showDurability;
    private final BooleanSetting showEnchants;
    private final dev.sevennv.client.config.EnumSetting<TargetHud.Anchor> anchor;

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        if (mc.player == null) {
            return;
        }
        ItemStack stack = mc.player.getMainHandItem();
        Theme theme = theme();
        int width = 130;
        int height = 28;
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        int x = switch (anchor.get()) {
            case TOP_LEFT, BOTTOM_LEFT -> 4;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - width - 4;
        };
        int y = switch (anchor.get()) {
            case TOP_LEFT, TOP_RIGHT -> 90;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - height - 4;
        };

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);
        RenderUtils.text(graphics, stack.isEmpty() ? "Empty hand" : stack.getHoverName().getString(),
                x + 4, y + 3, theme.text);
        if (showDamage.get() && !stack.isEmpty()) {
            double damage = InventoryUtils.attributeOf(stack,
                    net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            RenderUtils.text(graphics, String.format(java.util.Locale.ROOT, "Damage %.1f", damage),
                    x + 4, y + 13, theme.textDim);
        }
        if (showDurability.get() && !stack.isEmpty() && stack.isDamageableItem()) {
            int remaining = ItemUtils.remainingDurability(stack);
            RenderUtils.textRight(graphics, remaining + " dur", x + width - 4, y + 13, theme.textDim);
        }
        if (showEnchants.get() && !stack.isEmpty()) {
            String enchants = "";
            int sharpness = ItemUtils.sharpnessLevel(stack);
            int unbreaking = ItemUtils.unbreakingLevel(stack);
            if (sharpness > 0) {
                enchants += "Sharp " + sharpness;
            }
            if (unbreaking > 0) {
                enchants += (enchants.isEmpty() ? "" : "  ") + "Unbr " + unbreaking;
            }
            if (!enchants.isEmpty()) {
                RenderUtils.text(graphics, enchants, x + 4, y + 20, theme.textDim);
            }
        }
    }

    private Theme theme() {
        var config = dev.sevennv.client.SevennvClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
