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
    * `com.comp2042.model`: Pure gameplay rules, coordinates, state, and entities (`Game`, `Snake`, `Obstacle`, `Coin`, `BlueCoin`, `GoldCoin`, `SilverCoin`, `RainbowCoin`).
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
* **Snake Model Decoupling & Optimisation:**
  * Removed all JavaFX `ImageView` and `SnapshotParameters` instances from `Snake` (model no longer imports any scene-graph nodes), replacing them with `private static final Image` singletons — enabling headless unit testing.
  * `move()` refactored with shared `attemptMove()` / `wrapCoordinate()` helpers, removing copy-pasted collision/rollback blocks across all four direction cases.
  * Border-wrap behaviour unified into a single `borderWrap` flag, eliminating the redundant `ignoreBorders` / `noBorder` dual-flag anti-pattern.
  * `body` collection changed from `ArrayList` to `ArrayDeque`, making tail removal O(1) on every non-eating tick.
  * "Move first, rollback on collision" pattern replaced by validate-before-move, eliminating fragile inverse-translation code.
* **GameView Rendering Pipeline Rewrite:**
  * All `ImageView.snapshot()` calls replaced by `GraphicsContext.save()` / `gc.rotate()` / `gc.drawImage()` / `gc.restore()` — hardware-accelerated canvas rotation with zero per-frame allocations.
  * Duplicate private `drawGrid` overload removed; a single `drawGrid` implementation handles all call sites.
  * Double-scaling bug (`getMultiplier() * fullScale * fullScale`) corrected to a single `cellSize = getMultiplier() * fullScale` factor applied uniformly to both position and size.
* **Core Gameplay Loop & Features:**
  * **Snake Movement & Controls:** Responsive 4-directional navigation with anti-reverse logic (cannot 180° turn into self).
  * **Polymorphic Food & Coin System:** Spawning coins with distinct attributes and behaviors using object-oriented polymorphism (`Coin`, `GoldCoin`, `SilverCoin`, `BlueCoin`, `RainbowCoin`):
    * Standard Red Coin: Basic point increment (+5).
    * Gold Coin: Higher point reward (+15).
    * Blue Coin: Temporary wall pass-through / border-ignore ability.
    * Silver Coin: Spawns additional barrier obstacles on the board and adjusts target score.
    * Rainbow Coin: Grants speed boost (+5.0 speed) to the game loop.
  * **Asset Caching:** Snake and coin sprites are loaded once as `static final` assets — no disk reads or texture re-allocations per tick or per coin spawn.
  * **Dynamic Obstacle Generation:** Barriers randomly populate without colliding with snake spawn or coins.
  * **Collision Detection:** Accurate wall collision, self-bite collision, and obstacle collision handling.
  * **Score & Win Conditions:** Dynamically updated HUD score label and victory state checks based on board capacity.
  * **Audio Playback:** Low-latency coin collection sound effects via `AudioClip`.
  * **Controls:** Pause (`P`), Reset (`TAB`), Fullscreen toggle (`+` / `-`), Return to Menu (`ESC`).

---

## 4. Implemented but Not Working Properly

* **Flawed Anti-Reverse Validation in `GameController.valKey(KeyCode key)`:**
  * *Issue:* When evaluating a downward turn (`KeyCode.DOWN`), the validation method checks `if (!this.key.equals(KeyCode.DOWN))` instead of `KeyCode.UP`. This allows the snake to inadvertently turn 180° backward into its own neck when pressing Down while moving Up.
  * *Status / Steps Taken:* Documented in bug catalog; scheduled for correction to ensure symmetrical 4-directional anti-reversal logic.
* **Tight Coupling via Public Static Fields:**
  * *Issue:* `GameView.turnDir()` directly reads public static variables from `GameController` (`GameController.key`, `GameController.lastKey`), creating an inverted dependency from View into Controller.
  * *Status / Steps Taken:* Documented; planned to pass direction parameters via method arguments or an observer event to decouple the View from the Controller's static state.
* **Dead Code & Unused Variables:**
  * *Issue:* `calculateBonusScore()` and `legacyScoreMultiplier` in `GameController` are never invoked in gameplay, and `unusedObstacleCount` in `reset()` is unused.

---

## 5. Features Not Implemented

* **Persistent High Score Table:**
  * *Reason:* Local disk/file-based leaderboard was not present in the original codebase; left out to prioritize core architectural refactoring and stability.
* **Difficulty Selection UI:**
  * *Reason:* The `Game` model supports different difficulties (`'E'`, `'N'`, `'H'`), but the `MainMenu.fxml` UI currently hardcodes difficulty to `'N'`. An interactive menu selector will be added in a future update.
* **Settings & Volume Control Slider:**
  * *Reason:* Focused on core gameplay mechanics and architectural decoupling before adding secondary audio settings.
* **Headless Automated Unit Testing Suite:**
  * *Reason:* `Snake` has been decoupled from JavaFX scene-graph nodes (no `ImageView` imports remain). Pure POJO unit tests for snake logic can now run without `Toolkit not initialized` exceptions. Full test suite implementation deferred to a future iteration.

---

## 6. New Java Classes

*(No new Java classes introduced at this stage. All original 13 source files were strictly preserved and reorganized into the MVC directory structure without adding extra code files.)*

*Future classes planned for this assignment:*
* `AssetManager` / `SoundManager`: Centralized asset cache to prevent repeated image instantiation.
* `CoinFactory`: Factory pattern for coin creation.

---

## 7. Modified Java Classes

| Class | Original Package | New Package | Changes Made & Rationale |
| :--- | :--- | :--- | :--- |
| **`Main.java`** | `com.comp2042` | `com.comp2042` | Added explicit `ViewManager` import and programmatic font loading to ensure custom pixel fonts register across systems. |
| **`ViewManager.java`** | `com.comp2042` | `com.comp2042.view` | Moved to `view` package. Replaced fragile file path strings with `getClass().getResource(...)` for `MainMenu.fxml` and window icon. |
| **`MainMenuController.java`** | `com.comp2042` | `com.comp2042.controller` | Moved to `controller` package. Added cross-package imports for models and view components. |
| **`GameController.java`** | `com.comp2042` | `com.comp2042.controller` | Moved to `controller` package. Added explicit imports for models/view. Refactored `powerCoin()` to use polymorphic dispatch (`coin.applyEffect(...)`), and corrected `newCoin()` to spawn `RainbowCoin` on `ran == 2`. |
| **`GameView.java`** | `com.comp2042` | `com.comp2042.view` | Moved to `view` package. Updated CSS stylesheet resolution using `getResource(...).toExternalForm()` and imported required models/controllers. Rendering pipeline rewritten: `ImageView.snapshot()` replaced by `GraphicsContext.save()/rotate()/drawImage()/restore()` (Bugs #11, #20); duplicate private `drawGrid` overload removed (Bug #1); double-scaling `fullScale * fullScale` corrected to single `cellSize` factor (Bug #9); all per-frame `SnapshotParameters` allocations eliminated (Bug #7). |
| **`Game.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package to encapsulate game rules and scoring state. |
| **`Snake.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Major refactor: (1) all `ImageView` / `SnapshotParameters` removed — model now exposes `static final Image` singletons only (Bugs #11, #20); (2) `move()` refactored with `attemptMove()` / `wrapCoordinate()` helpers removing 4-way copy-paste (Bug #13); (3) redundant `ignoreBorders` / `noBorder` flags merged into single `borderWrap` boolean (Bug #12); (4) `body` changed from `ArrayList` to `ArrayDeque` for O(1) tail removal (Bug #14); (5) `ateCoin()` caches equality result to avoid double evaluation (Bug #15); (6) validate-before-move replaces move-then-rollback pattern (Bug #17); (7) `borderControl()` renamed to `isOutOfBounds(Point)` (Bug #18); (8) constructor params renamed `x, y` → `gameWidth, gameHeight` (Bug #19). |
| **`Obstacle.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Preserved obstacle coordinates and barrier generation logic. |
| **`Coin.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Cached static `RED_COIN_IMAGE`, added polymorphic `applyEffect(...)` and `getSpeedBonus()`, and optimized collision generation using a shared static `Random`. |
| **`BlueCoin.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Cached static `BLUE_COIN_IMAGE`, encapsulated border-ignore behavior inside `applyEffect(...)`, and removed redundant `getImage()`. |
| **`GoldCoin.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Cached static `GOLD_COIN_IMAGE`, overridden `getPoints()` with constant reward (15), and removed redundant `getImage()`. |
| **`SilverCoin.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Cached static `SILVER_COIN_IMAGE`, encapsulated dynamic barrier spawning and score adjustment in `applyEffect(...)`, and removed redundant `getImage()`. |
| **`RainbowCoin.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Cached static `RAINBOW_COIN_IMAGE`, provided speed bonus value, and removed redundant `getImage()`. |

---

## 8. Unexpected Problems

1. **Relative Resource Path Failures in JavaFX:**
   * *Problem:* The original codebase relied on working-directory paths (e.g. `"resources/views/res/..."`). When the application was compiled or packaged into a JAR, these paths failed because resources were inside the JAR rather than loose files on disk.
   * *Resolution:* Converted all asset references to standard classpath resource streams (`getClass().getResourceAsStream("/com/comp2042/...")`).

2. **CSS Font-Face Loading Across Environments:**
   * *Problem:* Custom `@font-face` definitions inside `styling.css` failed to register fonts consistently on some JVMs when packaged.
   * *Resolution:* Added programmatic font loading (`Font.loadFont(...)`) inside `Main.java` at application startup as a reliable fallback.

3. **FXML Controller Binding Mismatch:**
   * *Problem:* When moving `MainMenuController` into `com.comp2042.controller`, `MainMenu.fxml` failed to launch because `fx:controller` still pointed to `com.comp2042.MainMenuController`.
   * *Resolution:* Updated `MainMenu.fxml` with the fully qualified package name `fx:controller="com.comp2042.controller.MainMenuController"`.

4. **Broken Maven Wrapper Distribution URL:**
   * *Problem:* The repository's `.mvn/wrapper/maven-wrapper.properties` contained a non-existent Maven version URL (`.../apache-maven/3/apache-maven-3-bin.zip`), causing wrapper scripts and build tools to fail with HTTP 404 / `FileNotFoundException`.
   * *Resolution:* Corrected the `distributionUrl` to point to the official `apache-maven-3.9.6-bin.zip` release archive.

