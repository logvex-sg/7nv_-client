package dev.sevennv.client.diagnostics;

import dev.sevennv.client.config.Theme;
import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.render.RenderUtils;
import dev.sevennv.client.utils.PlayerUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity metadata inspector: dumps the non-default tracked data values of the
 * entity under the crosshair. The data is the client's own copy of what the
 * server published; nothing is queried or mutated.
 */
public final class EntityMetadataInspector extends DiagModule {
    public EntityMetadataInspector() {
        super("Entity Metadata Inspector", "Shows tracked data for the entity you look at");
        maxEntries = integer("Max entries", "How many data values to list", 8, 1, 24);
        showValues = bool("Values", "Show the value of each entry", true);
        showId = bool("Entity id", "Show the entity id and type", true);
    }

    private final IntSetting maxEntries;
    private final BooleanSetting showValues;
    private final BooleanSetting showId;

    private final List<String> lines = new ArrayList<>();

    @Override
    public void onTick() {
        lines.clear();
        Entity target = PlayerUtils.crosshairTarget();
        if (target == null) {
            return;
        }
        List<SynchedEntityData.DataValue<?>> values = target.getEntityData().getNonDefaultValues();
        if (values == null) {
            return;
        }
        for (SynchedEntityData.DataValue<?> value : values) {
            if (lines.size() >= maxEntries.get()) {
                break;
            }
            String shown = showValues.get() ? shorten(value.value()) : "";
            lines.add("#" + value.id() + " " + shown);
        }
    }

    private static String shorten(Object value) {
        if (value == null) {
            return "null";
        }
        String text = String.valueOf(value);
        return text.length() > 24 ? text.substring(0, 23) + "\u2026" : text;
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Theme theme = theme();
        int width = 186;
        int rows = (showId.get() ? 1 : 0) + Math.max(1, lines.size());
        int height = 14 + rows * 9 + 2;
        int x = graphics.guiWidth() - width - 4;
        int y = graphics.guiHeight() - height - 4;

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);
        RenderUtils.text(graphics, "Entity Metadata", x + 3, y + 3, theme.accent);

        int rowY = y + 14;
        if (showId.get()) {
            Entity target = PlayerUtils.crosshairTarget();
            String idText = target == null ? "none" : target.getType().toShortString() + " #" + target.getId();
            RenderUtils.text(graphics, "Target", x + 3, rowY, theme.textDim);
            RenderUtils.textRight(graphics, idText, x + width - 3, rowY, theme.text);
            rowY += 9;
        }
        if (lines.isEmpty()) {
            RenderUtils.text(graphics, "(look at an entity)", x + 3, rowY, theme.textDim);
            return;
        }
        for (String line : lines) {
            RenderUtils.text(graphics, line, x + 3, rowY, theme.text);
            rowY += 9;
        }
    }
}
