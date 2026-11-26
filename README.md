# Advanced Snake Game (JavaFX)

**Module** COMP2042 Developing Maintainable Software

**Academic Year** 2026/2027

---

## 1. GitHub

- [Coursework repository](https://github.com/JYTan27/Snake-Game)
- [Numbered bug fixes and improvement log](BUGS_AND_IMPROVEMENTS.md)

---

## 2. Compilation Instructions

### Prerequisites & Dependencies

- **JDK 23 or later** is required by JavaFX 25.0.1. Maven downloads the JavaFX dependencies declared in [pom.xml](pom.xml). [OpenJFX 25 release notes](https://gluonhq.com/products/javafx/openjfx-25-release-notes/)
- The included Maven Wrapper uses Maven 3.9.6. On macOS or Linux, use `sh mvnw` in place of `.\mvnw.cmd`. If Maven is already installed, use `mvn`.

### Step-by-Step Compilation & Execution Guide

1. Clone the repository and open the project directory.

   ```powershell
   git clone https://github.com/JYTan27/Snake-Game.git
   cd Snake-Game
   ```

2. Compile the source.

   ```powershell
   .\mvnw.cmd clean compile
   ```

3. Run the application.

   ```powershell
   .\mvnw.cmd javafx:run
   ```

4. Optionally, package and run the **JAR (Java Archive)**. A JAR bundles compiled Java code and resources into one file. This project's JAR also includes the JavaFX libraries for the build platform.

   ```powershell
   .\mvnw.cmd clean package
   java -jar target/snake-javafx.jar
   ```

---

## 3. Implemented and Working Properly

### Maintenance and refactoring

- **Model–View–Controller (MVC) and package organisation.** The original classes shared one package. The model now owns movement, scoring, and board state, the view draws the game, and controllers connect inputs and screen actions to those rules. Pickups, run statistics, audio, and persistence have dedicated packages so related changes are easier to locate. [Model](src/main/java/com/comp2042/model), [View](src/main/java/com/comp2042/view), [Controller](src/main/java/com/comp2042/controller), [Services](src/main/java/com/comp2042/service)
- **Encapsulation and readability.** Moved the current and previous directions from public static fields into each game controller and passed turn information to the view when needed. This removes the view's dependency on global controller state. Replaced AWT coordinates with `GridPoint` and clarified names for board dimensions and boundary checks. The obstacle list can be read by the view while additions and resets stay in the model. [GameController](src/main/java/com/comp2042/controller/GameController.java), [GridPoint](src/main/java/com/comp2042/model/GridPoint.java), [Snake](src/main/java/com/comp2042/model/Snake.java), [Obstacle](src/main/java/com/comp2042/model/Obstacle.java)
- **Movement and collision handling.** Four direction branches used to repeat border and collision logic. They now share one movement path that checks the destination after any wrap before advancing the snake. This closes the wrapped-edge collision gap and preserves the last valid pose on blocked moves. Storing the body in a deque also avoids shifting every segment when removing the tail. [Snake](src/main/java/com/comp2042/model/Snake.java)
- **Placement and full-board completion.** Random retries could stall on a crowded board, and the old obstacle bounds could produce invalid positions. Pickups and obstacles now choose from available cells. When no pickup cell remains, the factory reports a full board and the controller finishes the run. This uses board occupancy as the win condition and removes the need to adjust a separate target score when obstacles are added. [Pickup](src/main/java/com/comp2042/model/pickup/Pickup.java), [Obstacle](src/main/java/com/comp2042/model/Obstacle.java), [PickupFactory](src/main/java/com/comp2042/model/pickup/PickupFactory.java), [GameController](src/main/java/com/comp2042/controller/GameController.java)
- **Pickup polymorphism and factory.** The controller originally checked coin types to decide which effects to apply. Pickup subclasses now define their own effects, and `Game` applies effects, speed changes, and points in one place. The factory handles creating the next pickup using weights from `GameMode`. This keeps mode rules, pickup effects, and creation separate so changing one does not require editing the game loop. [Pickup](src/main/java/com/comp2042/model/pickup/Pickup.java), [Game](src/main/java/com/comp2042/model/Game.java), [GameMode](src/main/java/com/comp2042/model/GameMode.java), [PickupFactory](src/main/java/com/comp2042/model/pickup/PickupFactory.java)
- **Rendering performance and board appearance.** The original renderer created snapshots to rotate individual snake segments. The view now rotates cached sprites directly on the canvas and takes one body-list copy per redraw. A shared cell size corrects double scaling and keeps tiles and sprites aligned. The board renderer and palette are separate, so a colour change needs no gameplay changes. [GameView](src/main/java/com/comp2042/view/GameView.java), [BoardRenderer](src/main/java/com/comp2042/view/board/BoardRenderer.java), [BoardTheme](src/main/java/com/comp2042/view/board/BoardTheme.java)
- **Navigation and shared controls.** Reusing one stage and scene prevents extra windows and lets fullscreen persist between the game and menu screens. One mode selector configuration serves both the main menu and leaderboards, keeping labels, selection sounds, and fullscreen sizing consistent. [ViewManager](src/main/java/com/comp2042/view/ViewManager.java), [GameModeSelector](src/main/java/com/comp2042/view/GameModeSelector.java)
- **Audio and leaderboard responsibilities.** Controllers request a sound by its purpose, while the shared service handles clip lookup, caching, and playback. Leaderboard ranking and summaries are also separate from XML storage through a repository interface. This gives each service one place to handle its own rules and keeps media and file handling out of the controllers. [SoundService](src/main/java/com/comp2042/service/audio/SoundService.java), [JavaFxSoundService](src/main/java/com/comp2042/service/audio/JavaFxSoundService.java), [LeaderboardService](src/main/java/com/comp2042/service/leaderboard/LeaderboardService.java), [LeaderboardRepository](src/main/java/com/comp2042/service/leaderboard/LeaderboardRepository.java)
- **Build and resource loading.** Working-directory paths could break when the game was packaged. Sprites, sounds, fonts, and layouts now sit under Maven resources and load from the classpath. Registering fonts at startup and correcting the Maven Wrapper download URL also made the build and launch steps more reliable. [Resources](src/main/resources/com/comp2042), [Main](src/main/java/com/comp2042/Main.java), [Maven Wrapper settings](.mvn/wrapper/maven-wrapper.properties)

### Gameplay and interface extensions

- **Five game modes.** Modes vary speed, borders, and pickup selection to give players distinct challenges. The main menu defaults to Classic and remembers the selection during the session. The help screen uses the same mode descriptions. [GameMode](src/main/java/com/comp2042/model/GameMode.java), [MainMenuController](src/main/java/com/comp2042/controller/MainMenuController.java), [HowToPlayController](src/main/java/com/comp2042/controller/HowToPlayController.java)

  | Mode | Rules |
  | --- | --- |
  | Classic | Steady speed, solid borders, and diamonds. |
  | Obstacles | Solid borders, diamonds, and blocks that add barriers. Once barriers fill 5% of the board, diamonds replace blocks. |
  | Hyperspeed | Faster movement, solid borders, and diamonds. |
  | Power-ups | Diamonds, bonus-point stars, rings that enable wrapping, and bolts that increase speed. Ring and bolt effects last until the next pickup. |
  | Wraparound | Diamonds and permanent edge wrapping. |

- **Controls and responsive input.** **WASD** starts, moves, or resumes the snake. Up to two turns can be queued so quick inputs are applied in order. Reverse turns are blocked. **P** pauses, **Tab** restarts in the same mode, and **Esc** returns to the menu. [GameController](src/main/java/com/comp2042/controller/GameController.java)
- **Win, loss, and restart.** Points reward pickups. Filling all available cells wins the run, and hitting the snake's own body ends it in every mode. The game stops and shows the result. On a loss, the snake's head immediately switches to the dead-head sprite. Pressing **Tab** restores the normal head and clears points, obstacles, temporary effects, and the previous end state. [Game](src/main/java/com/comp2042/model/Game.java), [GameController](src/main/java/com/comp2042/controller/GameController.java), [Dead-head sprite](src/main/resources/com/comp2042/images/snake/snakedeadhead.png)
- **Menu and help.** Added a rotating splash message on menu visits, an **Exit** button, and a scrollable **How to Play** screen with fixed navigation. [MainMenuController](src/main/java/com/comp2042/controller/MainMenuController.java), [ViewManager](src/main/java/com/comp2042/view/ViewManager.java), [Screen layouts](src/main/resources/com/comp2042/view)
- **Board Colours.** Six checkerboard palettes can be previewed and saved across launches. Soft Cream is the default. Palette settings are separate from gameplay rules. [BoardColoursController](src/main/java/com/comp2042/controller/BoardColoursController.java), [BoardThemePreferences](src/main/java/com/comp2042/view/board/BoardThemePreferences.java), [CheckerboardBoardRenderer](src/main/java/com/comp2042/view/board/CheckerboardBoardRenderer.java)
- **Local leaderboards.** Each mode shows its top 10 scores plus full-history totals, best and average scores, and active time. Runs rank by score, with ties in recording order. Wins, losses, and abandoned runs count once, and active time excludes pauses and initial waiting. A fixed mode selector lets the player browse results independently of the main menu selection. History is saved locally in XML so results survive restarts. The leaderboard reports storage problems. [LeaderboardController](src/main/java/com/comp2042/controller/LeaderboardController.java), [RunSession](src/main/java/com/comp2042/model/stats/RunSession.java), [LeaderboardService](src/main/java/com/comp2042/service/leaderboard/LeaderboardService.java), [XmlLeaderboardRepository](src/main/java/com/comp2042/service/leaderboard/XmlLeaderboardRepository.java)
- **Audio.** A shared service plays distinct pickup sounds and cues for buttons, pause, resume, restart, win, and loss. The menu loops through three different music tracks. It pauses for gameplay and resumes when returning to the menu. [SoundEffect](src/main/java/com/comp2042/service/audio/SoundEffect.java), [JavaFxSoundService](src/main/java/com/comp2042/service/audio/JavaFxSoundService.java), [BackgroundMusic](src/main/java/com/comp2042/service/audio/BackgroundMusic.java)

---

## 4. Implemented but Not Working Properly

- All features working as intended.

---

## 5. Remaining Unfixed Bugs / Open Issues

- No unresolved bugs.

---

## 6. Features Not Implemented

- **Game AI:** Add an AI player that can fill the board and win in any game mode.
  - **Reason:** The game was designed around player input. An AI that can win would need a separate path planner that handles each mode's rules and avoids trapping the snake as it grows.
- **Modifiers.** Define reusable speed, obstacle, border-wrapping rules etc for Sandbox Mode and Daily Challenge Mode.
  - **Reason.** Each mode currently defines its rules as a preset. Reusable modifiers would require separating those rules from the presets and defining how they interact when combined.
- **Sandbox Mode:** Let players build a custom game by combining compatible modifiers.
  - **Reason.** The menu currently starts games from fixed presets. A sandbox would need a setup screen and rules for rejecting incompatible combinations before play.
- **Daily Challenge Mode.** Randomly select a compatible combination of modifiers for each day's challenge.
  - **Reason.** This depends on the shared modifier system. It also needs a way to generate a compatible challenge and keep it consistent throughout the day.
- **Multiplayer.** Add two-player games in which either snake can die by hitting the other snake's body.
  - **Reason.** The game loop manages one snake. Supporting two players would require separate input handling and resolving both snakes' movements and collisions together.
- **Settings.** Add remappable controls, in-app audio volume controls, and adjustable interface scaling in fullscreen.
  - **Reason.** The game currently uses fixed key bindings, default audio levels, and automatic fullscreen scaling. A shared settings screen would need to save and apply player preferences and prevent conflicting key bindings.
- **Snake Skin Customisation.** Offer skin presets with a preview like Board Colours, plus a custom colour picker for the snake.
  - **Reason.** The existing customisation work covers the board. Snake skins would need a separate way to preview and recolour the head, body, turns, and tail consistently.
- **Background Music Controls.** Add controls at the top left of every menu screen to pause, resume, skip, or select a track.
  - **Reason.** The current soundtrack is managed automatically when screens change. Manual playback would require shared controls across the menus and rules for preserving the player's choice when navigating.
- **Snake Death Animation.** Animate the snake when it dies.
  - **Reason.** The current death effect swaps the head sprite in the snake's final pose. An animation would need a timed sequence that can be interrupted by restart or menu navigation.
- **Pause Popup.** Show a pause screen with a Resume button and a countdown before movement restarts.
  - **Reason.** The current pause resumes directly from keyboard input. A popup would add a countdown state that must hold movement until it finishes and cancel cleanly on restart or exit.
- **Snake Turning Sounds.** Play one of four short sounds based on the direction of each turn.
  - **Reason.** The audio work focused on pickups and major game events. Turn sounds would play much more frequently and would need to be coordinated with buffered turns and other effects.
- **Achievements.** Add achievements for different milestones across all game modes.
  - **Reason.** The leaderboard stores run results and summaries. Achievements would also need unlock criteria, progress tracking, and saved unlocks across modes.
- **Custom Board Colour Picker.** Let players choose their own combination of checkerboard colours.
  - **Reason.** Board Colours currently previews and saves predefined palettes. A custom picker would require previewing and saving individual colours in addition to preset choices.

---

## 7. New Java Classes

| Class or interface | Package | Purpose |
| --- | --- | --- |
| [GridPoint](src/main/java/com/comp2042/model/GridPoint.java) | `com.comp2042.model` | Represents a board cell and replaces the dependency on AWT coordinates. |
| [GameMode](src/main/java/com/comp2042/model/GameMode.java) | `com.comp2042.model` | Defines the five modes, their rules, pickup weights, and descriptions for the menu and help screen. |
| [DiamondPickup](src/main/java/com/comp2042/model/pickup/DiamondPickup.java) | `com.comp2042.model.pickup` | Standard growth and scoring pickup. Used at the start of each run and as the obstacle-cap fallback. |
| [PickupType](src/main/java/com/comp2042/model/pickup/PickupType.java) | `com.comp2042.model.pickup` | Identifies pickup types for mode configuration, spawning, and sound selection. |
| [PickupFactory](src/main/java/com/comp2042/model/pickup/PickupFactory.java) | `com.comp2042.model.pickup` | Creates mode-appropriate pickups and reports when the board has no free cell. |
| [WeightedPickupSelector](src/main/java/com/comp2042/model/pickup/WeightedPickupSelector.java) | `com.comp2042.model.pickup` | Chooses a pickup type using the weights defined by the current mode. |
| [RunOutcome](src/main/java/com/comp2042/model/stats/RunOutcome.java) | `com.comp2042.model.stats` | Defines win, loss, and abandoned outcomes. |
| [RunResult](src/main/java/com/comp2042/model/stats/RunResult.java) | `com.comp2042.model.stats` | Stores a completed run's mode, score, outcome, duration, length, and timestamp. |
| [RunSession](src/main/java/com/comp2042/model/stats/RunSession.java) | `com.comp2042.model.stats` | Tracks active play time and finalises a run once. |
| [ModeStatistics](src/main/java/com/comp2042/model/stats/ModeStatistics.java) | `com.comp2042.model.stats` | Holds aggregate results for one mode. |
| [HowToPlayController](src/main/java/com/comp2042/controller/HowToPlayController.java) | `com.comp2042.controller` | Shows mode descriptions in the help screen and handles Back navigation. |
| [LeaderboardController](src/main/java/com/comp2042/controller/LeaderboardController.java) | `com.comp2042.controller` | Displays rankings, statistics, and storage warnings for the selected mode. |
| [BoardColoursController](src/main/java/com/comp2042/controller/BoardColoursController.java) | `com.comp2042.controller` | Previews checkerboard palettes and applies the selected palette. |
| [GameModeSelector](src/main/java/com/comp2042/view/GameModeSelector.java) | `com.comp2042.view` | Shares mode labels, selection sounds, and fullscreen dropdown behaviour across menus. |
| [BoardRenderer](src/main/java/com/comp2042/view/board/BoardRenderer.java) | `com.comp2042.view.board` | Defines how a board background is drawn. |
| [CheckerboardBoardRenderer](src/main/java/com/comp2042/view/board/CheckerboardBoardRenderer.java) | `com.comp2042.view.board` | Draws checkerboard tiles aligned with the gameplay grid. |
| [BoardTheme](src/main/java/com/comp2042/view/board/BoardTheme.java) | `com.comp2042.view.board` | Defines the six board colour presets. |
| [BoardThemePreferences](src/main/java/com/comp2042/view/board/BoardThemePreferences.java) | `com.comp2042.view.board` | Loads and saves the selected palette across launches. |
| [SoundEffect](src/main/java/com/comp2042/service/audio/SoundEffect.java) | `com.comp2042.service.audio` | Maps gameplay and menu events to clips and defines the default effects volume. |
| [SoundService](src/main/java/com/comp2042/service/audio/SoundService.java) | `com.comp2042.service.audio` | Provides a shared interface for sound effects. |
| [JavaFxSoundService](src/main/java/com/comp2042/service/audio/JavaFxSoundService.java) | `com.comp2042.service.audio` | Caches and plays effects and manages the Windows audio output line. |
| [BackgroundMusic](src/main/java/com/comp2042/service/audio/BackgroundMusic.java) | `com.comp2042.service.audio` | Loops menu tracks and pauses or resumes them as screens change. |
| [LeaderboardRepository](src/main/java/com/comp2042/service/leaderboard/LeaderboardRepository.java) | `com.comp2042.service.leaderboard` | Defines how run history is loaded and saved. |
| [XmlLeaderboardRepository](src/main/java/com/comp2042/service/leaderboard/XmlLeaderboardRepository.java) | `com.comp2042.service.leaderboard` | Stores run history in a local XML file. |
| [LeaderboardService](src/main/java/com/comp2042/service/leaderboard/LeaderboardService.java) | `com.comp2042.service.leaderboard` | Records runs and calculates per-mode rankings and summaries. |

---

## 8. Modified Java Classes

| Class | Original Package | New Package | Changes Made & Rationale |
| --- | --- | --- | --- |
| [Main](src/main/java/com/comp2042/Main.java) | `com.comp2042` | `com.comp2042` | • Loads bundled fonts at startup so they are available from the packaged application.<br>• Creates the shared audio and leaderboard services.<br>• Finalises active gameplay and closes audio on shutdown. |
| [ViewManager](src/main/java/com/comp2042/view/ViewManager.java) | `com.comp2042` | `com.comp2042.view` | • Reuses one stage and scene so switching screens preserves focus and fullscreen.<br>• Centralises navigation, shared services, and session settings so screens use the same application state. |
| [MainMenuController](src/main/java/com/comp2042/controller/MainMenuController.java) | `com.comp2042` | `com.comp2042.controller` | • Adds the mode selector and navigation to the new menu screens.<br>• Starts games with the selected mode and board palette so the launch uses the player's choices. |
| [GameController](src/main/java/com/comp2042/controller/GameController.java) | `com.comp2042` | `com.comp2042.controller` | • Buffers up to two WASD turns and applies them in order so quick inputs are handled across successive moves.<br>• Delegates pickup effects and scoring rules to `Game`, keeping the controller focused on coordinating play.<br>• Handles run endings and records the outcome for the leaderboard. |
| [GameView](src/main/java/com/comp2042/view/GameView.java) | `com.comp2042` | `com.comp2042.view` | • Draws rotated sprites directly on the canvas and reuses one body list per redraw to reduce rendering work.<br>• Uses a common cell size so the selected checkerboard, snake, and pickups stay aligned in fullscreen.<br>• Shows the win or loss message from the game state, removing duplicate visibility flags.<br>• Draws the dead-head sprite at the normal head's position and rotation after a fatal collision. |
| [Game](src/main/java/com/comp2042/model/Game.java) | `com.comp2042` | `com.comp2042.model` | • Uses `GameMode` for speed and default borders and applies scoring and pickup effects in one place.<br>• Tracks victory separately from display points so filling the available board determines completion. |
| [Snake](src/main/java/com/comp2042/model/Snake.java) | `com.comp2042` | `com.comp2042.model` | • Checks destinations before moving, including after wrapping, so blocked moves preserve the last valid pose.<br>• Stores body segments in a deque so removing the tail avoids shifting the other segments. |
| [Obstacle](src/main/java/com/comp2042/model/Obstacle.java) | `com.comp2042` | `com.comp2042.model` | Selects valid free cells and applies a board-size-based cap so obstacles stay in bounds and never overlap. |
| [Pickup (formerly Coin)](src/main/java/com/comp2042/model/pickup/Pickup.java) | `com.comp2042` | `com.comp2042.model.pickup` | Shares placement and scoring behaviour across pickup types and lets each subclass define its effect. |
| [StarPickup (formerly GoldCoin)](src/main/java/com/comp2042/model/pickup/StarPickup.java) | `com.comp2042` | `com.comp2042.model.pickup` | Provides bonus points and uses a cached sprite. |
| [RingPickup (formerly BlueCoin)](src/main/java/com/comp2042/model/pickup/RingPickup.java) | `com.comp2042` | `com.comp2042.model.pickup` | Applies temporary border wrapping through the shared pickup interface. |
| [BoltPickup (formerly RainbowCoin)](src/main/java/com/comp2042/model/pickup/BoltPickup.java) | `com.comp2042` | `com.comp2042.model.pickup` | Provides a temporary speed bonus through the shared pickup interface. |
| [BlocksPickup (formerly SilverCoin)](src/main/java/com/comp2042/model/pickup/BlocksPickup.java) | `com.comp2042` | `com.comp2042.model.pickup` | • Adds barriers within the board's placement limit.<br>• Removes the old score-target adjustment because victory is determined by available cells. |

---

## 9. Unexpected Problems

1. **Custom fonts did not load consistently.**
   - **Cause:** The CSS paths used to register the bundled fonts did not resolve reliably.
   - **Fix:** Registered the fonts directly from the bundled resources at application startup.

2. **The main menu could fail to load after the controller moved.**
   - **Cause:** The FXML still referred to the controller's old package. A failed load could also leave the application trying to open a screen without content.
   - **Fix:** Updated the controller binding and made failed screen loads stop with an error message.

3. **The Maven Wrapper could not download Maven.**
   - **Cause:** Its configuration pointed to a distribution archive that did not exist.
   - **Fix:** Updated the URL to the Maven 3.9.6 archive.

4. **Switching screens could lose focus or interrupt fullscreen.**
   - **Cause:** The original game opened a separate window for gameplay. Later, replacing the scene on the same window still disrupted fullscreen.
   - **Fix:** Reused one window and one scene, switching only the displayed content.

5. **A pickup could disable permanent border wrapping.**
   - **Cause:** The collection logic cleared wrapping before applying the pickup effect, even in a mode with wrapping enabled by default.
   - **Fix:** Restored the current mode's border rule before applying the pickup effect.

6. **The snake could pass through an obstacle when wrapping.**
   - **Cause:** The movement logic checked for obstacles only on moves that stayed inside the board.
   - **Fix:** Resolved the wrapped destination first and checked it for obstacles before moving.

7. **The plus shortcut failed to enter fullscreen on some keyboards.**
   - **Cause:** The handler expected a `PLUS` key event, but Shift+= could be reported as `EQUALS` with Shift held.
   - **Fix:** Replaced the plus and minus shortcuts with **F** to toggle fullscreen.

8. **The snake's head and neck overlapped after a fatal wall or obstacle collision.**
   - **Cause:** The body advanced even when the head was blocked.
   - **Fix:** Advanced the body only after a valid move, preserving the last valid pose.

9. **Crowded boards could stall, and full boards could show an invalid pickup or miss the win message.**
   - **Cause:** Placement kept trying random cells even when none were available. A later fallback could use an occupied cell, and a win branch could return before updating the display.
   - **Fix:** Placed pickups and obstacles in available cells. When no pickup cell remains, one victory path stops the game and shows the final score and win message.

10. **The mode dropdown grew after a fullscreen selection.**
    - **Cause:** Scaled popup rows affected the height of the main dropdown control.
    - **Fix:** Kept the main dropdown height fixed and scaled its popup rows separately.

11. **Short sound effects played late on Windows.**
    - **Cause:** On the affected setup, the delay returned when the audio output line was closed between effects.
    - **Fix:** Kept the output line initialised on Windows and retained standard playback if it could not be opened.

12. **The original `RainbowCoin` never spawned.**
    - **Cause:** Both branches checked the same random value, so the gold coin branch always handled that roll.
    - **Fix:** Gave the branches separate values. The current weighted selector gives the equivalent bolt pickup its own chance in Power-ups.

13. **Snake sprites were drawn at the wrong size in fullscreen.**
    - **Cause:** Sprite dimensions applied the fullscreen scaling factor twice.
    - **Fix:** Used one shared cell size for drawing positions and dimensions.

14. **Obstacles could appear off the board or overlap.**
    - **Cause:** The column calculation started at one and could select a column beyond the board. Placement also ignored existing obstacles.
    - **Fix:** Selected in-bounds cells and excluded cells already occupied by obstacles.

15. **The player could reverse from up to down.**
    - **Cause:** The down-input check looked for the wrong current direction, so it failed to reject down while the snake was moving up.
    - **Fix:** Applied the opposite-direction rule consistently to all four directions.

16. **The tail sprite could flip at a wrapped edge.**
    - **Cause:** Tail orientation treated cells on opposite edges as far apart even when they were adjacent through wrapping.
    - **Fix:** Calculated the tail's direction using the wrapped distance between its segments.

17. **Blocks could still spawn after the obstacle placement cap was reached.**
    - **Cause:** Placement stopped at 5% of the board, but the pickup factory still checked against a 25% limit.
    - **Fix:** Used the same 5% cap for placement and eligibility. At the cap, a block roll produces a diamond.
