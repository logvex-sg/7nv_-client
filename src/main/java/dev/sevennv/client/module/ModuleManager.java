package dev.sevennv.client.module;

import dev.sevennv.client.SevennvClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Owns every registered module and drives their tick lifecycle.
 */
public final class ModuleManager {
    private final Map<Class<? extends Module>, Module> modules = new LinkedHashMap<>();
    private final List<Module> ordered = new ArrayList<>();

    public void register(Module module) {
        modules.put(module.getClass(), module);
        ordered.add(module);
        ordered.sort(Comparator.comparing(Module::getName));
    }

    public void registerAll(Module... entries) {
        for (Module module : entries) {
            register(module);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> type) {
        return (T) modules.get(type);
    }

    public Module getByName(String name) {
        for (Module module : ordered) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public List<Module> getAll() {
        return ordered;
    }

    public List<Module> getByCategory(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module module : ordered) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }
        return result;
    }

    public List<Module> getEnabled() {
        List<Module> result = new ArrayList<>();
        for (Module module : ordered) {
            if (module.isEnabled()) {
                result.add(module);
            }
        }
        return result;
    }

    public int getEnabledCount() {
        int count = 0;
        for (Module module : ordered) {
            if (module.isEnabled()) {
                count++;
            }
        }
        return count;
    }

    /** Ticked once per client tick from the tick event. */
    public void tick() {
        for (Module module : ordered) {
            if (!module.isEnabled()) {
                continue;
            }
            try {
                module.onTick();
            } catch (Exception exception) {
                SevennvClient.LOGGER.error("Module {} threw during tick, disabling", module.getName(), exception);
                module.setEnabled(false);
            }
        }
    }

    /** Polls bound module keybinds. Called once per tick before {@link #tick()}. */
    public void pollKeybinds() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) {
            return;
        }
        for (Module module : ordered) {
            if (!module.isBound()) {
                continue;
            }
            InputConstants.Key key = module.getKeybind();
            while (dev.sevennv.client.utils.InputUtils.consumePress(key)) {
                module.toggle();
            }
        }
    }

    public void onWorldChange() {
        for (Module module : ordered) {
            if (module.isEnabled()) {
                try {
                    module.onWorldChange();
                } catch (Exception exception) {
                    SevennvClient.LOGGER.error("Module {} threw on world change", module.getName(), exception);
                }
            }
        }
    }

    /** Dispatches a 2D render pass to every enabled module. */
    public void render2D(net.minecraft.client.gui.GuiGraphics graphics, float partialTick) {
        for (Module module : ordered) {
            if (!module.isEnabled()) {
                continue;
            }
            try {
                module.onRender2D(graphics, partialTick);
            } catch (Exception exception) {
                SevennvClient.LOGGER.error("Module {} threw during 2D render", module.getName(), exception);
            }
        }
    }

    /** Dispatches the world render pass to every enabled module. */
    public void renderWorld() {
        for (Module module : ordered) {
            if (!module.isEnabled()) {
                continue;
            }
            try {
                module.onWorldRender();
            } catch (Exception exception) {
                SevennvClient.LOGGER.error("Module {} threw during world render", module.getName(), exception);
            }
        }
    }

    /** Dispatches a packet to every enabled module. Called from the packet observer. */
    public void onPacket(dev.sevennv.client.events.PacketEvent event) {
        for (Module module : ordered) {
            if (!module.isEnabled()) {
                continue;
            }
            try {
                module.onPacket(event);
            } catch (Exception exception) {
                SevennvClient.LOGGER.error("Module {} threw handling a packet", module.getName(), exception);
            }
        }
    }

    /** Dispatches an incoming chat message to every enabled module. */
    public void onChat(dev.sevennv.client.events.ChatReceiveEvent event) {
        for (Module module : ordered) {
            if (!module.isEnabled()) {
                continue;
            }
            try {
                module.onChat(event);
            } catch (Exception exception) {
                SevennvClient.LOGGER.error("Module {} threw handling a chat message", module.getName(), exception);
            }
        }
    }

    /** Disables everything except modules that opt out of the disconnect sweep. */
    public void disableAll() {
        for (Module module : ordered) {
            if (module.isEnabled() && !module.survivesDisconnect()) {
                module.setEnabled(false);
            }
        }
    }

    public void resetKeybinds() {
        for (Module module : ordered) {
            module.setKeybind(InputConstants.UNKNOWN);
        }
    }
}
