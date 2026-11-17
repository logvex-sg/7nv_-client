package dev.logvex.client.misc;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.IntSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.utils.ChatUtils;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Saves screenshots with a configurable filename and an optional caption.
 *
 * <p>Capture uses the vanilla screenshot path, so the image is identical to a
 * normal F2 screenshot and lands in the standard screenshots directory.
 */
public final class ScreenshotManager extends Module {
    public ScreenshotManager() {
        super("Screenshot Manager", "Saves screenshots with custom names", Category.MISC);
        subfolder = bool("Subfolder", "Save into a logvex subfolder", false);
        filenameFormat = text("Filename", "Java date pattern for the filename", "yyyy-MM-dd_HH.mm.ss");
        notify = bool("Notify", "Report the saved path in chat", true);
        downscale = integer("Downscale", "Downscale factor for smaller files", 1, 1, 4);
    }

    private final BooleanSetting subfolder;
    private final dev.logvex.client.config.StringSetting filenameFormat;
    private final BooleanSetting notify;
    private final IntSetting downscale;

    /** Captures the current frame and writes it to disk. */
    public void capture() {
        int scale = downscale.get();
        Screenshot.takeScreenshot(mc.getMainRenderTarget(), scale, image -> {
            try {
                Path directory = mc.gameDirectory.toPath().resolve("screenshots");
                if (subfolder.get()) {
                    directory = directory.resolve("logvex");
                }
                File folder = directory.toFile();
                if (!folder.exists() && !folder.mkdirs()) {
                    ChatUtils.error("[Screenshots] could not create " + folder);
                    return;
                }
                String pattern;
                try {
                    pattern = filenameFormat.get();
                    new SimpleDateFormat(pattern, Locale.ROOT).format(new Date());
                } catch (IllegalArgumentException exception) {
                    pattern = "yyyy-MM-dd_HH.mm.ss";
                }
                String stamp = new SimpleDateFormat(pattern, Locale.ROOT).format(new Date());
                File target = uniqueFile(folder, "logvex-" + stamp);
                image.writeToFile(target.toPath());
                if (notify.get()) {
                    ChatUtils.info("[Screenshots] saved " + target.getName());
                }
            } catch (Exception exception) {
                ChatUtils.error("[Screenshots] failed: " + exception.getMessage());
            } finally {
                image.close();
            }
        });
    }

    private static File uniqueFile(File folder, String base) {
        File candidate = new File(folder, base + ".png");
        int index = 1;
        while (candidate.exists() && index < 1000) {
            candidate = new File(folder, base + "_" + index++ + ".png");
        }
        return candidate;
    }
}
