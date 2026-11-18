package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.events.PacketEvent;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.render.WorldRenderUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.phys.AABB;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Marks chunks the server sent while the player was watching, which is the
 * classic "new chunks" signal for finding freshly generated terrain. The client
 * only records chunks it already received; no chunk is requested and nothing is
 * sent back.
 */
public final class NewChunks extends Module {
    public NewChunks() {
        super("New Chunks", "Highlights chunks as they are first received", Category.RENDER);
        color = color("Color", "Highlight colour", 0xFF30C0FF);
        maxChunks = integer("Max chunks", "How many chunks to keep highlighted", 256, 8, 2048);
        clearOnWorldChange = bool("Clear on world change", "Forget chunks when the world changes", true);
    }

    private final ColorSetting color;
    private final IntSetting maxChunks;
    private final BooleanSetting clearOnWorldChange;

    private final Set<Long> chunks = new LinkedHashSet<>();

    @Override
    public void onWorldChange() {
        if (clearOnWorldChange.get()) {
            chunks.clear();
        }
    }

    @Override
    public void onDisable() {
        chunks.clear();
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getDirection() != PacketEvent.Direction.INBOUND
                || !(event.getPacket() instanceof ClientboundLevelChunkWithLightPacket packet)) {
            return;
        }
        long key = (((long) packet.getX()) << 32) ^ (packet.getZ() & 0xFFFFFFFFL);
        if (chunks.add(key) && chunks.size() > maxChunks.get()) {
            var iterator = chunks.iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
    }

    @Override
    public void onWorldRender() {
        if (mc.player == null) {
            return;
        }
        int y = mc.player.blockPosition().getY();
        for (long key : chunks) {
            int chunkX = (int) (key >> 32);
            int chunkZ = (int) key;
            BlockPos corner = new BlockPos(chunkX << 4, y, chunkZ << 4);
            WorldRenderUtils.box(new AABB(corner).inflate(16.0, 1.0, 16.0).move(0.0, -0.5, 0.0), color.get(), 1.0f);
        }
    }

    @Override
    public String getArraylistSuffix() {
        return chunks.isEmpty() ? "" : String.valueOf(chunks.size());
    }
}
