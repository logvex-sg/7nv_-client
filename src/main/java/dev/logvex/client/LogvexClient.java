package dev.logvex.client;

import dev.logvex.client.command.CommandManager;
import dev.logvex.client.config.ConfigManager;
import dev.logvex.client.events.EventManager;
import dev.logvex.client.hud.ArmorHud;
import dev.logvex.client.hud.ArraylistHud;
import dev.logvex.client.hud.BiomeHud;
import dev.logvex.client.hud.CoordinatesHud;
import dev.logvex.client.hud.CpsHud;
import dev.logvex.client.hud.DirectionHud;
import dev.logvex.client.hud.DurabilityHud;
import dev.logvex.client.hud.EntityCounterHud;
import dev.logvex.client.hud.FoodHud;
import dev.logvex.client.hud.FpsHud;
import dev.logvex.client.hud.HealthHud;
import dev.logvex.client.hud.HudManager;
import dev.logvex.client.hud.InventoryHud;
import dev.logvex.client.hud.MovementHud;
import dev.logvex.client.hud.PerformanceHud;
import dev.logvex.client.hud.PingHud;
import dev.logvex.client.hud.PotionHud;
import dev.logvex.client.hud.ServerHud;
import dev.logvex.client.hud.SessionHud;
import dev.logvex.client.hud.SpeedHud;
import dev.logvex.client.hud.TargetHud;
import dev.logvex.client.hud.TimeHud;
import dev.logvex.client.hud.TotemHud;
import dev.logvex.client.hud.WatermarkHud;
import dev.logvex.client.module.KeybindManager;
import dev.logvex.client.module.ModuleManager;
import dev.logvex.client.module.ModuleRegistry;
import net.fabricmc.api.ClientModInitializer;

/**
 * Entry point. Builds the managers in dependency order, registers every
 * module and HUD element, then hands control to the event bridges.
 */
public final class LogvexClient implements ClientModInitializer {
    public static final String MOD_ID = "logvex";
    public static final String MOD_NAME = "Logvex Client";
    public static final String VERSION = "1.0.0";
    public static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MOD_NAME);

    private static LogvexClient instance;

    private final EventManager eventManager = new EventManager();
    private final ModuleManager moduleManager = new ModuleManager();
    private final HudManager hudManager = new HudManager();
    private final KeybindManager keybindManager = new KeybindManager(moduleManager);
    private ConfigManager configManager;
    private CommandManager commandManager;

    public static LogvexClient getInstance() {
        return instance;
    }

    public static EventManager getEventManager() {
        return instance == null ? null : instance.eventManager;
    }

    public static ModuleManager getModuleManager() {
        return instance == null ? null : instance.moduleManager;
    }

    public static HudManager getHudManager() {
        return instance == null ? null : instance.hudManager;
    }

    public static KeybindManager getKeybindManager() {
        return instance == null ? null : instance.keybindManager;
    }

    public static ConfigManager getConfigManager() {
        return instance == null ? null : instance.configManager;
    }

    public static CommandManager getCommandManager() {
        return instance == null ? null : instance.commandManager;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("{} {} initialising", MOD_NAME, VERSION);

        registerModules();
        registerHudElements();

        configManager = new ConfigManager(moduleManager, hudManager);
        configManager.load();
        commandManager = new CommandManager();

        ClientEvents.register(this);
        LOGGER.info("{} ready: {} modules, {} HUD elements", MOD_NAME,
                moduleManager.getAll().size(), hudManager.getElements().size());
    }

    private void registerModules() {
        ModuleRegistry.registerAll(moduleManager);
    }

    private void registerHudElements() {
        hudManager.register(new ArraylistHud());
        hudManager.register(new PerformanceHud());
        hudManager.register(new FpsHud());
        hudManager.register(new PingHud());
        hudManager.register(new CpsHud());
        hudManager.register(new CoordinatesHud());
        hudManager.register(new DirectionHud());
        hudManager.register(new BiomeHud());
        hudManager.register(new ServerHud());
        hudManager.register(new SpeedHud());
        hudManager.register(new HealthHud());
        hudManager.register(new FoodHud());
        hudManager.register(new ArmorHud());
        hudManager.register(new DurabilityHud());
        hudManager.register(new PotionHud());
        hudManager.register(new SessionHud());
        hudManager.register(new TimeHud());
        hudManager.register(new TotemHud());
        hudManager.register(new TargetHud());
        hudManager.register(new MovementHud());
        hudManager.register(new EntityCounterHud());
        hudManager.register(new InventoryHud());
        hudManager.register(new WatermarkHud());
    }
}
