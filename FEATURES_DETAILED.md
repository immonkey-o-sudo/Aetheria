# Aetheria - Detailed Feature Reference

> **Complete breakdown of all user-facing features with technical implementation details**

---

## 📋 Table of Contents

| Category | Features | Commands |
|----------|----------|----------|
| [QoL](#-qol-quality-of-life) | 22 | `/asmbuttons`, `/asmtimer`, `/athrcalc` |
| [Misc](#-miscellaneous) | 24 | `/asmprotect` |
| [Dungeons](#-dungeons) | 9 | `/diana`, `/pdt`, `/prt` |
| [Mining](#-mining) | 7 | — |
| [Fishing](#-fishing) | 2 | — |
| [Overlays](#-overlays) | 1 | `/pv`, `/sync` |
| [Diana](#-diana-event) | 5 | `/diana` |
| [Farming](#-farming) | 2 | `/lockmouse` |
| [Scoreboard](#-scoreboard) | 1 | — |
| [Chat](#-chat) | 8 | `/chatfilters` |
| [Cosmetics](#-cosmetics) | 1 | `/capes` |
| [Storage](#-storage) | 3 | — |
| [Waypoints](#-waypoints) | 3 | `/waypoint`, `/athrw` |
| [Core Commands](#-core-commands) | 16 | `/asm`, `/athr`, `/jef` |

---

## 🎯 QoL (Quality of Life)

| Feature | Description | Implementation |
|---------|-------------|----------------|
| **Block Selection Overlay** | Custom filled/outline highlight replacing vanilla block selection | `BlockOverlay.java` - renders custom block highlight with configurable style |
| **Enchant Parser** | Colors enchants by level, sorts ultimates to top, normal/compressed/expanded layout, chroma animation | `EnchantProcessor.java`, `EnchantChromaRenderer.java`, `MissingEnchants.java`, `EnchantLevelTip.java` |
| **Gyro Wand Helper** | Shows AoE ring + cooldown timer when holding Gyrokinetic Wand | `GyroWandHelper.java` - world render + HUD timer |
| **Roman Numerals** | Converts Roman numerals to integers in tooltips/chat | Part of `EnchantProcessor.java` / tooltip processing |
| **Prevent Cursor Reset** | Stops mouse cursor from resetting when opening GUIs | `CursorResetHandler.java` - mixin into mouse handling |
| **Skyblock ID** | Shows internal SkyBlock item ID at bottom of tooltips | `SkyblockTooltips.java` - appends ID to tooltip |
| **Disable Enchant Glint** | Removes enchantment glint effect | Mixin disabling glint render |
| **Brewing Helper** | Highlights brewing stands with recipe info | `BrewingStandHelper.java` - GUI overlay on brewing stand |
| **Missing Enchants** | Hold Shift on enchanted item to see missing enchants | `MissingEnchants.java` - Shift tooltip augmentation |
| **Confirm Disconnect** | Double-click to disconnect prevention | `ConfirmDisconnect.java` - GUI screen wrapper |
| **Chat State Restore** | Restores chat text when server closes chat | `ChatStateManager.java` - caches/restores chat buffer |
| **Anvil Combine Helper** | Highlights matching items when one anvil slot filled | `AnvilCombineHelper.java` - anvil GUI overlay |
| **Slot Binds** | Bind inventory slots to hotbar (N:1 unidirectional) | Keybind system + container slot mapping |
| **Better Containers** | Improved SkyBlock menu backgrounds, styles, watermark | `BetterContainers.java` - GUI texture replacements |
| **Damage Formatter** | Shortens large numbers (1,234,567 → 1.2M) | `DamageNameplates.java` - number formatting on damage events |
| **Profile Parser (SkyAtlas)** | Parse profiles for SkyAtlas web viewer | `ProfileParser.java`, `ProfileCompressor.java` - JSON export |
| **Search Bar** | Search bar in inventory GUIs with item highlighting | `SearchBar.java` - GUI text filter + highlight render |
| **Item Cooldowns** | Tracks ability cooldowns + invincibility timers with HUD | `ItemCooldowns.java`, `ItemAbilityTimers.java`, `ItemInvincibilityTimers.java` + overlay renderers |
| **Current Pet** | Active pet as HUD overlay | `CurrentPetTracker.java`, `CurrentPetOverlay.java`, `CurrentPetApi.java`, `PetCache.java` |
| **Item Pickup Log** | Recently picked up/dropped items HUD | `ItemPickupLog.java`, `ItemLogAlerts.java` - event listener + rolling log |
| **Inventory Buttons** | Clickable shortcut buttons in inventories | `GuiButtonManager.java` (implied) - `/asmbuttons` editor |
| **Item Stack Tips** | Enchant levels on books, floor numbers on passes | `SkyblockTooltips.java` / `EnchantLevelTip.java` |
| **Party Finder Floor Labels** | Shows F1–F7, M1–M7, ENT on party listings | `PartyFinderFloorTip.java` - scoreboard/name parsing |
| **Skill XP Display** | Hold Shift on skill item for XP to max | `SkillXpDisplay.java` - tooltip augmentation |
| **No Swap Animation** | Removes item lowering animation on hotbar swap | Mixin into `PlayerControllerMP` |
| **Show Own Nametag** | Shows your nametag in third person | Mixin into `RendererLivingEntity` |
| **Disable Entity Fire** | Hides fire overlay on burning entities | Mixin into entity render |
| **SkyBlock XP in Chat** | Sends action bar XP gains to chat (needs server support) | `SkyblockXpInChat.java`, `ActionBarDispatcher.java` |
| **DVD Screensaver** | Bouncing DVD logo screensaver | Overlay implementation (idle detection) |
| **Hoppity Rabbit Highlight** | Highlights NEW rabbits in Hoppity | Entity highlight + name detection |
| **ATHRProtect** | Item protection from drops/sales | `/asmprotect` - NBT tag + event cancellation |
| **Sign Calculator** | Advanced calculator with expressions in signs | `CalcCommand.java` - expression parser |
| **Timer** | Countdown HUD with pause/resume/cancel | `TimerCommand.java`, `UptimeManager.java`, `UptimeOverlay.java` |
| **Item List & Recipe Viewer** | Browse SkyBlock items + crafting recipes in-game | `SkyblockItem.java`, `ItemFamily.java`, `RecipeViewerGUI.java`, `WikiPane.java` |
| **Player Join/Leave Notifier** | Alerts for watched players; custom messages | `PlayerJoinLeaveNotifier.java` - tab list tracking |
| **Bazaar Order Highlights** | Gold highlight sell orders, green highlight buy orders | `PriceDetector.java` + GUI tooltip/overlay modification |

---

## 📦 Miscellaneous

| Feature | Description | Implementation |
|---------|-------------|----------------|
| **Performance HUD** | FPS, TPS, ping, coords, rotation overlay | `PerformanceHUD.java` - `Overlay` extension |
| **Item Cooldowns** | Ability/invincibility cooldown HUD | See QoL section |
| **Current Pet** | Active pet HUD overlay | See QoL section |
| **Item Pickup Log** | Recent pickup/drop HUD | See QoL section |
| **Inventory Buttons** | Shortcut buttons in inventories | See QoL section |
| **Item Stack Tips** | Enchant levels, floor numbers | See QoL section |
| **Party Finder Floor Labels** | F1–F7/M1–M7/ENT on listings | See QoL section |
| **Skill XP Display** | XP to max on Shift+hover | See QoL section |
| **No Swap Animation** | Remove hotbar swap animation | See QoL section |
| **Show Own Nametag** | Nametag in third person | See QoL section |
| **Disable Entity Fire** | Hide fire overlay | See QoL section |
| **SkyBlock XP in Chat** | Action bar XP → chat | See QoL section |
| **DVD Screensaver** | Bouncing DVD logo | See QoL section |
| **Hoppity Rabbit Highlight** | Highlight NEW rabbits | See QoL section |
| **ATHRProtect** | Item protection system | See QoL section |
| **Sign Calculator** | Expression calculator in signs | See QoL section |
| **Timer** | Countdown HUD with controls | See QoL section |
| **Item List & Recipe Viewer** | Browse items + recipes | See QoL section |
| **Player Join/Leave Notifier** | Watched player alerts | See QoL section |
| **Bazaar Order Highlights** | Color-coded order highlights | See QoL section |

---

## 🏰 Dungeons

| Feature | Description | Implementation |
|---------|-------------|----------------|
| **Blood Mob Highlight** | Box/glow highlight on blood room mobs | `BloodMobHighlight.java` - entity render mixin |
| **Boss Highlights** | Configurable colors for Bonzo, Scarf, minions, Professor | `BossHighlight.java` - per-boss highlight config |
| **Dungeon Overlay** | Run timers + end-of-run stats in chat | `DungeonStats.java` + `DungeonTimers`, `DungeonEndStats`, `PBTracker`, `PhaseDetector`, `PhaseOverlay`, `StatsPrinter` |
| **Dungeon Breaker Overlay** | Breaker charges HUD in dungeon | `DungeonBreakerOverlay.java` - scoreboard/entity tracking |
| **Dungeon Room Overlay** | Current room name display | `DungeonRoomDetector.java`, `DungeonRoom.java`, `SecretRenderUtils.java` |
| **CSGO Chest Opening** | CS:GO-style crate animation for obsidian/bedrock chests | `ChestListener.java`, `CustomDropAnimationGui.java`, `GuiInterceptChest.java`, `CitManager.java`, `FloatFontRenderer.java` |
| **Hide Blessing Messages** | Suppresses blessing chat spam | `HideBlessing.java` - chat event cancellation |
| **Dungeon Map Overlay** | Minimap-style dungeon layout | `DungeonMapOverlay.java` - room graph rendering |
| **Water Puzzle Solver** | Auto-solves water board puzzle | `WaterSolver.java` - algorithm + overlay hints |
| **Reward Analyzer** | Real-time reward profit estimation | `DungeonReward.java`, `DungeonRewardProfitEstimator.java`, `RewardAnalyzerOverlay.java`, `RewardEstimate.java` |
| **Dungeon Pass Floor Tips** | Floor-specific tips on entry | `DungeonPassFloorTip.java` - chat/scoreboard detection |

---

## ⛏️ Mining

| Feature | Description | Implementation |
|---------|-------------|----------------|
| **Fetchur Overlay** | Today's Fetchur item display | `FetchurOverlay.java`, `FetchurData.java` - NPC trade data |
| **Powder Tracker** | Gemstone powder, chest drops, goblin eggs (excludes PRISTINE) | `PowderTracker.java`, `PowderStats.java`, `PowderOverlay.java`, `PowderData.java`, `PowderDisplayEntry.java` |
| **Pristine Tracker** | Dedicated PRISTINE gemstone drops with rates/hour | `PristineTracker.java`, `PristineStats.java`, `PristineData.java`, `PristineOverlay.java` |
| **HOTM Powder Display** | Powder spent vs max cost on perk tooltips; Shift for next 10 levels | `HotmPowderDisplay.java`, `HotmPerkData.java`, `CoreOfTheMountainData.java` |
| **Commission Highlight** | Green highlight completed commissions | GUI tooltip/commission menu modification |
| **Pickobulus Preview** | Wireframe cube preview of blast radius | `PickobulusPreview.java` - world render preview |

---

## 🎣 Fishing

| Feature | Description | Implementation |
|---------|-------------|----------------|
| **Trophy Fish Tracker** | Tracks counts with overlay, chat formatting, Odger tooltip totals | `TrophyFishTracker.java`, `TrophyFishStorage.java`, `TrophyFishOverlay.java`, `TrophyRarity.java` |
| **Fishing Timer** | Timer while fishing with configurable alert | `FishingTimerOverlay.java` - bobber state + timer |

---

## 🖼️ Overlays

| Feature | Description | Implementation |
|---------|-------------|----------------|
| **Profile Viewer** | `/pv [username]` - View SkyBlock profiles via SkyAtlas (stats, dungeons, slayers) | `GuiProfileViewer.java` (implied), `ProfileParser.java`, `ProfileListener.java` - API integration + GUI |

---

## 👑 Diana Event

| Feature | Description | Implementation |
|---------|-------------|----------------|
| **Diana Tracker** | Playtime, burrows, mob rates during event | `DianaTracker.java`, `DianaStats.java`, `DianaData.java`, `LootshareDetect.java` |
| **Event Overlay** | HUD for event stats | `DianaEventOverlay.java` - `Overlay` extension |
| **Loot Overlay** | HUD for chimeras, rare drops, coins | `DianaLootOverlay.java` |
| **Inquisitor HP Overlay** | Live HP bar for nearest Minos Inquisitor | `InquisitorOverlay.java` - entity tracking + render |
| **Diana Mob HP Overlay** | Live HP bar for nearest non-inquisitor Diana mob | `DianaMobHealthOverlay.java` |

---

## 🌾 Farming

| Feature | Description | Implementation |
|---------|-------------|----------------|
| **Lock Mouse** | Locks yaw/pitch to prevent camera movement | `LockMouse.java`, `LockMouseCommand.java` - config toggle + keybind |
| **BPS Overlay** | Blocks broken per second while farming | `LockMouse.java` (integrated) - tick counter + HUD |

---

## 📊 Scoreboard

| Feature | Description | Implementation |
|---------|-------------|----------------|
| **Custom Scoreboard** | Replaces vanilla sidebar with fully customizable version | `CustomScoreboard.java`, `CustomScoreboardAPI.java`, `BankParser.java`, `MaxwellPowerSync.java`, `UnknownLinesHandler.java` |
| • Configurable lines, order, colors, scale, alignment | Line definitions in config + drag-reorder GUI |
| • Minimum width setting | `CustomScoreboard.java` - layout constraints |
| • Hide when Tab held | Input listener + visibility toggle |
| • Background color & corner radius | `Overlay` base class styling |
| • Drag-to-reorder with bin for unrecognized lines | `CustomScoreboardAPI.java` - line management GUI |

---

## 💬 Chat

| Feature | Description | Implementation |
|---------|-------------|----------------|
| **Chat Filters** | `/chatfilters` - Block/rewrite messages via custom patterns | `ChatFilterManager.java`, `ChatFilter.java`, `ChatFilterListener.java`, `ChatFilterCommand.java` + GUI editors |
| **Chat Compacting** | Collapses repeated identical messages (configurable expiry/consecutive) | `ChatLineHook.java`, `ChatUtilsState.java` - message deduplication |
| **Chat Timestamps** | Prepends timestamps (12/24hr, optional seconds) | `GuiNewChatHook.java` / chat line formatting mixin |
| **Chat Heads** | Player head next to messages; hides repeats | Chat line render mixin + head texture cache |
| **Chat Copy** | Click/Ctrl+click to copy line with/without color codes | `GuiChatHook.java`, `GuiNewChatHook.java` - hover detection + clipboard |
| **Transparent Chat** | Fully transparent chat background | Mixin into `GuiNewChat` render |
| **Animated Chat** | New messages slide into view | Chat line animation system |
| **Chat Ping** | Sound + highlight on name mention | `ChatPingListener.java` - pattern match + notification |

---

## 🎭 Cosmetics

| Feature | Description | Implementation |
|---------|-------------|----------------|
| **Capes** | Custom capes visible to Aetheria users; manage via `/capes` | `CapeManager.java`, `CapeLoader.java`, `Cape.java`, `CapeMenuCommand.java`, `CapeSelectorGUI.java`, `CapeDisplay.java` |

---

## 📦 Storage

| Feature | Description | Implementation |
|---------|-------------|----------------|
| **Storage Overlay** | Custom overlay for managing inventories (enderchest, backpacks) | `StorageManager.java`, `StorageRenderer.java`, `StorageData.java`, `StorageSaving.java`, `StorageParser.java`, `StorageListener.java` |
| **Jump To Active** | Auto-center active container in overlay | `StorageManager.java` - `requestScrollToActive()` |
| **Multiple Themes** | Default, Dark, Wooden, Ender, Parchment themes | `StorageRenderer.java` - themeable render pipeline |

---

## 🗺️ Waypoints

| Feature | Description | Implementation |
|---------|-------------|----------------|
| **Ordered Waypoints** | `/athrw guide` - sequential waypoint navigation | `WaypointCommand.java`, `WaypointState.java` - guide mode logic |
| **Waypoint Manager** | GUI to manage waypoint groups | `WaypointGroupGui.java`, `WaypointGroup.java`, `WaypointPoint.java` |
| **Auto Advance** | Auto-move to next waypoint when close enough | `WaypointRenderer.java`, `WaypointState.java` - proximity detection |

---

## ⌨️ Core Commands

> **All commands work with `asm`, `athr`, and `jef` prefixes**

| Command | Description |
|---------|-------------|
| `/asm` | Opens main Aetheria menu |
| `/asm config [category]` | Opens config, optionally to category |
| `/asm reload` | Reloads repo data |
| `/pv [username]` | Opens in-game Profile Viewer |
| `/sync` | Generate Discord↔SkyAtlas sync code |
| `/asmtimer <time>` | Countdown timer (pause/resume/cancel) |
| `/chatfilters` | Opens Chat Filters editor |
| `/athrcalc <expression>` | Advanced calculator (multpliers, trig) |
| `/diana <reset\|toggle>` | Reset/pause Diana tracking |
| `/pdt <reset\|toggle>` | Powder tracker controls |
| `/prt <reset\|toggle>` | Pristine tracker controls |
| `/lockmouse` | Toggle mouse lock for farming |
| `/athrw guide` | Ordered waypoint commands |
| `/waypoint` | Open waypoint group manager |
| `/asmbuttons` | Open inventory button editor |
| `/capes` | Open cape manager |
| `/asmprotect` | Protect held item from drops/sales |
| `/athrdebug` | Debug tools (tab list, footer copy) |

---

## 🎮 Party Commands

| Command | Description |
|---------|-------------|
| `!help` | Diana command help |
| `!pb` | View personal bests (f1–m7, br, p1–p5) |
| `!athr` | View user's mod version |

---

## 🔧 Technical Architecture

### Configuration System
- **ATHRConfig.java** - Central config with categories per feature
- JSON persistence in `config/aetheria/`
- Per-overlay position/scale/color/anchor settings
- Keybinding integration

### Event Registration
- `@RegisterEvents` annotation → `RegisterEvents.java` auto-registers Forge event handlers
- Mixin injection points for render/chat/GUI modifications

### Core Utilities (`utils/`)
| Utility | Purpose |
|---------|---------|
| `ChatUtils` | Message cleaning, formatting, detection |
| `ContainerUtils` | Container type/name detection |
| `ItemUtils` | ItemStack helpers, lore parsing |
| `ColorUtils` | Color code strip/format |
| `SkyblockData` | Location detection, API data |
| `DungeonUtils` | Floor/room detection |
| `Overlay` | Base class: position, scale, config binding |
| `Position` | Screen anchor + offset |

### Data Persistence
- **StorageManager** - Atomic JSON save/load with Gson
- Per-feature data classes (`*Data.java`, `*Storage.java`)
- Version migration via `ProfileCompressor` etc.

---

## 📁 Source Organization

```
src/main/java/io/hamlook/aetheria/features/
├── capes/           # Cosmetic capes
├── chat/            # Chat hooks, filters, timestamps, heads
├── debug/           # Dev commands
├── diana/           # Diana event tracking + overlays
├── dungeons/        # Dungeon stats, puzzles, rewards, case opening
├── farming/         # Mouse lock, BPS
├── fishing/         # Trophy fish, timer
├── mining/          # Pristine, powder, HOTM, Fetchur, Pickobulus
├── misc/            # Performance, pets, items, XP, timers
├── price/           # Bazaar/Auction API
├── profile/         # SkyAtlas profile parsing
├── qol/             # Timers, overlays, enchants, helpers
├── scoreboard/      # Custom scoreboard
├── storage/         # Enderchest/backpack overlay
└── waypoints/       # Waypoint system
```

---

## 🚀 Installer
Run the JAR directly: `java -jar Aetheria-*.jar` → standalone installer downloads/updates mods from GitHub releases.