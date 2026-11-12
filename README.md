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

* **MVC (Model-View-Controller) Architectural Refactoring:**
  * Clean separation of concerns into distinct packages:
    * `com.comp2042.model`: Pure gameplay rules, coordinates, state, and entities (`Game`, `Snake`, `Obstacle`, `Coin`, `BlueCoin`, `GoldCoin`, `SilverCoin`, `RainbowCoin`, `GridPoint`).
    * `com.comp2042.view`: Display and scene orchestration (`GameView`, `ViewManager`).
    * `com.comp2042.controller`: Input handling, keyboard controls, and animation loop (`GameController`, `MainMenuController`).
* **Standard Maven Resource Packaging:**
  * All 21 project assets migrated from legacy nested folders into structured directories:
    * `src/main/resources/com/comp2042/view/` (`MainMenu.fxml`, `styling.css`)
    * `src/main/resources/com/comp2042/images/` (`coins/`, `snake/`, `ui/`)
    * `src/main/resources/com/comp2042/audio/` (`coinSound.wav`)
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
  * **Toroidal Tail Orientation Calculation (`getTailDir`):** Encapsulated tail orientation in the model using shortest-displacement toroidal vector calculation, ensuring the tail sprite smoothly faces the forward direction without 180° flipping when crossing wrap-around borders.
* **Deterministic Coordinate Sampling (Elimination of Infinite Loops):**
  * Replaced unconstrained guess-and-retry `do...while` generation in both `Coin` and `Obstacle` with deterministic available-cell sampling. Guarantees instantaneous termination even on crowded endgame boards without freezing.
* **Robust Obstacle Management:**
  * Corrected off-by-one bounds check so obstacles spawn in valid `[0, boardWidth)` columns.
  * Duplicate obstacle placement strictly prevented by checking against existing obstacles.
  * Encapsulation enforced via `Collections.unmodifiableList(obstacles)`.
  * Scalable obstacle cap based on board dimensions (`boardWidth * boardHeight / 4`).
* **GameView Rendering Pipeline Rewrite:**
  * All `ImageView.snapshot()` calls replaced by `GraphicsContext.save()` / `gc.rotate()` / `gc.drawImage()` / `gc.restore()` — hardware‑accelerated canvas rotation with zero per‑frame allocations.
  * Background fill color moved from `Game` model into `GameView` (`BACKGROUND_COLOR`).
  * Double‑scaling bug (`getMultiplier() * fullScale * fullScale`) corrected to a single `cellSize = getMultiplier() * fullScale` factor applied uniformly to both position and size.
  * Stale turn entries cleared via `resetTurnArray()` upon game restart.
  * Decoupled from controller static variables: accepts direction parameters in `turnDir(snake, lastKey, key)`.
* **Single Stage Architecture & Safe Lifecycle Management:**
  * Single Stage reused across scenes via `ViewManager.showGameScene()` and `showMainMenu()`, eliminating secondary stage creation, focus issues, and duplicate OS taskbar icons.
  * Safe FXML loading in `ViewManager` with user alert dialog and early exit on `IOException`, preventing `NullPointerException`.
  * Method typo corrected: `initializeMainMenu()`.
* **Core Gameplay Loop & Features:**
  * **Snake Movement & Controls:** Symmetrical 4‑directional navigation with correct anti‑reverse logic for all directions (including Down vs. Up).
  * **Responsive 2-Key FIFO Input Buffer:** Bounded input queue (`Deque<KeyCode>`) in `GameController` that buffers rapid sequential keystrokes. Quick double-turns (e.g. `UP` then `RIGHT` within a fraction of a second) are never dropped or ignored, while perfectly synchronizing corner elbow sprite (`snaketurnleft.png`) placement with logic tick execution.
  * **Difficulty & Speed Configuration:** Strongly-typed `Game.Difficulty` enum with balanced speeds:
    * `EASY`: `7.0` moves/sec (with border-wrap enabled).
    * `NORMAL`: `9.0` moves/sec.
    * `HARD`: `12.0` moves/sec.
  * **Polymorphic Food & Coin System:** Spawning coins with distinct attributes and behaviours using object‑oriented polymorphism (`Coin`, `GoldCoin`, `SilverCoin`, `BlueCoin`, `RainbowCoin`):
    * Standard Red Coin: Basic point increment (+5).
    * Gold Coin: Higher point reward (+15).
    * Blue Coin: Temporary wall pass‑through / border‑ignore ability.
    * Silver Coin: Spawns additional barrier obstacles on the board and adjusts target score.
    * Rainbow Coin: Grants speed boost (+5.0 speed) to the game loop.
  * **Asset Caching:** Snake and coin sprites are loaded once as `static final` assets — no disk reads or texture re‑allocations per tick or per coin spawn.
  * **Score & Win Conditions:** Clarified score tracking (`coinsEaten` vs `displayScore`) with documented, guarded target score decrements (`decMaxScore`).
  * **Audio Playback:** Low‑latency coin collection sound effects via `AudioClip`.
  * **Controls:** Pause (`P`), Reset (`TAB`), Fullscreen toggle (`+` / `-`), Return to Menu (`ESC` with animation stop and scene return).

---

## 4. Implemented but Not Working Properly

*(None. All 41 previously identified bugs and issues have been resolved, verified, and compiled successfully.)*

---

## 5. Remaining Unfixed Bugs / Open Issues

*(None. All bugs from `BUGS_AND_IMPROVEMENTS.md` have been addressed.)*

---

## 6. Features Not Implemented

* **Persistent High Score Table:**
  * *Reason:* Local disk/file‑based leaderboard was not present in the original codebase; left out to prioritize core architectural refactoring and stability.
* **Difficulty Selection UI in Main Menu:**
  * *Reason:* The `Game` model supports strongly-typed difficulties (`Game.Difficulty.EASY`, `NORMAL`, `HARD`), but the current `MainMenu.fxml` UI does not yet feature a dropdown/picker. The setting can be configured in `MainMenuController`.
* **Settings & Volume Control Slider:**
  * *Reason:* Focused on core gameplay mechanics and architectural decoupling before adding secondary audio settings.
* **Automated Unit Testing Suite:**
  * *Reason:* With pure POJO models (`GridPoint`, `Snake`, `Obstacle`, `Game`) now decoupled from UI nodes and legacy AWT, headless JUnit tests can be readily added in a future milestone.

---

## 7. New Java Classes

| Class | Package | Purpose & Description |
| :--- | :--- | :--- |
| **`GridPoint.java`** | `com.comp2042.model` | Pure Java integer coordinate representation (`x`, `y`) designed to replace legacy `java.awt.Point`. Provides getters, setters, translation, copy constructor, and standard `equals`/`hashCode`/`toString` methods without pulling in desktop AWT UI dependencies. |

---

## 8. Modified Java Classes

| Class | Original Package | New Package | Changes Made & Rationale |
| :--- | :--- | :--- | :--- |
| **`Main.java`** | `com.comp2042` | `com.comp2042` | Added explicit `ViewManager` import and programmatic font loading. Passes `primaryStage` to `new ViewManager(primaryStage)` and calls `viewManager.initializeMainMenu()`. |
| **`ViewManager.java`** | `com.comp2042` | `com.comp2042.view` | Moved to `view` package. Implemented single-stage scene swapping (`showGameScene`, `showMainMenu`), eliminated multiple `Stage` anti-pattern, added safe `IOException` error dialog, and corrected method typo `initializeMainMenu()`. |
| **`MainMenuController.java`** | `com.comp2042` | `com.comp2042.controller` | Moved to `controller` package. Uses strongly-typed `Game.Difficulty.EASY`, refactored `launchSnake` into `buildGameScene` with proper static `StackPane` alignment, and wired escape key to clean return-to-menu callback. |
| **`GameController.java`** | `com.comp2042` | `com.comp2042.controller` | Moved to `controller` package. Implemented 2-key FIFO input queue (`inputQueue`) to prevent dropped inputs during rapid double-turns; synchronized `turnDir` calls with logic ticks; encapsulated static `key`/`lastKey` as private fields with getters; corrected `valKey` DOWN anti-reverse logic (Bug #40); removed dead code (`calculateBonusScore`, `legacyScoreMultiplier`, `unusedObstacleCount`); reused class-level `Random`; reset turn array on restart; and hooked return-to-menu callback to stop animation and swap scenes. |
| **`GameView.java`** | `com.comp2042` | `com.comp2042.view` | Moved to `view` package. Encapsulated background fill color (`BACKGROUND_COLOR`); eliminated snapshots in favor of `GraphicsContext` rotation; fixed double-scaling bug; eliminated per-frame `SnapshotParameters`; added `resetTurnArray()`; migrated from `Point` to `GridPoint`; decoupled `turnDir` from controller static state; unified tail rendering in `drawBody` under `index == 0`; and delegated `tailRotation` to `Snake.getTailDir()`. |
| **`Game.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Removed UI `Color` coupling; added strongly-typed `Game.Difficulty` enum with tuned speeds (`EASY: 7.0`, `NORMAL: 9.0`, `HARD: 12.0`); renamed dual score tracking (`coinsEaten` vs `displayScore`); replaced magic scaling numbers with named constants; and documented/guarded `decMaxScore`. |
| **`Snake.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Fully migrated from `Point` to `GridPoint`; removed `ImageView` nodes and exposed `static final Image` singletons; extracted `attemptMove` / `wrapCoordinate`; unified border flags into `borderWrap`; switched body to `ArrayDeque`; cached coin collision checks; validate-before-move; renamed constructor parameters; and implemented `getTailDir()` with toroidal shortest-displacement vector calculation for border wrap orientation. |
| **`Obstacle.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Migrated to `GridPoint`; replaced unbounded generation with deterministic free-cell sampling; fixed off-by-one X range; prevented duplicates; encapsulated `getObstacles()` via `Collections.unmodifiableList`; added proportional cap; and renamed dimension fields `boardWidth, boardHeight`. |
| **`Coin.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Migrated to `GridPoint`; replaced unbounded `do...while` with deterministic free-cell sampling to eliminate freeze risks; cached static sprite; added polymorphic `applyEffect` and `getSpeedBonus`; and used shared `Random`. |
| **`BlueCoin.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Cached static sprite, encapsulated border-ignore behavior inside `applyEffect(...)`, and removed redundant `getImage()`. |
| **`GoldCoin.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Cached static sprite, overridden `getPoints()` with constant reward (15), and removed redundant `getImage()`. |
| **`SilverCoin.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Cached static sprite, encapsulated dynamic barrier spawning and score adjustment in `applyEffect(...)`, and removed redundant `getImage()`. |
| **`RainbowCoin.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Cached static sprite, provided speed bonus value, and removed redundant `getImage()`. |

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
