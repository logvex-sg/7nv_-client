package dev.sevennv.client.module;

import dev.sevennv.client.combat.ArmorDurability;
import dev.sevennv.client.combat.AutoArmor;
import dev.sevennv.client.combat.AutoTotem;
import dev.sevennv.client.combat.AutoWeapon;
import dev.sevennv.client.combat.CombatNotifications;
import dev.sevennv.client.combat.CrystalAssist;
import dev.sevennv.client.combat.CrystalAura;
import dev.sevennv.client.combat.CrystalCounter;
import dev.sevennv.client.combat.DamageCalculator;
import dev.sevennv.client.combat.HoleFill;
import dev.sevennv.client.combat.Surround;
import dev.sevennv.client.combat.TargetHud;
import dev.sevennv.client.combat.TotemCounter;
import dev.sevennv.client.combat.WeaponInformation;
import dev.sevennv.client.diagnostics.ChunkLoadingDiagnostics;
import dev.sevennv.client.diagnostics.ClientStateInspector;
import dev.sevennv.client.diagnostics.ConnectionDiagnostics;
import dev.sevennv.client.diagnostics.DesyncDetector;
import dev.sevennv.client.diagnostics.EntityMetadataInspector;
import dev.sevennv.client.diagnostics.PacketInspector;
import dev.sevennv.client.diagnostics.PacketStatistics;
import dev.sevennv.client.diagnostics.ProtocolInformation;
import dev.sevennv.client.diagnostics.ServerCapabilityDetection;
import dev.sevennv.client.misc.AutoEz;
import dev.sevennv.client.misc.AutoGg;
import dev.sevennv.client.misc.AutoReconnect;
import dev.sevennv.client.misc.ChatMacros;
import dev.sevennv.client.misc.ChatTimestamps;
import dev.sevennv.client.misc.EnemyList;
import dev.sevennv.client.misc.FriendList;
import dev.sevennv.client.misc.ModuleSearch;
import dev.sevennv.client.misc.Notifications;
import dev.sevennv.client.misc.ScreenshotManager;
import dev.sevennv.client.misc.SessionStatistics;
import dev.sevennv.client.movement.AutoSprint;
import dev.sevennv.client.movement.ElytraUtils;
import dev.sevennv.client.movement.FallDistance;
import dev.sevennv.client.movement.JumpAssist;
import dev.sevennv.client.movement.MovementHud;
import dev.sevennv.client.movement.NoSlow;
import dev.sevennv.client.movement.SafeWalk;
import dev.sevennv.client.movement.Speed;
import dev.sevennv.client.movement.Spider;
import dev.sevennv.client.movement.Sprint;
import dev.sevennv.client.movement.StepHeight;
import dev.sevennv.client.movement.VelocityDisplay;
import dev.sevennv.client.player.ArmorHud;
import dev.sevennv.client.player.AutoEat;
import dev.sevennv.client.player.DurabilityHud;
import dev.sevennv.client.player.HealthHud;
import dev.sevennv.client.player.InventoryHud;
import dev.sevennv.client.player.InventoryManager;
import dev.sevennv.client.player.ItemInformation;
import dev.sevennv.client.player.PotionHud;
import dev.sevennv.client.player.StatusEffects;
import dev.sevennv.client.player.TotemHud;
import dev.sevennv.client.render.BlockHighlight;
import dev.sevennv.client.render.ChunkBorders;
import dev.sevennv.client.render.CustomCrosshair;
import dev.sevennv.client.render.EntityBoxes;
import dev.sevennv.client.render.EntityEsp;
import dev.sevennv.client.render.Fullbright;
import dev.sevennv.client.render.HealthBars;
import dev.sevennv.client.render.HoleEsp;
import dev.sevennv.client.render.ItemEsp;
import dev.sevennv.client.render.LogoutSpots;
import dev.sevennv.client.render.MobEsp;
import dev.sevennv.client.render.Nametags;
import dev.sevennv.client.render.PlayerEsp;
import dev.sevennv.client.render.ProjectileTrails;
import dev.sevennv.client.render.StorageEsp;
import dev.sevennv.client.render.Tracers;
import dev.sevennv.client.world.BiomeDisplay;
import dev.sevennv.client.world.ChunkInformation;
import dev.sevennv.client.world.ContainerInformation;
import dev.sevennv.client.world.Coordinates;
import dev.sevennv.client.world.DeathCoordinates;
import dev.sevennv.client.world.EntityCounter;
import dev.sevennv.client.world.MapMarkers;
import dev.sevennv.client.world.PortalInformation;
import dev.sevennv.client.world.StashNotes;
import dev.sevennv.client.world.Waypoints;
import dev.sevennv.client.world.WorldTime;

/**
 * Registers every module with the {@link ModuleManager}. Kept in one place so
 * the module list is easy to audit against the design document.
 */
public final class ModuleRegistry {
    private ModuleRegistry() {
    }

    public static void registerAll(ModuleManager manager) {
        manager.registerAll(
                // Combat
                new CrystalAura(),
                new CrystalAssist(),
                new AutoTotem(),
                new AutoArmor(),
                new AutoWeapon(),
                new Surround(),
                new HoleFill(),
                new TargetHud(),
                new TotemCounter(),
                new CrystalCounter(),
                new DamageCalculator(),
                new CombatNotifications(),
                new ArmorDurability(),
                new WeaponInformation(),

                // Movement
                new Sprint(),
                new AutoSprint(),
                new ElytraUtils(),
                new Speed(),
                new StepHeight(),
                new SafeWalk(),
                new NoSlow(),
                new Spider(),
                new JumpAssist(),
                new MovementHud(),
                new FallDistance(),
                new VelocityDisplay(),

                // Render
                new PlayerEsp(),
                new MobEsp(),
                new StorageEsp(),
                new ItemEsp(),
                new EntityEsp(),
                new Tracers(),
                new Nametags(),
                new Fullbright(),
                new HoleEsp(),
                new ChunkBorders(),
                new EntityBoxes(),
                new HealthBars(),
                new ProjectileTrails(),
                new CustomCrosshair(),
                new BlockHighlight(),
                new LogoutSpots(),

                // World
                new Waypoints(),
                new DeathCoordinates(),
                new StashNotes(),
                new PortalInformation(),
                new ChunkInformation(),
                new BiomeDisplay(),
                new Coordinates(),
                new MapMarkers(),
                new WorldTime(),
                new ContainerInformation(),
                new EntityCounter(),

                // Player
                new InventoryManager(),
                new AutoEat(),
                new ArmorHud(),
                new InventoryHud(),
                new PotionHud(),
                new TotemHud(),
                new HealthHud(),
                new DurabilityHud(),
                new ItemInformation(),
                new StatusEffects(),

                // Miscellaneous
                new AutoEz(),
                new AutoGg(),
                new AutoReconnect(),
                new ChatTimestamps(),
                new ChatMacros(),
                new ScreenshotManager(),
                new SessionStatistics(),
                new FriendList(),
                new EnemyList(),
                new Notifications(),
                new ModuleSearch(),

                // Diagnostics
                new PacketInspector(),
                new PacketStatistics(),
                new ProtocolInformation(),
                new ConnectionDiagnostics(),
                new ServerCapabilityDetection(),
                new DesyncDetector(),
                new ChunkLoadingDiagnostics(),
                new EntityMetadataInspector(),
                new ClientStateInspector()
        );
    }
}
