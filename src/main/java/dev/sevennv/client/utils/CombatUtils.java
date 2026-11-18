package dev.sevennv.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared combat maths: crystal placement search, block placement and a
 * documented explosion damage estimate.
 *
 * <p>The damage figures are the same closed-form approximation used by the
 * in-game explosion formula, evaluated client-side against the blocks that are
 * currently loaded. They are an estimate, not a server-authoritative value.
 */
public final class CombatUtils {
    /** Crystal explosion power, matching {@code EntityType.END_CRYSTAL}. */
    public static final float CRYSTAL_POWER = 6.0f;
    /** Respawn-anchor explosion power. */
    public static final float ANCHOR_POWER = 5.0f;

    private CombatUtils() {
    }

    public static Minecraft mc() {
        return Minecraft.getInstance();
    }

    /** All end crystals within {@code range} blocks of the local player. */
    public static List<EndCrystal> nearbyCrystals(double range) {
        List<EndCrystal> result = new ArrayList<>();
        Minecraft mc = mc();
        if (mc.level == null || mc.player == null) {
            return result;
        }
        double rangeSq = range * range;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof EndCrystal crystal && mc.player.distanceToSqr(crystal) <= rangeSq) {
                result.add(crystal);
            }
        }
        return result;
    }

    /** True when a crystal can legally be placed on top of {@code base}. */
    public static boolean canPlaceCrystal(BlockPos base) {
        Minecraft mc = mc();
        if (mc.level == null) {
            return false;
        }
        BlockState below = mc.level.getBlockState(base);
        if (!below.is(Blocks.OBSIDIAN) && !below.is(Blocks.BEDROCK)) {
            return false;
        }
        BlockPos air = base.above();
        if (!mc.level.getBlockState(air).isAir() || !mc.level.getBlockState(air.above()).isAir()) {
            return false;
        }
        // No existing crystal or other entity already occupying the space.
        return mc.level.getEntities(null, new net.minecraft.world.phys.AABB(air)).isEmpty();
    }

    /** Obsidian or bedrock positions near {@code centre} that have room above. */
    public static List<BlockPos> crystalBases(BlockPos centre, int radius) {
        List<BlockPos> result = new ArrayList<>();
        Minecraft mc = mc();
        if (mc.level == null) {
            return result;
        }
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = centre.offset(dx, dy, dz);
                    if (canPlaceCrystal(pos)) {
                        result.add(pos);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Places the held block against {@code target}, clicking the given face.
     * Returns false when the block is out of reach or the game mode is absent.
     */
    public static boolean placeBlock(BlockPos target, Direction face) {
        Minecraft mc = mc();
        if (mc.player == null || mc.gameMode == null || mc.level == null) {
            return false;
        }
        Vec3 hit = new Vec3(
                target.getX() + 0.5 + face.getStepX() * 0.5,
                target.getY() + 0.5 + face.getStepY() * 0.5,
                target.getZ() + 0.5 + face.getStepZ() * 0.5);
        BlockHitResult hitResult = new BlockHitResult(hit, face, target, false);
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);
        mc.player.swing(InteractionHand.MAIN_HAND);
        return true;
    }

    /** Places a block on top of {@code base} by clicking its upward face. */
    public static boolean placeOnTop(BlockPos base) {
        return placeBlock(base, Direction.UP);
    }

    /**
     * Estimated explosion damage against {@code entity} from a blast centred at
     * {@code centre} with the given power. Includes a simple armour reduction.
     */
    public static float estimateExplosionDamage(Entity entity, Vec3 centre, float power, boolean applyArmor) {
        if (entity == null) {
            return 0f;
        }
        Vec3 point = entity.position().add(0, entity.getBbHeight() * 0.5, 0);
        double distance = point.distanceTo(centre);
        double diameter = power * 2.0;
        if (distance > diameter) {
            return 0f;
        }
        double falloff = 1.0 - distance / diameter;
        // The vanilla curve is (falloff^2 + falloff) / 2 scaled to the blast power.
        double exposure = blockDensity(centre, entity);
        double damage = (falloff * falloff + falloff) / 2.0 * 7.0 * diameter * exposure;
        float result = (float) damage;
        if (applyArmor && entity instanceof net.minecraft.world.entity.LivingEntity living) {
            float armor = living.getArmorValue();
            float toughness = (float) living.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);
            result = result * (1.0f - armorReduction(armor, toughness));
        }
        return Math.max(0f, result);
    }

    /** Vanilla armour damage-reduction fraction. */
    public static float armorReduction(float armor, float toughness) {
        if (armor <= 0f) {
            return 0f;
        }
        float f = 2.0f + toughness / 4.0f;
        float g = Math.min(20.0f, Math.max(armor / 5.0f, armor - 0f / f));
        return Math.min(1.0f, g / 25.0f);
    }

    /**
     * Fraction of rays from {@code centre} to the entity that are unobstructed.
     * A coarse 3x3 sampling of the entity's box, which is accurate enough for a
     * client-side readout.
     */
    public static double blockDensity(Vec3 centre, Entity entity) {
        Minecraft mc = mc();
        if (mc.level == null) {
            return 1.0;
        }
        var box = entity.getBoundingBox();
        int hits = 0;
        int total = 0;
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    double px = box.minX + (box.maxX - box.minX) * (x + 0.5) / 3.0;
                    double py = box.minY + (box.maxY - box.minY) * (y + 0.5) / 3.0;
                    double pz = box.minZ + (box.maxZ - box.minZ) * (z + 0.5) / 3.0;
                    Vec3 point = new Vec3(px, py, pz);
                    total++;
                    BlockHitResult hit = mc.level.clip(new net.minecraft.world.level.ClipContext(
                            centre, point, net.minecraft.world.level.ClipContext.Block.COLLIDER,
                            net.minecraft.world.level.ClipContext.Fluid.NONE, mc.player));
                    if (hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS) {
                        hits++;
                    }
                }
            }
        }
        return total == 0 ? 1.0 : (double) hits / total;
    }

    /** Total amount of a given item across the player's inventory. */
    public static int countItem(net.minecraft.world.item.Item item) {
        Minecraft mc = mc();
        if (mc.player == null) {
            return 0;
        }
        int total = 0;
        var inventory = mc.player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var stack = inventory.getItem(i);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    /** Hotbar slot holding the given item, or -1. */
    public static int findHotbarSlot(net.minecraft.world.item.Item item) {
        Minecraft mc = mc();
        if (mc.player == null) {
            return -1;
        }
        var inventory = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inventory.getItem(i).is(item)) {
                return i;
            }
        }
        return -1;
    }
}
