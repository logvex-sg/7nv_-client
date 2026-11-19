# 7nv_ Client

An independent, client-side Fabric mod for **Minecraft Java 1.21.11** that recreates
the classic 1.8-era "hacked client" interface: dark grey panels, small readable
text, green ON / grey OFF indicators, draggable category windows, an expandable
module list, a search bar and a flat array list. No dashboards, no gradients,
no rounded corners.

* Mod ID: `sevennv`
* Package: `dev.sevennv.client`
* Environment: client only (nothing is sent to the server except your own chat)
* License: MIT

Press **Right Shift** in game to open the ClickGUI. Press **Right Control** to
open the HUD layout editor.

---

## Building

Requires JDK 21 or newer. Loom needs a JDK 25 to *run* the build; the mod itself
is compiled to Java 21 bytecode. `gradle.properties` pins
`org.gradle.java.home=/usr/lib/jvm/java-25-openjdk-amd64` — change that if your
JDK lives elsewhere.

```bash
./gradlew clean build
```

The mod JAR is written to `build/libs/sevennv-1.10.1.jar`. A sources JAR is
produced alongside it.

### Offline builds

The first build downloads Minecraft, the mappings and Fabric API into the
Gradle cache. After that you can build without a network:

```bash
./gradlew clean build --offline
```

---

## Installing

### Prism Launcher (Linux and Windows)

```bash
./scripts/prism-install.sh          # auto-detects your Prism data directory
./scripts/prism-install.sh /path/to/prism
```

The script creates a `7nv_ 1.21.11` instance, downloads Fabric Loader and
Fabric API if they are missing, drops the built JAR into `mods/` and registers
the instance. Run `./gradlew build` first.

### Manual install

1. Install Fabric Loader for 1.21.11.
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.11 in `.minecraft/mods/`.
3. Put `build/libs/sevennv-1.10.1.jar` in `.minecraft/mods/`.
4. Launch the Fabric 1.21.11 profile.

### Kali Linux setup

```bash
./scripts/kali-setup.sh
```

Installs a JDK 25 and a JDK 21 if missing, verifies the Gradle wrapper, builds
the mod and prints the JAR path. Add `--prism` to also run the Prism installer.

---

## Using the client

### Keybinds

| Key | Action |
| --- | --- |
| Right Shift | Open / close the ClickGUI |
| Right Control | Open the HUD layout editor |

Every module can be bound to its own key from the ClickGUI (click the key field,
then press a key) or with `!bind <module>` followed by a key press.

### ClickGUI

* One draggable panel per category. Drag a panel by its title bar; positions are
  saved to the config automatically.
* Click a module name to toggle it. Green means ON, grey means OFF.
* Click the arrow to expand a module and see its settings.
* Settings render as checkboxes, sliders, dropdowns, colour pickers, text fields
  and keybind rows.
* The search box filters modules across every panel. Theme presets sit next to it.
* The `Scale -` / `Scale +` buttons next to the presets resize the whole GUI
  (0.5x to 2.0x). The chosen scale is saved in `config.json`.

### HUD editor

Press Right Control, then drag elements to move them and right-click an element
to toggle it. Positions and visibility are saved when you close the editor.
The array list is moved the same way as any other element.

Available elements: array list, FPS, FPS graph, performance, ping, CPS, CPS graph,
coordinates, direction, biome, compass, server information, server IP, speed,
health, food, armour, durability, active effects, session timer, session clock,
world time, totem counter, totem count, crystal count, enemy count, target HUD,
movement, entity counter, inventory, packets, reach, TPS, lag meter, memory,
keystrokes, text radar, radar, ping graph, item count, fall distance, attack
cooldown, saturation, culling and the watermark. Each one is toggled and
positioned independently.

### Commands

Commands start with `!` so they never collide with the server's namespace. Type them
in chat (they are handled locally and never sent) or as `/!command`.

| Command | Description |
| --- | --- |
| `!toggle <module>` | Toggle a module |
| `!bind <module>` | Bind a module to the next key you press |
| `!friend <add\|remove\|list> [name]` | Manage the friend list |
| `!enemy <add\|remove\|list> [name]` | Manage the enemy list |
| `!config <save\|reload\|profile\|list\|create\|delete\|export\|import\|backup\|restore>` | Profiles, import/export and backups |
| `!hud <element\|editor>` | Toggle a HUD element, or open the layout editor |
| `!waypoint <add\|remove\|list\|clear> [name] [x y z]` | Manage stored waypoints |
| `!marker <name\|remove <name>\|clear>` | Place or clear temporary map markers |
| `!screenshot` | Capture a screenshot with the configured name |
| `!effects` | List your active status effects |
| `!ac` | Report the detected anti-cheat and server software |
| `!perf [reset]` | Report or reset the client's own dispatch timings |
| `!help` | List the commands |

---

## Modules

192 modules across seven categories, plus 57 movable HUD elements.

**Combat (30)** — Crystal Aura, Crystal Assist, Auto Totem, Auto Armor, Auto
Weapon, Surround, Hole Fill, Target HUD, Totem Counter, Crystal Counter, Damage
Calculator, Combat Notifications, Armor Durability, Weapon Information, Auto
Clicker, Reach, Criticals, Auto Block, Auto Weapon Switch, Gapple Offhand, Auto
Refill, Kill Aura, Trigger Bot, Auto Trap, Offhand Manager, Kill Tracker, Aim
Assist, Auto City, TNT Timer, Anti Anchor.

**Movement (33)** — Sprint, Auto Sprint, Elytra Utilities, Speed, Step, Safe
Walk, No Slow, Spider, Jump Assist, Movement HUD, Fall Distance, Velocity
Display, Velocity, No Fall, Anti Void, Auto Walk, Fast Ladder, Long Jump, Ice
Speed, Auto Jump Reset, No Jump Delay, Auto Jump, Auto Sneak, Gui Move, No
Levitation, Keep Sprint, Sprint Reset, No Web, Water Speed, Elytra Boost, Auto
Swim, Auto Parkour, Bunny Hop.

**Render (41)** — Player ESP, Mob ESP, Storage ESP, Item ESP, Entity ESP,
Tracers, Nametags, Fullbright, Hole ESP, Chunk Borders, Entity Boxes, Health
Bars, Projectile Trails, Custom Crosshair, Block Highlight, Logout Spots, Zoom,
Freecam, Camera Clip, View Model, Anti Fog, No Render, No Hurt Cam, Aspect
Ratio, Anti Blind, Damage Tint, No Weather, Trajectories, Breadcrumbs, Item
Frame ESP, Search ESP, No Bob, No Rotate, True Sight, Entity Culling, Block Entity
Culling, Pixel Blocks, Fast Graphics, Particle Limiter, Entity Distance, Dynamic
Render Distance.

**World (25)** — Waypoints, Death Coordinates, Stash Notes, Portal Information,
Chunk Information, Biome Display, Coordinates, Map Markers, World Time,
Container Information, Entity Counter, Auto Tool, Fast Place, Fast Break, Auto
Mine, Chunk Finder, Stop Watch, Item Notifier, Spawner Finder, Chest Tracker,
Sign Reader, Spawn ESP, Distance Tracker, Scaffold, Nuker.

**Player (24)** — Inventory Manager, Auto Eat, Armor HUD, Inventory HUD, Potion
HUD, Totem HUD, Health HUD, Durability HUD, Item Information, Status Effects,
Auto Respawn, Auto Log, Anti Hunger, Auto Pot, Refill, Auto Armor Switch, Chest
Stealer, Auto Fish, Fast Use, Auto Tool Switch, Auto Drop, Auto Milk, Auto Exp, Health Warning.

**Misc (23)** — Auto EZ, Auto GG, Auto Reconnect, Chat Timestamps, Chat Suffix, Chat Macros,
Screenshot Manager, Session Statistics, Friend List, Enemy List, Notifications,
Module Search, Auto Tip, Announcer, Item Drop Guard, Chat Filter, Auto Shear,
Auto Breed, Auto Mount, Auto Bucket, Chat Logger, Anti AFK, Middle Click.

**Diagnostics (16)** — Packet Inspector, Packet Statistics, Protocol
Information, Connection Diagnostics, Server Capability Detection, Anti Cheat
Detector, Detection Exposure, Desync Detector, Chunk Loading Diagnostics, Entity
Metadata Inspector, Client State Inspector, Ping History, Entity Diagnostics,
Frame Time, Packet Rate, Frame Profiler.

Every diagnostics overlay has its own `X`/`Y` setting, so several can be open at
once without covering each other. Packet Rate reports peak and spike counts and
names the busiest packet type during a spike, which is what tells you whether a
burst came from chunk streaming or entities.

A handful of modules hook the render pipeline through mixins (Zoom, Freecam,
Camera Clip, View Model, Anti Fog, No Render, No Hurt Cam, Aspect Ratio, Anti
Blind, Damage Tint, Fast Place, Fast Break, Velocity, No Jump Delay, Auto Fish,
No Weather, Entity Culling, Block Entity Culling, Pixel Blocks, Particle Limiter).

Each one only touches local client state — a projection matrix, a fog distance, a
camera transform, a tesselation pose or a local cooldown — and never sends
anything the server did not already expect.

No Rotate is camera-only. It measures the jump a server rotation correction would
cause and eases the view back with a decaying offset, but the player entity keeps
exactly the rotation the server sent. Nothing is spoofed, no packet is cancelled
and the position the client reports is unchanged — it is a comfort feature, not a
combat bypass.

### Performance

Performance modules are **always-on**. They cannot be switched off from the
ClickGUI (they show a green lock), they start enabled on a fresh install, and
they survive a disconnect. They only ever cut work the client does; none of them
changes what the server sees, and none changes what you can and cannot see in a
way that matters (a distant entity still exists — it is just not drawn).

* **Entity Culling** — adds a distance cap and a view-cone test on top of the
  vanilla frustum check. Entities you are looking at, nametagged or glowing
  entities and players inside the keep range are never culled.
* **Block Entity Culling** — skips render-state extraction for distant or
  fully-buried chests, signs and the like, with per-type keeps (chests, signs,
  beacons, spawners) and a keep-looked-at guard.
* **Particle Limiter** — caps particles spawned per frame and drops particles
  spawned far from the camera. Explosion, totem and portal particles are kept by
  default because they carry information. A generous budget means ordinary play
  is unchanged; only a burst is trimmed.

These stay **toggleable** because they change what you see, so you decide when to
trade looks for frames:

* **Pixel Blocks** — shrinks each block model slightly so neighbouring blocks
  read as a thin 1px outline, the classic 1.8 look. Collision, light and
  hitboxes are untouched. Solid, cutout and translucent layers each toggle
  separately, and the shrink can fade out with distance.
* **Fast Graphics** — one switch for cheap local options (clouds off, fewer
  particles, ambient occlusion off, optional entity shadows). It restores your
  old values when switched off.
* **Dynamic Render Distance** — nudges the render distance down when FPS is low
  and back up when there is headroom, with hysteresis and a cooldown.
* **Entity Distance** — pulls the entity-draw distance in, optionally lowering it
  further when the frame rate drops and recovering when there is headroom.

The Culling HUD element shows entities culled this frame and the current render
distance, and the FPS Graph element plots the last few seconds of frame rate.
Culling only ever skips something the client was about to draw; it never forces a
draw vanilla skipped.

### Auto EZ and Auto GG

Both share the same message engine. You get custom templates with placeholders
(`{name}`, `{kills}`, `{health}`, ...), multiple message profiles, per-trigger
enable flags, a configurable delay before sending, a cooldown between messages,
an anti-spam guard (the message is dropped if the server rate-limits you) and a
live preview of the message that would be sent.

Triggers: kill credit (a player you damaged died), observed death (any nearby
player death), low health, and local death for Auto GG.

### Diagnostics

The diagnostics modules are **read-only observers**. They count and display
packets, report the negotiated protocol version, show connection state and
detect desync and chunk-loading stalls. They never send anything, never modify
traffic and never attempt to bypass server protections.

**Anti Cheat Detector** names the anti-cheat, proxy and server software the
current connection is running. It is passive: it reads only data the server
already sends — the brand string, the plugin channels it registers, the command
tree it advertises, the tab-list header/footer and the scoreboard objective
names — and matches them against a built-in knowledge base (Vulcan, Grim, Matrix,
Spartan, Intave, Verus, AAC, NoCheatPlus, Polar, Reflex, Horizon, Karhu, Themis,
Negativity, OpenEye and more; proxies such as Velocity and BungeeCord; server
flavours such as Paper, Purpur, Folia, Fabric and Forge; and plugins such as
ViaVersion, Geyser, LuckPerms, WorldEdit and PlaceholderAPI). Each match carries
a confidence and, optionally, the signal it came from. It sends no probe packet
and never touches the server's view of you. `!ac` prints the same summary in
chat, and the movable **Anti-cheat** HUD element shows the top match.

**Detection Exposure** is a self-audit of *your own* footprint: for every
enabled module it estimates how far the module diverges from vanilla client
behaviour (rotation, attack timing, physics-defying movement, packet rewriting,
automated inventory or block actions) and ranks them High / Medium / Low. It is
a heuristic, not a prediction of any specific server's rules, and it offers no
bypass — it only shows you what you are running. It can also fold in the
server-side signals (latency, jitter, TPS) from the connection diagnostics.

---

## Configuration

Everything lives under `.minecraft/config/sevennv/`:

```
config/sevennv/
├── config.json              global settings: theme, GUI layout, HUD layout
├── profiles/
│   ├── default.json         module states, settings and keybinds
│   ├── anarchy.json
│   └── crystal.json
└── backups/
    └── pre-switch-*.json    timestamped backups
```

* Saves are coalesced: a burst of changes writes once every two seconds, and
  the file is also flushed on disconnect.
* **Profiles** hold the module states, settings and keybinds. Switch with
  `!config profile <name>` or from the ClickGUI.
* **Backups** are taken before a profile switch and before a restore.
* **Import / export** copies a profile in or out of the config root.

Example profiles ship in `config-examples/` — copy them into
`config/sevennv/profiles/` to use them:

* `default.json` — a light, safe setup.
* `anarchy.json` — full render and combat set for anarchy servers.
* `crystal.json` — crystal PvP focus.

---

## Architecture

```
dev.sevennv.client
├── SevennvClient              mod entrypoint, wires the managers
├── ClientEvents              Fabric event callbacks, tick/packet/chat plumbing
├── combat/  movement/  render/  world/  player/  misc/  diagnostics/
├── hud/                      HUD elements plus HudManager
├── gui/                      ClickGUI, category panels, settings widgets
├── config/                   ConfigManager, settings types, themes
├── events/                   EventManager and event types
├── module/                   Module base class, ModuleManager, keybinds
├── command/                  chat commands (! prefix)
├── mixin/                    packet hooks and the bind capture
└── utils/                    chat, packets, combat tracking, notifications
```

* **ModuleManager** owns every module, ticks the enabled ones and routes
  packets, chat and world events to them.
* **EventManager** is a small typed event bus for cross-module messages.
* **ConfigManager** owns the JSON on disk and the save/backup/profile logic.
* **KeybindManager** polls module keybinds with its own edge detection rather
  than vanilla `KeyMapping`, so binding a module to `E` or `R` does not steal
  the vanilla action.

Packets are recorded on the Netty thread and drained on the client thread, so
modules never touch game state off-thread. World changes and disconnects disable
modules that depend on a world (Auto Reconnect opts out) and reset all
per-session state.

---

## Performance

A client with 192 modules has to be careful about what it does per frame. The
dispatch layer is built around that:

* **Enabled-only dispatch.** `ModuleManager` keeps pre-filtered lists of the
  modules that actually implement each hook, so a disabled module — or an enabled
  module that does not override a hook — is never called. The lists are rebuilt
  only when a module is toggled or rebound, not every tick.
* **No-op overrides are skipped.** Each module records at construction which
  hooks it overrides; a module with no `onTick` is not in the tick list at all.
* **World scans are throttled.** Block-scanning modules (Storage ESP, Search ESP,
  Hole ESP, Portal Information, Sign Reader, Container Information, Mob Spawn
  ESP) cache their results and rescan on a configurable interval rather than
  every frame, and they sweep with a single reused `MutableBlockPos` instead of
  allocating a position per block.
* **The array list caches its labels.** Suffix strings are recomputed at most
  four times a second, so a module that counts chunks cannot stall a frame.
* **Packet capture is opt-in by consumer.** The packet log keeps a reusable
  buffer and memoises packet class names, and the packet dispatcher is skipped
  entirely when no enabled module listens to packets.

To see what the client itself is costing, enable **Frame Profiler** (Diagnostics)
or run `!perf`. Both report the time spent inside Logvex's own tick, 2D render
and world render loops, which separates "the client is heavy" from "the game is
heavy". Profiling is off unless you ask for it.

## Limitations

* Client-side only. Nothing here changes server-side behaviour, and features
  that depend on server data (latency, chunk state) are only as accurate as what
  the server sends.
* The world renderer uses the 1.21.9+ Gizmo collector, so ESP-style rendering is
  box/line based rather than textured overlays.
* Auto EZ, Auto GG and chat macros send **your** messages from **your** account;
  use them within the rules of whatever server you play on.
* Diagnostics are observational. There is deliberately no anti-cheat bypass, no
  DoS, no crash and no credential code anywhere in this project. The Anti Cheat
  Detector and Detection Exposure modules only *read* what the server already
  sends and *describe* it — they never probe the server, send an extra packet or
  attempt to evade anything.
* Performance modules (Entity Culling, Block Entity Culling, Particle Limiter)
  are always-on and cannot be switched off; the visual modules (Pixel Blocks,
  Fast Graphics, Dynamic Render Distance, Entity Distance) remain toggleable so
  you control the looks-for-frames trade.
* Scaffold and Nuker place and break blocks through the ordinary interaction and
  destroy paths, so the server's own reach, break-time and anti-cheat rules still
  apply. They are conveniences, not world-edits.

## License

MIT — see [LICENSE](LICENSE).
