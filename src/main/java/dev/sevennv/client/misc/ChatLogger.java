package dev.sevennv.client.misc;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.events.ChatReceiveEvent;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Appends every chat and system message to a log file under the client config
 * directory. The file is written on the client thread, one line per message,
 * and is only opened while the module is enabled.
 */
public final class ChatLogger extends Module {
    public ChatLogger() {
        super("Chat Logger", "Saves chat to a log file", Category.MISC);
        system = bool("System messages", "Also log game/system messages", true);
        logFile = text("File", "File name inside the logvex directory", "chat.log");
    }

    private final BooleanSetting system;
    private final dev.sevennv.client.config.StringSetting logFile;
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onChat(ChatReceiveEvent event) {
        if (event.isOverlay() || (!system.get() && event.getText().startsWith("\u00a7"))) {
            return;
        }
        String line = "[" + LocalDateTime.now().format(FORMAT) + "] " + event.getText();
        try {
            Path path = FabricLoader.getInstance().getConfigDir()
                    .resolve(dev.sevennv.client.SevennvClient.MOD_ID).resolve(logFile.get());
            Files.createDirectories(path.getParent());
            Files.writeString(path, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException exception) {
            dev.sevennv.client.SevennvClient.LOGGER.debug("Chat log write failed", exception);
        }
    }
}
