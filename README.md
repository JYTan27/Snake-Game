# Advanced Snake Game (JavaFX)
**Module:** COMP2042 — Developing Maintainable Software  
**Academic Year / Coursework:** 2026

---

## 1. GitHub
* **Repository Link:** [https://github.com/JYTan27/Snake-Game](https://github.com/JYTan27/Snake-Game)

---

## 2. Compilation Instructions

### Prerequisites & Dependencies
* **JDK (Java Development Kit):** Version 21 or later.
* **Build Tool:** Apache Maven 3.8+ (or Maven Wrapper / IDE bundled Maven).
* **JavaFX Version:** 25.0.1 (managed automatically via `pom.xml` dependencies):
  * `javafx-controls`
  * `javafx-fxml`
  * `javafx-graphics`
  * `javafx-media`
  * `javafx-base`

### Step-by-Step Compilation & Execution Guide

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/JYTan27/Snake-Game.git
   cd Snake-Game
   ```

2. **Compile the Project:**
   ```bash
   mvn clean compile
   ```

3. **Run the Application via Maven:**
   ```bash
   mvn javafx:run
   ```

4. **Package into an Executable "Fat" JAR (Optional):**
   ```bash
   mvn clean package
   ```
   The runnable JAR file with all JavaFX dependencies bundled will be created at:  
   `target/snake-javafx.jar`  
   You can run it with:
   ```bash
   java -jar target/snake-javafx.jar
   ```

---

## 3. Implemented and Working Properly

* **Configuration Sources:** Exact tuning values are maintained in the source rather than repeated here:
  * [GameMode](src/main/java/com/comp2042/model/GameMode.java): mode speeds and pickup weights.
  * [Pickup](src/main/java/com/comp2042/model/Pickup.java) and its subclasses in the same package: point rewards, speed bonuses, and pickup effects.
  * [Obstacle](src/main/java/com/comp2042/model/Obstacle.java): obstacle capacity; [BlocksPickup](src/main/java/com/comp2042/model/BlocksPickup.java): obstacle additions per collection.
  * [GameController](src/main/java/com/comp2042/controller/GameController.java): input-buffer capacity.
  * [SoundEffect](src/main/java/com/comp2042/service/audio/SoundEffect.java): default audio volume.
  * [LeaderboardController](src/main/java/com/comp2042/controller/LeaderboardController.java): displayed ranking limit; all run history is retained for summaries.
  * [Game](src/main/java/com/comp2042/model/Game.java): board scaling; [MainMenuController](src/main/java/com/comp2042/controller/MainMenuController.java): initial board dimensions.
* **MVC (Model-View-Controller) Architectural Refactoring:**
  * Clean separation of concerns into distinct packages:
    * `com.comp2042.model`: Pure gameplay rules, coordinates, state, and entities (`Game`, `GameMode`, `Snake`, `Obstacle`, `Pickup`, `DiamondPickup`, `PickupFactory`, `WeightedPickupSelector`, `PickupType`, `RingPickup`, `StarPickup`, `BlocksPickup`, `BoltPickup`, `GridPoint`).
    * `com.comp2042.view`: Display and scene orchestration (`GameView`, `ViewManager`).
    * `com.comp2042.model.stats`: Immutable run results, outcomes, summaries, and active-time tracking without JavaFX dependencies.
    * `com.comp2042.service.leaderboard`: Ranking, summary calculation, and replaceable local persistence.
    * `com.comp2042.controller`: Input handling, keyboard controls, animation loop, and reference screens (`GameController`, `MainMenuController`, `HowToPlayController`, `LeaderboardController`).
* **Standard Maven Resource Packaging:**
  * Project resources, including the new help layout, are packaged in structured directories:
    * `src/main/resources/com/comp2042/view/` (`MainMenu.fxml`, `HowToPlay.fxml`, `Leaderboards.fxml`, `styling.css`)
    * `src/main/resources/com/comp2042/images/` (`coins/`, `snake/`, `ui/`)
    * `src/main/resources/com/comp2042/audio/` (pickup, button, death, and win MP3s; unused experimental variants retained)
    * `src/main/resources/com/comp2042/fonts/` (`SFPixelate.ttf`, `SFPixelateShaded-Bold.ttf`, `kenvector_future.ttf`)
* **Classpath Asset Loading:**
  * Replaced brittle relative file paths with standard `getClass().getResourceAsStream(...)` and `getResource(...).toExternalForm()` to ensure resources load reliably in both IDE and standalone JAR packaging.
* **Custom `GridPoint` Model (Elimination of AWT Coupling):**
  * Created custom `GridPoint` integer coordinate class in `com.comp2042.model`, completely eliminating the legacy `java.awt.Point` dependency from all models, controllers, and views.
* **Snake Model Decoupling & Optimisation:**
  * Removed all JavaFX `ImageView` and `SnapshotParameters` instances from `Snake` (model no longer imports any scene‑graph nodes), replacing them with `private static final Image` singletons — enabling headless unit testing.
  * `move()` refactored with shared `attemptMove()` / `wrapCoordinate()` helpers, removing copy‑pasted collision/rollback blocks across all four direction cases.
  * Border‑wrap behaviour unified into a single `borderWrap` flag, eliminating the redundant `ignoreBorders` / `noBorder` anti‑pattern.
  * `body` collection changed from `ArrayList` to `ArrayDeque`, making tail removal O(1) on every non‑eating tick.
  * "Move first, rollback on collision" pattern replaced by validate‑before‑move, eliminating fragile inverse‑translation code.
  * **Consistent Death Pose:** Solid-border and obstacle collisions freeze the head and all body segments at their last valid positions, preventing head/neck overlap even after fullscreen redraws.
  * **Obstacle Checks After Border Wrapping:** Wrapped destinations receive the same obstacle checks as ordinary moves, so obstacles on the opposite edge remain lethal in Wraparound mode and during ring-enabled wrapping.
  * **Toroidal Tail Orientation Calculation (`getTailDir`):** Encapsulated tail orientation in the model using shortest-displacement toroidal vector calculation, ensuring the tail sprite smoothly faces the forward direction without 180° flipping when crossing wrap-around borders.
* **Deterministic Coordinate Sampling (Elimination of Infinite Loops):**
  * Replaced unconstrained guess-and-retry `do...while` generation in both `Pickup` and `Obstacle` with deterministic available-cell sampling. Guarantees instantaneous termination even on crowded endgame boards without freezing.
* **Robust Obstacle Management:**
  * Corrected off-by-one bounds check so obstacles spawn in valid `[0, boardWidth)` columns.
  * Duplicate obstacle placement strictly prevented by checking against existing obstacles.
  * Encapsulation enforced via `Collections.unmodifiableList(obstacles)`.
  * Scalable obstacle cap proportional to board dimensions, defined in `Obstacle`.
* **Modular Board Appearance:**
  * Open **Board Colours** from the main menu to preview Soft Cream, Mint, Sky, Lavender, Peach, or Slate. **Apply** saves the palette across launches. Select **Soft Cream** to restore the default.
  * `GameView(Game, BoardRenderer, BoardTheme)` takes the chosen palette. Background tiles align with gameplay cells in windowed and fullscreen modes.
* **GameView Rendering Pipeline Rewrite:**
  * All `ImageView.snapshot()` calls replaced by `GraphicsContext.save()` / `gc.rotate()` / `gc.drawImage()` / `gc.restore()` — canvas rotation without allocating snapshot images per segment.
  * Snake rendering reuses one body-list copy per redraw. Straight-segment orientation uses adjacent points, tail orientation reads the deque directly, and the tail's turn-history entry is cleared once per redraw. Body rendering now takes O(n) work instead of repeatedly copying the entire body for each segment; it still allocates the shared body-list copy.
  * Board appearance stays in the view layer through `BoardRenderer` and `BoardTheme`, separate from gameplay models.
  * Double‑scaling bug (`getMultiplier() * fullScale * fullScale`) corrected to a single `cellSize = getMultiplier() * fullScale` factor applied uniformly to both position and size.
  * Stale turn entries cleared via `resetTurnArray()` upon game restart.
  * Decoupled from controller static variables: accepts direction parameters in `turnDir(snake, lastKey, key)`.
* **Single Stage Architecture & Safe Lifecycle Management:**
  * Single Stage reused across scenes via `ViewManager.showGameScene()` and `showMainMenu()`, eliminating secondary stage creation, focus issues, and duplicate OS taskbar icons.
  * Safe FXML loading in `ViewManager` with user alert dialog and early exit on `IOException`, preventing `NullPointerException`.
  * Method typo corrected: `initializeMainMenu()`.
* **Core Gameplay Loop & Features:**
  * **Snake Movement & Controls:** Symmetrical 4‑directional navigation with correct anti‑reverse logic for all directions (including Down vs. Up).
  * **Responsive FIFO Input Buffer:** Bounded input queue (`Deque<KeyCode>`) in `GameController` that buffers rapid sequential keystrokes up to its configured capacity. Buffered turns (e.g. `UP` then `RIGHT`) are applied on successive logic ticks, synchronizing corner elbow sprite (`snaketurnleft.png`) placement with movement.
  * **Game Mode Selection:** Choose Classic, Obstacles, Hyperspeed, Power-ups, or Wraparound before starting. New sessions default to Classic. The choice is remembered during the current session, including after visiting How to Play or returning from a game.
  * **How to Play Screen:** Explains game-mode rules and keyboard controls in a vertically scrollable area, with the heading and Back button always visible. Scroll using the mouse wheel, scrollbar, or keyboard; use Back or Esc to return to the menu.
  * **Local Leaderboards:** Open Leaderboards from the menu and choose a mode using the fixed Game Mode dropdown beneath the heading. Only the selected mode's ranked runs and summary are displayed; its results can scroll while the heading, selector, and Back button remain visible. The screen initially uses the main menu's selected mode. Browsing other leaderboards leaves the menu selection unchanged; switching modes resets scrolling to the top. Rankings sort by score descending; equal scores retain recording order. Each row shows score, outcome, active time, snake length including the head, and local date. Summaries use all recorded history, including runs beyond the displayed ranking limit.
  * **Shared Mode Selector:** Main-menu and leaderboard dropdowns share `GameModeSelector` configuration for mode names, selection audio, and fullscreen popup scaling. Available modes follow `GameMode.values()`. Initial selection is silent; changing modes plays the button effect. Leaderboard browsing is local to the current screen and is not stored as an additional preference.
  * **Run Recording:** Wins, losses, and abandoned runs count toward rankings, run counts, average score, and total active time. Restarting, returning to the menu, or closing the application normally abandons a started run. Boards never started with movement input are ignored; pauses and initial waiting time are excluded. Terminal runs are recorded once, including after later reset, menu navigation, or shutdown. Fatal self-collision records the last settled snake length; a win includes the final pickup's points and growth.
  * **Persistent Results:** The application loads local XML history once and keeps queries in memory. Recording a new run saves the full history using a temporary file and atomic replacement where supported. Windows storage is `%LOCALAPPDATA%/AdvancedSnake/leaderboard.xml`, with `${user.home}/.advanced-snake/leaderboard.xml` as a fallback. Missing history starts empty. Corrupt or unsupported history is preserved and disables writes for that session. Save failures retain results in memory and retry when the next new run is recorded. Leaderboards displays storage warnings when opened.
  * **Menu Design:** The title and controls are centred, with consistent button states and clear mode-selection highlights. Exit closes the application.
  * **Menu Fullscreen:** F toggles fullscreen on menu, help, leaderboard, and gameplay screens. Menu elements and dropdown options scale proportionally, and fullscreen is preserved between screens. Esc handles in-app navigation without JavaFX's default fullscreen exit message.
  * **Game Modes:** A standalone `GameMode` enum defines names, help descriptions, base speeds, border rules, and immutable pickup weights. Select one mode at a time:

    | Mode | Borders | Pickups |
    | :--- | :--- | :--- |
    | Classic | Solid | Diamond |
    | Obstacles | Solid | Diamond, Blocks |
    | Hyperspeed | Solid | Diamond |
    | Power-ups | Solid | Diamond, Star, Ring, Bolt |
    | Wraparound | Wrapping | Diamond |

    Self-collision is lethal in every mode. All modes win when no free pickup cell remains. Reset keeps the selected mode and clears obstacles, points, temporary effects, and completion.
  * **Collectible Pickups:** Distinct pixel-art collectibles use an abstract `Pickup` base and distinct subclasses:
    * Diamond (`DiamondPickup`, `DIAMOND`): Awards the standard pickup points.
    * Star (`StarPickup`, `STAR`): Awards pickup-specific points defined by `StarPickup`.
    * Ring (`RingPickup`, `RING`): Border wrapping until the next pickup; Power-ups only.
    * Bolt (`BoltPickup`, `BOLT`): Applies a temporary speed boost until the next pickup.
    * Blocks (`BlocksPickup`, `BLOCKS`): Adds barriers in Obstacles mode while the configured obstacle cap and available cells permit.
    * Sprites are centred within their cells and drawn without image smoothing. Supplied PNGs and unused legacy coin assets are preserved.
  * **Coin-to-Pickup Rename:**

    Classes were renamed to match the new sprites and make their purpose clearer:

    | Original class | Replacement | Preserved behaviour |
    | :--- | :--- | :--- |
    | `Coin` | Abstract `Pickup` base + `DiamondPickup` | Standard pickup point reward |
    | `GoldCoin` | `StarPickup` | Pickup-specific point reward |
    | `BlueCoin` | `RingPickup` | Border wrapping until the next pickup |
    | `RainbowCoin` | `BoltPickup` | Speed boost until the next pickup |
    | `SilverCoin` | `BlocksPickup` | Creates obstacles; numeric target adjustment was later removed |

    `CoinFactory` became `PickupFactory`, and `CoinType` became `PickupType`. The rename itself did not change effects, spawn probabilities, or mode restrictions. Historical bug notes retain the original names to describe the code as it existed at the time.

  * **Pickup Selection Factory:** `GameMode` defines immutable positive pickup weights; `PickupFactory` passes them to `WeightedPickupSelector`, which selects a `PickupType` proportionally using its stored, injectable `Random`. The factory then constructs the pickup, replacing selected blocks with diamonds without rerolling when the obstacle cap is reached. Obstacles selects diamonds and blocks; Power-ups selects diamonds, stars, rings, and bolts. Other modes are diamond-only; initial/reset pickups remain diamonds. Exact probabilities follow the weights configured in `GameMode`.
  * **Simplified Rule Configuration:** `Game(width, height, mode)` gets its base speed and default border behaviour directly from `GameMode`. The separate `powerUp` flag and duplicate `allowsPickup()` methods have been removed; the mode's weights determine which types can be selected. The factory no longer repeats mode-permission or ring checks that cannot reject a selection under the current five modes. Temporary ring wrapping remains state held by `Snake`; selector weight and random-source validation remain in place.
  * **Asset Caching:** Snake and pickup sprites are loaded once as `static final` assets — no disk reads or texture re‑allocations per tick or per pickup spawn.
  * **Full-Board Completion:** The objective is to occupy every non-obstacle cell. After collection, `PickupFactory.createPickup(...)` returns empty only when no free cell remains for a replacement pickup. The controller then records completion, stops movement, clears buffered inputs and the pickup, and displays the final score and win message. F can redraw the completed board; Tab clears completion, obstacles, and display points before recreating the snake and initial diamond.
  * **End-Message Visibility:** `GameView.gameOver(Snake)` and `gameWon()` set label visibility directly from the model's death and victory state, removing duplicate visibility flags. Existing controller calls refresh the messages during play, completion, and reset.
  * **Score & Win Conditions:** Display points reward collected pickups and do not determine victory. `Game.gameWon()` reports the completion flag set by `markWon()`; numeric collection targets and obstacle-related target adjustments have been removed. `resetScore()` clears display points and completion, with no target to recalculate.
  * **Centralised Pickup Collection:** After collection is detected, `GameController.collectPickup()` delegates to `Game.collectPickup(Pickup, Snake, Obstacle)`. The model calculates movement speed from the mode's base speed and the pickup's bonus, restores default border behaviour, applies the pickup's effect, and adds display points. It returns the resulting speed for the controller to apply to its animation. The controller retains collection detection, sound playback, completion handling, replacement spawning, and rendering; pickup subclasses retain their individual values and effects.
  * **Sound Effects:** A shared `SoundService` caches pickup, button, pause, resume, restart, death, and win clips. Each pickup has a distinct sound; terminal sounds play once and interrupt ongoing effects. P plays pause audio only when stopping a running game; WASD plays resume audio only after a player-requested pause, not on initial movement or movement after restart. Reset stops older effects before playing `restart.mp3` and leaves the game waiting for movement input. Menu return stops gameplay effects. Default volume is defined in `SoundEffect` and adjustable through the service. Numbered variants, alternate jingles, and turn/wrapping sounds remain unused.
  * **Background Music:** The soundtrack loops through Aventure's *Afternoon Coffee* and *Sunset*, followed by ruskerdax's *puzzling*. It plays on the main menu, Leaderboards, How to Play, and Board Colours. It pauses when gameplay opens and resumes from the same spot on return to the menu.
  * **Controls:** Move/start/resume with `W` (up), `A` (left), `S` (down), and `D` (right). Arrow keys remain available for menu navigation only. Pause (`P`), Reset (`TAB`), Fullscreen toggle (`F`, press again to restore the original size), Return to Menu (`ESC` with animation stop and scene return).

---

### Automated Validation

Run `./mvnw compile` (Windows: `.\mvnw.cmd compile`) to check the build. To check leaderboard persistence, finish a run, then relaunch the application and open Leaderboards for the same mode.

The earlier redundancy cleanup was verified with temporary smoke checks covering gameplay configuration, pickup effects, wrapping, buffered movement, and reset; those earlier checks were not retained as permanent tests.

---

## 4. Implemented but Not Working Properly

*(None currently recorded. All 49 entries in `BUGS_AND_IMPROVEMENTS.md` are marked as fixed.)*

---

## 5. Remaining Unfixed Bugs / Open Issues

*(None. All bugs from `BUGS_AND_IMPROVEMENTS.md` have been addressed.)*

---

## 6. Features Not Implemented

* **Settings & Volume Control Slider:**
  * *Reason:* Focused on core gameplay mechanics and architectural decoupling before adding secondary audio settings.

---

## 7. New Java Classes

| Class | Package | Purpose & Description |
| :--- | :--- | :--- |
| **`RunResult.java`**, **`RunOutcome.java`**, **`ModeStatistics.java`**, **`RunSession.java`** | `com.comp2042.model.stats` | Pure Java result and summary types, plus idempotent run finalisation and monotonic active-time tracking with injectable clocks. |
| **`LeaderboardRepository.java`**, **`XmlLeaderboardRepository.java`**, **`LeaderboardService.java`** | `com.comp2042.service.leaderboard` | Replaceable persistence, versioned XML, per-mode queries, stable rankings, and storage warnings. |
| **`LeaderboardController.java`** | `com.comp2042.controller` | Renders the selected mode's summary and rankings from shared service data. Receives its initial mode from `ViewManager`; dropdown changes refresh results and storage warnings and reset scrolling. Handles Back navigation without changing the menu mode. |
| **`GameModeSelector.java`** | `com.comp2042.view` | Shared dropdown configuration for mode display names, silent initial selection, later selection audio, and fullscreen popup scaling. |
| **`HowToPlayController.java`** | `com.comp2042.controller` | Handles the help screen Back button, returning to the main menu through `ViewManager`. |
| **`BoardRenderer.java`**, **`CheckerboardBoardRenderer.java`**, **`BoardTheme.java`** | `com.comp2042.view.board` | Separate board patterns from colour themes; provide the default Soft Cream checkerboard. |
| **`SoundEffect.java`**, **`SoundService.java`**, **`JavaFxSoundService.java`** | `com.comp2042.service.audio` | Shared cached audio, central resource mapping and volume, replaceable playback for testing. |
| **`WeightedPickupSelector.java`** | `com.comp2042.model` | Selects a pickup type from positive weights in stable enum order; stores the injected `Random` and validates weights and the random source. |
| **`GameMode.java`** | `com.comp2042.model` | Defines the five independent gameplay modes, shared menu/help descriptions, base speeds, border rules, and immutable pickup weights. |
| **`PickupType.java`** | `com.comp2042.model` | Identifies pickup types for per-mode spawning configuration. |
| **`DiamondPickup.java`** | `com.comp2042.model` | Standard diamond pickup, used for initial spawning, reset, and blocks-to-diamond fallback at the obstacle cap. |
| **`PickupFactory.java`** | `com.comp2042.model` | Centralizes replacement spawning through `createPickup(Game, Snake, Obstacle)`: delegates selection from mode weights to `WeightedPickupSelector`, replaces selected blocks with diamonds at the obstacle cap, and constructs the pickup. Returns empty only when no free cell remains; accepts an injectable `Random` passed to the selector for deterministic type selection. |
| **`GridPoint.java`** | `com.comp2042.model` | Pure Java integer coordinate representation (`x`, `y`) designed to replace legacy `java.awt.Point`. Provides getters, setters, translation, copy constructor, and standard `equals`/`hashCode`/`toString` methods without pulling in desktop AWT UI dependencies. |

---

## 8. Modified Java Classes

| Class | Original Package | New Package | Changes Made & Rationale |
| :--- | :--- | :--- | :--- |
| **`Main.java`** | `com.comp2042` | `com.comp2042` | Added explicit `ViewManager` import and programmatic font loading. Creates shared audio and leaderboard services and passes them to `ViewManager`. Normal shutdown finalizes active gameplay and then closes audio. |
| **`ViewManager.java`** | `com.comp2042` | `com.comp2042.view` | Moved to `view` package. Reuses one stage for gameplay, menu, help, and leaderboards, with fullscreen-aware scaling and safe FXML error handling. Retains the selected mode, supplies application-owned services, and detaches/invokes game cleanup on navigation and shutdown. |
| **`MainMenuController.java`** | `com.comp2042` | `com.comp2042.controller` | Moved to `controller` package. Populates the typed mode selector from `GameMode.values()`, restores its session selection, and remembers it before opening reference screens or gameplay. Builds a mode-configured game scene, injects shared audio and leaderboard services, and registers cleanup with `ViewManager`. Opens Leaderboards with button audio. |
| **`GameController.java`** | `com.comp2042` | `com.comp2042.controller` | Moved to `controller` package. Implemented a bounded FIFO input queue (`inputQueue`) for rapid sequential turns; synchronized `turnDir` calls with logic ticks; replaced plus/minus size shortcuts with an F fullscreen toggle (Bug #46); encapsulated static `key`/`lastKey` as private fields with getters; corrected `valKey` DOWN anti-reverse logic (Bug #40); removed dead code (`calculateBonusScore`, `legacyScoreMultiplier`, `unusedObstacleCount`); delegated replacement pickup creation to `PickupFactory`, which uses `WeightedPickupSelector` for random type selection; delegated pickup effects and display-point updates to `Game.collectPickup(...)`, applying its returned animation speed; removed the numeric win check before spawning and retained `finishGame()` for exhausted pickup placement; reset turn array on restart; and hooked return-to-menu callback to stop animation and swap scenes. Removed the repeated score-label update after `reset()` and the arrow-key check already guaranteed by the WASD mapping, preserving duplicate-direction and reverse-direction rejection. |
| **`GameView.java`** | `com.comp2042` | `com.comp2042.view` | Moved to `view` package. delegated background rendering to configurable `BoardRenderer` / `BoardTheme`; removed unused `a`/`b` parameters from `drawGrid`, leaving only pickup, snake, obstacle, and graphics context; eliminated snapshots in favor of `GraphicsContext` rotation; fixed double-scaling bug; eliminated per-frame `SnapshotParameters`; added `resetTurnArray()`; migrated from `Point` to `GridPoint`; decoupled `turnDir` from controller static state; unified tail rendering in `drawBody` under `index == 0`; reused one body-list copy throughout each redraw; cleared the tail's turn-history entry once; and called `Snake.getTailDir()` directly after removing the redundant `tailRotation()` wrapper. |
| **`Game.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Removed UI `Color` coupling; accepts `Game(width, height, mode)` and delegates `getSpeed()` / `isNoBorder()` to `GameMode`, removing copied rule fields, the unused `powerUp` flag/getter, and the unused `allowsPickup()` wrapper. Added `collectPickup(Pickup, Snake, Obstacle)` to restore default border behaviour, apply pickup effects, award display points, and return movement speed; replaced magic scaling numbers with named constants; made `gameWon()` report recorded completion; and removed numeric win targets, the collection counter, and target-adjustment methods. `resetScore()` clears display points and completion. |
| **`Snake.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Fully migrated from `Point` to `GridPoint`; removed `ImageView` nodes and exposed `static final Image` singletons; extracted `attemptMove` / `wrapCoordinate`; unified border flags into `borderWrap`; switched body to `ArrayDeque`; cached pickup collision checks; validate-before-move with obstacle checks after resolving wrapped destinations (Bug #44); preserved the complete snake pose on blocked fatal moves by advancing the body only after successful movement (Bug #47); renamed constructor parameters; replaced `getBodyDir(int)` with `getBodyDir(GridPoint current, GridPoint previous)` to avoid list copying; and kept toroidal border-wrap orientation in `getTailDir()` while reading its two points directly from the deque. |
| **`Obstacle.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Migrated to `GridPoint`; replaced unbounded generation with deterministic free-cell sampling; fixed off-by-one X range; prevented duplicates; encapsulated `getObstacles()` via `Collections.unmodifiableList`; added proportional cap; and renamed dimension fields `boardWidth, boardHeight`. |
| **`Pickup.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Migrated to `GridPoint`; replaced unbounded `do...while` with deterministic free-cell sampling to eliminate freeze risks; renamed from `Coin` to the abstract `Pickup` base; added polymorphic `applyEffect` and `getSpeedBonus`; and used shared `Random`. |
| **`RingPickup.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Cached static sprite, encapsulated border-ignore behavior inside `applyEffect(...)`, and removed redundant `getImage()`. |
| **`StarPickup.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Cached static sprite, overridden `getPoints()` with a pickup-specific reward constant, and removed redundant `getImage()`. |
| **`BlocksPickup.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Cached static sprite, encapsulated dynamic barrier spawning in `applyEffect(...)`, removed numeric target adjustment because pickup placement already excludes obstacles, and removed redundant `getImage()`. |
| **`BoltPickup.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Cached static sprite, provided speed bonus value, and removed redundant `getImage()`. |

---

## 9. Unexpected Problems

1. **Relative Resource Path Failures in JavaFX:**
   * *Problem:* The original codebase relied on working‑directory paths (e.g. `"resources/views/res/..."`). When the application was compiled or packaged into a JAR, these paths failed because resources were inside the JAR rather than loose files on disk.
   * *Resolution:* Converted all asset references to standard classpath resource streams (`getClass().getResourceAsStream("/com/comp2042/…")`).
2. **CSS Font‑Face Loading Across Environments:**
   * *Problem:* Custom `@font-face` definitions inside `styling.css` failed to register fonts consistently on some JVMs when packaged.
   * *Resolution:* Added programmatic font loading (`Font.loadFont(...)`) inside `Main.java` at application startup as a reliable fallback.
3. **FXML Controller Binding Mismatch:**
   * *Problem:* When moving `MainMenuController` into `com.comp2042.controller`, `MainMenu.fxml` failed to launch because `fx:controller` still pointed to `com.comp2042.MainMenuController`.
   * *Resolution:* Updated `MainMenu.fxml` with the fully qualified package name `fx:controller="com.comp2042.controller.MainMenuController"`.
4. **Broken Maven Wrapper Distribution URL:**
   * *Problem:* The repository's `.mvn/wrapper/maven-wrapper.properties` contained a non‑existent Maven version URL, causing wrapper scripts and build tools to fail with HTTP 404 / `FileNotFoundException`.
   * *Resolution:* Corrected the `distributionUrl` to point to the official `apache-maven-3.9.6-bin.zip` release archive.
5. **Stage Accumulation and Window Focus Glitches on Return to Menu:**
   * *Problem:* The original implementation opened and closed new `Stage` instances on every game start and return-to-menu action, causing OS taskbar icon flickering and losing application focus.
   * *Resolution:* Converted `ViewManager` to a single-stage architecture that swaps `Scene` objects on the primary stage.
6. **Easy Mode Border Wrapping Disabled After Collecting a Coin:**
   * *Problem:* `GameController.powerCoin()` called `snake.ignoreBorders(false)` after every coin collection, overriding Easy mode's border setting.
   * *Resolution:* Replaced the hard-coded `false` with `game.isNoBorder()` before applying the coin's effect. Easy mode now keeps border wrapping after every coin, while Normal/Hard retain the blue coin's temporary wrapping effect.
7. **Snake Passed Through Obstacles at Wrapped Borders:**
   * *Problem:* `Snake.attemptMove()` moved the head directly to the wrapped destination, bypassing the obstacle check in its `else if` branch. Edge obstacles could be crossed in Easy mode or after collecting a blue coin.
   * *Resolution:* Resolve the wrapped target first, check that destination for obstacles, then update the head only if the cell is clear. A fatal collision leaves the head at its previous position; the fix applies to all four borders.
8. **Plus Shortcut Failed to Enter Fullscreen:**
   * *Problem:* Shift + = arrives as `KeyCode.EQUALS` with Shift held, so the `KeyCode.PLUS` check did not recognize it. Minus restored the original size but could not make an already windowed board smaller.
   * *Resolution:* Replaced both shortcuts with `F` to toggle fullscreen based on `stage.isFullScreen()`, reusing the existing board scaling and label sizes.
9. **Head Overlapped the Neck After a Blocked Fatal Move:**
   * *Problem:* `Snake.move()` appended the old head coordinate to the body before movement validation. On a fatal border or obstacle collision the head remained still, leaving the new body segment underneath it.
   * *Resolution:* `attemptMove()` reports success and commits the head position and direction only for valid destinations. `move()` advances the body only after successful movement and ignores further moves once dead. The whole snake retains its last valid pose, including when redrawn after pressing F.
10. **Full-Board Coin Fallback and Incomplete Win Handling:**
   * *Problem:* Coin placement fell back to `(0,0)` when every cell was occupied, potentially overlapping the snake or an obstacle. The score-based win branch returned before refreshing the final score and win message, and did not stop movement.
   * *Resolution:* Coin construction now throws `Coin.NoFreeCellException` when placement is impossible; `CoinFactory.createCoin()` returns `Optional<Coin>`, empty only for that condition. The controller completes either win path through a shared method that marks the game won, stops the timer, clears the coin and queued turns, and updates the display. Rendering skips an absent coin; arrows cannot restart a win. Reset clears obstacles before coin creation and restores the completion state and original target score.
   * *Later simplification:* The resolution above describes the earlier implementation. Improvement #55 in `BUGS_AND_IMPROVEMENTS.md` replaces numeric collection targets with full-board completion as the single win condition; current reset clears points and completion without recalculating a target.
