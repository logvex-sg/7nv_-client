package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/**
 * Skips block-entity render-state extraction for block entities that are too far
 * away or buried inside solid geometry. Chests, signs, banners and the rest are
 * expensive to extract, so hiding the ones you cannot see saves real frame time.
 *
 * <p>Block entities that matter for interaction (the one you are looking at) and
 * the ones a player would notice at range (beacons, end portals, signs, beds)
 * can be kept visible with the per-type toggles.
 */
public final class BlockEntityCulling extends Module {
    public BlockEntityCulling() {
        super("Block Entity Culling", "Skips chests, signs and other block entities you cannot see", Category.RENDER);
        setAlwaysOn();
        maxDistance = number("Max distance", "Do not render block entities beyond this many blocks", 32.0, 4.0, 128.0, 4.0);
        cullBuried = bool("Cull buried", "Hide block entities with no visible face", true);
        keepContainers = bool("Keep chests", "Always render chests, barrels and shulkers", true);
        keepSigns = bool("Keep signs", "Always render signs and hanging signs", true);
        keepBeacons = bool("Keep beacons", "Always render beacons and end portals", true);
        keepSpawners = bool("Keep spawners", "Always render mob spawners", true);
        keepLookingAt = bool("Keep looked-at", "Never hide the block entity in your crosshair", true);
    }

    private final DoubleSetting maxDistance;
    private final BooleanSetting cullBuried;
    private final BooleanSetting keepContainers;
    private final BooleanSetting keepSigns;
    private final BooleanSetting keepBeacons;
    private final BooleanSetting keepSpawners;
    private final BooleanSetting keepLookingAt;

    private static final Set<BlockEntityType<?>> CONTAINERS = new HashSet<>();
    private static final Set<BlockEntityType<?>> SIGNS = new HashSet<>();
    private static final Set<BlockEntityType<?>> BEACONS = new HashSet<>();
    private static final Set<BlockEntityType<?>> SPAWNERS = new HashSet<>();

    static {
        add(CONTAINERS, BlockEntityType.CHEST, BlockEntityType.TRAPPED_CHEST, BlockEntityType.BARREL,
                BlockEntityType.SHULKER_BOX, BlockEntityType.ENDER_CHEST, BlockEntityType.HOPPER,
                BlockEntityType.DISPENSER, BlockEntityType.DROPPER, BlockEntityType.FURNACE,
                BlockEntityType.BLAST_FURNACE, BlockEntityType.SMOKER, BlockEntityType.BREWING_STAND,
                BlockEntityType.CRAFTER, BlockEntityType.DECORATED_POT);
        add(SIGNS, BlockEntityType.SIGN, BlockEntityType.HANGING_SIGN);
        add(BEACONS, BlockEntityType.BEACON, BlockEntityType.END_PORTAL, BlockEntityType.END_GATEWAY);
        add(SPAWNERS, BlockEntityType.MOB_SPAWNER, BlockEntityType.TRIAL_SPAWNER, BlockEntityType.VAULT);
    }

    private static void add(Set<BlockEntityType<?>> set, BlockEntityType<?>... types) {
        for (BlockEntityType<?> type : types) {
            if (type != null) {
                set.add(type);
            }
        }
    }

    /** True when this block entity should be skipped this frame. */
    public boolean shouldCull(BlockEntity blockEntity, Vec3 cameraPos) {
        if (!isEnabled() || cameraPos == null || blockEntity == null || mc.level == null) {
            return false;
        }
        BlockEntityType<?> type = blockEntity.getType();
        if (keepContainers.get() && CONTAINERS.contains(type)) {
            return false;
        }
        if (keepSigns.get() && SIGNS.contains(type)) {
            return false;
        }
        if (keepBeacons.get() && BEACONS.contains(type)) {
            return false;
        }
        if (keepSpawners.get() && SPAWNERS.contains(type)) {
            return false;
        }
        if (keepLookingAt.get() && mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit
                && hit.getBlockPos().equals(blockEntity.getBlockPos())) {
            return false;
        }
        double distanceSq = blockEntity.getBlockPos().distToCenterSqr(cameraPos);
        double max = maxDistance.get();
        if (distanceSq > max * max) {
            return true;
        }
        if (cullBuried.get() && isBuried(blockEntity)) {
            return true;
        }
        return false;
    }

    /** True when every neighbouring block hides this one's faces. */
    private boolean isBuried(BlockEntity blockEntity) {
        if (mc.level == null) {
            return false;
        }
        net.minecraft.core.BlockPos pos = blockEntity.getBlockPos();
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
            BlockState neighbour = mc.level.getBlockState(pos.relative(direction));
            if (!neighbour.isSolidRender()) {
                return false;
            }
        }
        return true;
    }
}
