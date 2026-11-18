package dev.sevennv.client;

import dev.sevennv.client.command.CommandManager;
import dev.sevennv.client.config.ConfigManager;
import dev.sevennv.client.events.EventManager;
import dev.sevennv.client.hud.ArmorHud;
import dev.sevennv.client.hud.ArraylistHud;
import dev.sevennv.client.hud.BiomeHud;
import dev.sevennv.client.hud.CoordinatesHud;
import dev.sevennv.client.hud.CpsHud;
import dev.sevennv.client.hud.DirectionHud;
import dev.sevennv.client.hud.DurabilityHud;
import dev.sevennv.client.hud.EntityCounterHud;
import dev.sevennv.client.hud.FoodHud;
import dev.sevennv.client.hud.FpsHud;
import dev.sevennv.client.hud.HealthHud;
import dev.sevennv.client.hud.HudManager;
import dev.sevennv.client.hud.InventoryHud;
import dev.sevennv.client.hud.MovementHud;
import dev.sevennv.client.hud.PerformanceHud;
import dev.sevennv.client.hud.PingHud;
import dev.sevennv.client.hud.PotionHud;
import dev.sevennv.client.hud.ServerHud;
import dev.sevennv.client.hud.SessionHud;
import dev.sevennv.client.hud.SpeedHud;
import dev.sevennv.client.hud.TargetHud;
import dev.sevennv.client.hud.TimeHud;
import dev.sevennv.client.hud.TotemHud;
import dev.sevennv.client.hud.WatermarkHud;
import dev.sevennv.client.hud.CompassHud;
import dev.sevennv.client.hud.CpsGraphHud;
import dev.sevennv.client.hud.CrystalHud;
import dev.sevennv.client.hud.EnemyCountHud;
import dev.sevennv.client.hud.KeystrokesHud;
import dev.sevennv.client.hud.LagHud;
import dev.sevennv.client.hud.MemoryHud;
import dev.sevennv.client.hud.PacketHud;
import dev.sevennv.client.hud.RadarHud;
import dev.sevennv.client.hud.ReachHud;
import dev.sevennv.client.hud.ServerIpHud;
import dev.sevennv.client.hud.SessionTimerHud;
import dev.sevennv.client.hud.TotemCountHud;
import dev.sevennv.client.hud.TextRadarHud;
import dev.sevennv.client.hud.TpsHud;
import dev.sevennv.client.module.KeybindManager;
import dev.sevennv.client.module.ModuleManager;
import dev.sevennv.client.module.ModuleRegistry;
import net.fabricmc.api.ClientModInitializer;

/**
 * Entry point. Builds the managers in dependency order, registers every
 * module and HUD element, then hands control to the event bridges.
 */
public final class SevennvClient implements ClientModInitializer {
    public static final String MOD_ID = "sevennv";
    public static final String MOD_NAME = "7nv_ Client";
    public static final String VERSION = "1.0.0";
    public static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MOD_NAME);

    private static SevennvClient instance;

    private final EventManager eventManager = new EventManager();
    private final ModuleManager moduleManager = new ModuleManager();
    private final HudManager hudManager = new HudManager();
    private final KeybindManager keybindManager = new KeybindManager(moduleManager);
    private ConfigManager configManager;
    private CommandManager commandManager;

    public static SevennvClient getInstance() {
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
        hudManager.register(new TpsHud());
        hudManager.register(new LagHud());
        hudManager.register(new KeystrokesHud());
        hudManager.register(new RadarHud());
        hudManager.register(new PacketHud());
        hudManager.register(new ReachHud());
        hudManager.register(new MemoryHud());
        hudManager.register(new CompassHud());
        hudManager.register(new CrystalHud());
        hudManager.register(new TotemCountHud());
        hudManager.register(new EnemyCountHud());
        hudManager.register(new ServerIpHud());
        hudManager.register(new SessionTimerHud());
        hudManager.register(new CpsGraphHud());
        hudManager.register(new TextRadarHud());
    }
}
