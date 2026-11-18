package dev.sevennv.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Inventory manipulation for the automation modules.
 *
 * <p>Everything is driven through {@code MultiPlayerGameMode.handleInventoryMouseClick}
 * so the server receives ordinary container clicks. Player-inventory index to
 * menu-slot mapping follows the vanilla {@code InventoryMenu} layout.
 */
public final class InventoryUtils {
    /** Offhand swap button used by the vanilla "swap offhand" action. */
    public static final int OFFHAND_BUTTON = 40;

    private InventoryUtils() {
    }

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    // ------------------------------------------------------------- lookups

    /** Player-inventory index of the first stack matching the item, or -1. */
    public static int findItem(Item item) {
        if (mc().player == null) {
            return -1;
        }
        var inventory = mc().player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(item)) {
                return i;
            }
        }
        return -1;
    }

    public static int countItem(Item item) {
        if (mc().player == null) {
            return 0;
        }
        int total = 0;
        var inventory = mc().player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    /** Hotbar slot (0-8) holding the item, or -1. */
    public static int findHotbarSlot(Item item) {
        if (mc().player == null) {
            return -1;
        }
        var inventory = mc().player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inventory.getItem(i).is(item)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean hasInHotbar(Item item) {
        return findHotbarSlot(item) != -1;
    }

    /** Moves the first matching item into the hotbar and selects it. */
    public static boolean switchTo(Item item) {
        Minecraft mc = mc();
        if (mc.player == null) {
            return false;
        }
        int hotbar = findHotbarSlot(item);
        if (hotbar != -1) {
            selectSlot(hotbar);
            return true;
        }
        int inventoryIndex = findItem(item);
        if (inventoryIndex == -1) {
            return false;
        }
        // Move the stack into the currently selected hotbar slot.
        int selected = mc.player.getInventory().getSelectedSlot();
        if (mc.gameMode != null) {
            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId,
                    menuSlot(inventoryIndex), selected, ClickType.SWAP, mc.player);
        }
        selectSlot(selected);
        return true;
    }

    public static void selectSlot(int slot) {
        Minecraft mc = mc();
        if (mc.player == null || slot < 0 || slot > 8) {
            return;
        }
        mc.player.getInventory().setSelectedSlot(slot);
        PacketUtils.send(new ServerboundSetCarriedItemPacket(slot));
    }

    public static ItemStack heldStack() {
        return mc().player == null ? ItemStack.EMPTY : mc().player.getMainHandItem();
    }

    public static ItemStack offhandStack() {
        return mc().player == null ? ItemStack.EMPTY : mc().player.getOffhandItem();
    }

    // -------------------------------------------------------- menu clicks

    /** Maps a player-inventory index (0-35) to its {@code InventoryMenu} slot id. */
    public static int menuSlot(int playerInventoryIndex) {
        if (playerInventoryIndex >= 0 && playerInventoryIndex <= 8) {
            return playerInventoryIndex + 36;
        }
        return playerInventoryIndex;
    }

    /** Maps an armor {@link EquipmentSlot} to its {@code InventoryMenu} slot id. */
    public static int armorMenuSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 5;
            case CHEST -> 6;
            case LEGS -> 7;
            case FEET -> 8;
            default -> -1;
        };
    }

    public static void quickMove(int menuSlot) {
        click(menuSlot, 0, ClickType.QUICK_MOVE);
    }

    public static void swapWithOffhand(int menuSlot) {
        click(menuSlot, OFFHAND_BUTTON, ClickType.SWAP);
    }

    public static void pickup(int menuSlot, int button) {
        click(menuSlot, button, ClickType.PICKUP);
    }

    private static void click(int menuSlot, int button, ClickType type) {
        Minecraft mc = mc();
        if (mc.player == null || mc.gameMode == null || menuSlot < 0) {
            return;
        }
        mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, menuSlot, button, type, mc.player);
    }

    // ------------------------------------------------------------- scoring

    private static final EquipmentSlot[] ARMOR_SLOTS =
            {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public static EquipmentSlot[] armorSlots() {
        return ARMOR_SLOTS;
    }

    /**
     * Rough armour quality score: base protection plus toughness and blast
     * protection weighting. Higher is better.
     */
    public static double armorScore(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0;
        }
        double armor = 0.0;
        double toughness = 0.0;
        stack.forEachModifier(EquipmentSlot.CHEST, (attribute, modifier) -> {
            // Placeholder; real values are read per-slot below.
        });
        armor += attributeOf(stack, Attributes.ARMOR);
        toughness += attributeOf(stack, Attributes.ARMOR_TOUGHNESS);
        return armor * 2.0 + toughness + ItemUtils.blastProtectionLevel(stack) * 1.5
                + ItemUtils.protectionLevel(stack) * 1.2 + ItemUtils.unbreakingLevel(stack) * 0.2;
    }

    /** Sum of a single attribute's modifiers on a stack, ignoring the slot. */
    public static double attributeOf(ItemStack stack, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute) {
        double[] total = {0.0};
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            stack.forEachModifier(slot, (holder, modifier) -> {
                if (holder.equals(attribute)) {
                    total[0] += modifier.amount();
                }
            });
        }
        return total[0];
    }

    /** Weapon score used by {@link dev.sevennv.client.combat.AutoWeapon}. */
    public static double weaponScore(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0;
        }
        double damage = attributeOf(stack, Attributes.ATTACK_DAMAGE);
        if (damage <= 0.0) {
            return 0.0;
        }
        double speed = attributeOf(stack, Attributes.ATTACK_SPEED);
        return damage + ItemUtils.sharpnessLevel(stack) * 0.5 + speed * 0.1;
    }

    /** The hotbar slot with the best weapon score, or -1 when none is a weapon. */
    public static int bestHotbarWeapon(boolean preferSword, boolean preferAxe) {
        Minecraft mc = mc();
        if (mc.player == null) {
            return -1;
        }
        int bestSlot = -1;
        double bestScore = 0.0;
        var inventory = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getItem(i);
            double score = weaponScore(stack);
            if (score <= 0.0) {
                continue;
            }
            if (preferSword && stack.is(Items.NETHERITE_SWORD)) {
                score += 1.0;
            }
            if (preferAxe && stack.is(Items.NETHERITE_AXE)) {
                score += 1.0;
            }
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }
        return bestSlot;
    }
}
