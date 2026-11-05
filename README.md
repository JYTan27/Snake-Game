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
* **Core Gameplay Loop & Features:**
  * **Snake Movement & Controls:** Responsive 4-directional navigation with anti-reverse logic (cannot 180° turn into self).
  * **Food & Coin System:** Spawning coins with different attributes (points, speed boost, obstacle generation, border pass-through).
  * **Dynamic Obstacle Generation:** Barriers randomly populate without colliding with snake spawn or coins.
  * **Collision Detection:** Accurate wall collision, self-bite collision, and obstacle collision handling.
  * **Score & Win Conditions:** Dynamically updated HUD score label and victory state checks based on board capacity.
  * **Audio Playback:** Low-latency coin collection sound effects via `AudioClip`.
  * **Controls:** Pause (`P`), Reset (`TAB`), Fullscreen toggle (`+` / `-`), Return to Menu (`ESC`).

---

## 4. Implemented but Not Working Properly

* **Duplicate Random Evaluation in `GameController.newCoin()`:**
  * *Issue:* Lines 190 and 192 in `GameController` evaluate `if (ran == 1)` twice:
    ```java
    if (ran == 1) {
        coin = new GoldCoin(...);
    } else if (ran == 1) { // Unreachable!
        coin = new RainbowCoin(...);
    }
    ```
    This causes the `RainbowCoin` branch to be dead code when `ran == 1`.
  * *Status / Steps Taken:* Identified during code audit; planned to assign distinct random integer ranges to ensure all coin varieties spawn with their intended rarity.
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
  * *Reason:* Legacy models still import JavaFX `Image`/`ImageView` components; pure POJO decoupling must be completed before unit tests can run without `Toolkit not initialized` exceptions.

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
| **`GameController.java`** | `com.comp2042` | `com.comp2042.controller` | Moved to `controller` package. Added explicit imports for models, view, and updated sound file path to classpath resource URL. |
| **`GameView.java`** | `com.comp2042` | `com.comp2042.view` | Moved to `view` package. Updated CSS stylesheet resolution using `getResource(...).toExternalForm()` and imported required models/controllers. |
| **`Game.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package to encapsulate game rules and scoring state. |
| **`Snake.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Updated sprite paths to point to `/com/comp2042/images/snake/` via `getResourceAsStream`. |
| **`Obstacle.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Preserved obstacle coordinates and barrier generation logic. |
| **`Coin.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Updated base coin image path to `/com/comp2042/images/coins/redcoin.png`. |
| **`BlueCoin.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Updated sprite path to `/com/comp2042/images/coins/bluecoin.png`. |
| **`GoldCoin.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Updated sprite path to `/com/comp2042/images/coins/goldcoin.png`. |
| **`SilverCoin.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Updated sprite path to `/com/comp2042/images/coins/silvercoin.png`. |
| **`RainbowCoin.java`** | `com.comp2042` | `com.comp2042.model` | Moved to `model` package. Updated sprite path to `/com/comp2042/images/coins/rainbowcoin.png`. |

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

