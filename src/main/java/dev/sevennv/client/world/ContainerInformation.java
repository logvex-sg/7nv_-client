package dev.sevennv.client.world;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.render.WorldRenderUtils;
import dev.sevennv.client.utils.BlockUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Labels containers with their contents summary. Only containers whose block
 * entity is currently loaded are read, so nothing is requested from the server
 * that the client does not already have.
 */
public final class ContainerInformation extends Module {
    public ContainerInformation() {
        super("Container Information", "Labels containers with their contents", Category.WORLD);
        range = number("Range", "Maximum label distance", 16.0, 2.0, 64.0, 1.0);
        color = color("Color", "Label colour", 0xFFFFC040);
        showFill = bool("Show fill", "Show used/total slots", true);
        showNames = bool("Show names", "Show the first item names", true);
        maxNames = integer("Max names", "Maximum item names per label", 3, 1, 8);
        refresh = integer("Refresh", "Milliseconds between scans", 500, 100, 5000);
    }

    private final DoubleSetting range;
    private final dev.sevennv.client.config.ColorSetting color;
    private final BooleanSetting showFill;
    private final BooleanSetting showNames;
    private final dev.sevennv.client.config.IntSetting maxNames;
    private final dev.sevennv.client.config.IntSetting refresh;

    private final Timer timer = new Timer();
    private final List<BlockPos> containers = new ArrayList<>();

    @Override
    public void onWorldChange() {
        containers.clear();
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null || !timer.tick(refresh.get())) {
            return;
        }
        containers.clear();
        int radius = (int) Math.ceil(range.get());
        BlockPos centre = mc.player.blockPosition();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = centre.offset(dx, dy, dz);
                    if (!mc.level.isLoaded(pos)) {
                        continue;
                    }
                    BlockState state = mc.level.getBlockState(pos);
                    if (BlockUtils.isStorage(state) && mc.level.getBlockEntity(pos) instanceof Container) {
                        containers.add(pos.immutable());
                    }
                }
            }
        }
    }

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        double rangeSq = range.get() * range.get();
        for (BlockPos pos : containers) {
            if (mc.player.position().distanceToSqr(Vec3.atCenterOf(pos)) > rangeSq) {
                continue;
            }
            if (!(mc.level.getBlockEntity(pos) instanceof Container container)) {
                continue;
            }
            WorldRenderUtils.label(WorldRenderUtils.blockTop(pos), describe(container), color.get());
        }
    }

    private String describe(Container container) {
        StringBuilder builder = new StringBuilder();
        int used = 0;
        int total = container.getContainerSize();
        for (int index = 0; index < total; index++) {
            var stack = container.getItem(index);
            if (stack.isEmpty()) {
                continue;
            }
            used++;
            if (showNames.get() && used <= maxNames.get()) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(stack.getHoverName().getString());
                if (stack.getCount() > 1) {
                    builder.append(" x").append(stack.getCount());
                }
            }
        }
        if (used == 0) {
            return "Empty";
        }
        if (showFill.get()) {
            builder.insert(0, used + "/" + total + " ");
        }
        return builder.toString();
    }

    @Override
    public String getArraylistSuffix() {
        return String.valueOf(containers.size());
    }
}
