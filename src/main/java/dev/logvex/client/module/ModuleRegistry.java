package dev.logvex.client.module;

import dev.logvex.client.combat.ArmorDurability;
import dev.logvex.client.combat.AutoArmor;
import dev.logvex.client.combat.AutoTotem;
import dev.logvex.client.combat.AutoWeapon;
import dev.logvex.client.combat.CombatNotifications;
import dev.logvex.client.combat.CrystalAssist;
import dev.logvex.client.combat.CrystalAura;
import dev.logvex.client.combat.CrystalCounter;
import dev.logvex.client.combat.DamageCalculator;
import dev.logvex.client.combat.HoleFill;
import dev.logvex.client.combat.Surround;
import dev.logvex.client.combat.TargetHud;
import dev.logvex.client.combat.TotemCounter;
import dev.logvex.client.combat.WeaponInformation;
import dev.logvex.client.diagnostics.ChunkLoadingDiagnostics;
import dev.logvex.client.diagnostics.ClientStateInspector;
import dev.logvex.client.diagnostics.ConnectionDiagnostics;
import dev.logvex.client.diagnostics.DesyncDetector;
import dev.logvex.client.diagnostics.EntityMetadataInspector;
import dev.logvex.client.diagnostics.PacketInspector;
import dev.logvex.client.diagnostics.PacketStatistics;
import dev.logvex.client.diagnostics.ProtocolInformation;
import dev.logvex.client.diagnostics.ServerCapabilityDetection;
import dev.logvex.client.misc.AutoEz;
import dev.logvex.client.misc.AutoGg;
import dev.logvex.client.misc.AutoReconnect;
import dev.logvex.client.misc.ChatMacros;
import dev.logvex.client.misc.ChatTimestamps;
import dev.logvex.client.misc.EnemyList;
import dev.logvex.client.misc.FriendList;
import dev.logvex.client.misc.ModuleSearch;
import dev.logvex.client.misc.Notifications;
import dev.logvex.client.misc.ScreenshotManager;
import dev.logvex.client.misc.SessionStatistics;
import dev.logvex.client.movement.AutoSprint;
import dev.logvex.client.movement.ElytraUtils;
import dev.logvex.client.movement.FallDistance;
import dev.logvex.client.movement.JumpAssist;
import dev.logvex.client.movement.MovementHud;
import dev.logvex.client.movement.NoSlow;
import dev.logvex.client.movement.SafeWalk;
import dev.logvex.client.movement.Speed;
import dev.logvex.client.movement.Spider;
import dev.logvex.client.movement.Sprint;
import dev.logvex.client.movement.StepHeight;
import dev.logvex.client.movement.VelocityDisplay;
import dev.logvex.client.player.ArmorHud;
import dev.logvex.client.player.AutoEat;
import dev.logvex.client.player.DurabilityHud;
import dev.logvex.client.player.HealthHud;
import dev.logvex.client.player.InventoryHud;
import dev.logvex.client.player.InventoryManager;
import dev.logvex.client.player.ItemInformation;
import dev.logvex.client.player.PotionHud;
import dev.logvex.client.player.StatusEffects;
import dev.logvex.client.player.TotemHud;
import dev.logvex.client.render.BlockHighlight;
import dev.logvex.client.render.ChunkBorders;
import dev.logvex.client.render.CustomCrosshair;
import dev.logvex.client.render.EntityBoxes;
import dev.logvex.client.render.EntityEsp;
import dev.logvex.client.render.Fullbright;
import dev.logvex.client.render.HealthBars;
import dev.logvex.client.render.HoleEsp;
import dev.logvex.client.render.ItemEsp;
import dev.logvex.client.render.LogoutSpots;
import dev.logvex.client.render.MobEsp;
import dev.logvex.client.render.Nametags;
import dev.logvex.client.render.PlayerEsp;
import dev.logvex.client.render.ProjectileTrails;
import dev.logvex.client.render.StorageEsp;
import dev.logvex.client.render.Tracers;
import dev.logvex.client.world.BiomeDisplay;
import dev.logvex.client.world.ChunkInformation;
import dev.logvex.client.world.ContainerInformation;
import dev.logvex.client.world.Coordinates;
import dev.logvex.client.world.DeathCoordinates;
import dev.logvex.client.world.EntityCounter;
import dev.logvex.client.world.MapMarkers;
import dev.logvex.client.world.PortalInformation;
import dev.logvex.client.world.StashNotes;
import dev.logvex.client.world.Waypoints;
import dev.logvex.client.world.WorldTime;

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
