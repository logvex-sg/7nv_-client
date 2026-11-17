package dev.logvex.client.diagnostics;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.Theme;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/**
 * Chunk loading diagnostics: how many chunks the client currently holds, the
 * view radius and whether the chunk under the player is present and populated.
 * Reads the client chunk cache only; no chunks are requested.
 */
public final class ChunkLoadingDiagnostics extends Module {
    public ChunkLoadingDiagnostics() {
        super("Chunk Loading Diagnostics", "Reports chunk cache and loading state", Category.DIAGNOSTICS);
        showRadius = bool("View radius", "Show the client view radius", true);
        showPlayerChunk = bool("Player chunk", "Show the state of the chunk under you", true);
        showEmpty = bool("Empty sections", "Show the loaded-but-empty section count", false);
    }

    private final BooleanSetting showRadius;
    private final BooleanSetting showPlayerChunk;
    private final BooleanSetting showEmpty;

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Theme theme = theme();
        DiagPanel panel = new DiagPanel().title("Chunk Loading");

        if (mc.level == null || mc.player == null) {
            panel.row("State", "no world");
            panel.render(graphics, theme, 4, 4);
            return;
        }

        var cache = mc.level.getChunkSource();
        panel.row("Loaded chunks", String.valueOf(cache.getLoadedChunksCount()));
        if (showRadius.get()) {
            panel.row("View radius", mc.options.getEffectiveRenderDistance() + " chunks");
            panel.row("Sim distance", mc.level.getServerSimulationDistance() + " chunks");
        }
        if (showPlayerChunk.get()) {
            LevelChunk chunk = mc.level.getChunkSource().getChunk(
                    mc.player.chunkPosition().x, mc.player.chunkPosition().z, ChunkStatus.FULL, false);
            panel.row("Player chunk", chunk == null ? "missing" : "present");
            if (chunk != null) {
                panel.row("Chunk pos", mc.player.chunkPosition().x + ", " + mc.player.chunkPosition().z);
            }
        }
        if (showEmpty.get()) {
            panel.row("Empty sections", String.valueOf(cache.getLoadedEmptySections().size()));
        }
        panel.render(graphics, theme, 4, 4);
    }

    private Theme theme() {
        var config = dev.logvex.client.LogvexClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
