package dev.sevennv.client.render;

import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Shrinks each block model slightly so the gap between neighbouring blocks reads
 * as a thin dark outline — the "1 pixel common blocks" look from the 1.8 era.
 *
 * <p>The shrink is applied to the tesselation pose only, so collision, light and
 * hitboxes are untouched; it changes nothing the server sees. Because the gap is
 * a fraction of a block it stays a hairline at normal render distances. Set the
 * scale to 1.0 to disable the effect without toggling the module.
 */
public final class PixelBlocks extends Module {
    public PixelBlocks() {
        super("Pixel Blocks", "Shrinks block models so their edges form a 1px outline", Category.RENDER);
        scale = number("Block scale", "How much to shrink each block model", 0.985, 0.90, 1.0, 0.005);
        solids = bool("Solid blocks", "Outline full solid blocks", true);
        cutout = bool("Cutout blocks", "Outline leaves, glass and other cutout blocks", false);
        translucent = bool("Translucent blocks", "Outline water, ice and stained glass", false);
        depthOnly = bool("Distance falloff", "Fade the outline out with distance", true);
        maxDistance = number("Max distance", "Stop shrinking past this many blocks", 32.0, 4.0, 128.0, 4.0);
    }

    private final DoubleSetting scale;
    private final BooleanSetting solids;
    private final BooleanSetting cutout;
    private final BooleanSetting translucent;
    private final BooleanSetting depthOnly;
    private final DoubleSetting maxDistance;

    // Camera position, cached on the render thread so the section-compile
    // workers can read it without touching client state off-thread.
    private static volatile double camX;
    private static volatile double camY;
    private static volatile double camZ;

    // Last scale handed to the mesher, so a change can rebuild chunks once.
    private static float appliedScale = 1.0f;
    private static long lastReloadMs;

    /**
     * Called from the render thread once per frame. Caches the camera position
     * and, when the effective scale changed, rebuilds the loaded chunks so the
     * new look shows without waiting for them to be re-meshed on their own.
     */
    public static void updateCamera(double x, double y, double z) {
        camX = x;
        camY = y;
        camZ = z;
        if (SevennvClient.getModuleManager() == null) {
            return;
        }
        PixelBlocks module = SevennvClient.getModuleManager().get(PixelBlocks.class);
        float want = module != null && module.isEnabled() ? (float) module.scale.get() : 1.0f;
        if (Math.abs(want - appliedScale) <= 0.001f) {
            return;
        }
        appliedScale = want;
        long now = System.currentTimeMillis();
        if (net.minecraft.client.Minecraft.getInstance().level != null && now - lastReloadMs > 500) {
            lastReloadMs = now;
            net.minecraft.client.Minecraft.getInstance().levelRenderer.allChanged();
        }
    }

    /**
     * Scale to tesselate {@code pos} with. Returns 1.0 when the block should be
     * left alone. Safe to call from a section-compile worker thread.
     */
    public static float scaleForBlock(net.minecraft.world.level.block.state.BlockState state,
                                      net.minecraft.core.BlockPos pos) {
        if (SevennvClient.getModuleManager() == null) {
            return 1.0f;
        }
        PixelBlocks module = SevennvClient.getModuleManager().get(PixelBlocks.class);
        if (module == null || !module.isEnabled()) {
            return 1.0f;
        }
        return module.scaleForBlockInternal(state, pos);
    }

    private float scaleForBlockInternal(net.minecraft.world.level.block.state.BlockState state,
                                net.minecraft.core.BlockPos pos) {
        Layer layer;
        var chunkLayer = net.minecraft.client.renderer.ItemBlockRenderTypes.getChunkRenderType(state);
        if (chunkLayer == net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT) {
            layer = Layer.TRANSLUCENT;
        } else if (chunkLayer == net.minecraft.client.renderer.chunk.ChunkSectionLayer.SOLID) {
            layer = Layer.SOLID;
        } else {
            layer = Layer.CUTOUT;
        }
        double dx = pos.getX() + 0.5 - camX;
        double dy = pos.getY() + 0.5 - camY;
        double dz = pos.getZ() + 0.5 - camZ;
        return scaleFor(dx * dx + dy * dy + dz * dz, layer);
    }

    /** The scale to tesselate a block with at the given camera distance. */
    public float scaleFor(double distanceSq, Layer layer) {
        if (!isEnabled() || !applies(layer)) {
            return 1.0f;
        }
        double base = scale.get();
        if (depthOnly.get()) {
            double max = maxDistance.get();
            if (distanceSq >= max * max) {
                return 1.0f;
            }
            // Blend towards 1.0 as the block approaches the max distance.
            double t = 1.0 - Math.sqrt(distanceSq) / max;
            base = 1.0 - (1.0 - base) * t;
        }
        return (float) base;
    }

    private boolean applies(Layer layer) {
        return switch (layer) {
            case SOLID -> solids.get();
            case CUTOUT -> cutout.get();
            case TRANSLUCENT -> translucent.get();
        };
    }

    public enum Layer {
        SOLID, CUTOUT, TRANSLUCENT
    }
}
