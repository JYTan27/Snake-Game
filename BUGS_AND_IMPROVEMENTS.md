# Bugs / Unoptimised Sections

1. **Duplicate `drawGrid` methods** – there are two `drawGrid` implementations (one handling obstacles, one without) causing dead code and maintenance overhead. ✅ Fixed.
2. **`drawBody` snapshot per segment** – `ImageView.snapshot` is called for every snake body segment each tick, creating many temporary `Image` objects and leading to GC spikes and performance degradation. ✅ Fixed (rendering now uses `GraphicsContext.rotate`).
3. **Duplicate `if (ran == 1)` in `GameController.newCoin`** – logical error prevents the `RainbowCoin` branch from ever executing. ✅ Fixed (condition corrected to `ran == 2`).
4. **Static coupling of `GameView` to `GameController.key`** – `GameView` reads controller static fields directly, violating MVC separation and making testing harder. ✅ Fixed (key/lastKey are private instance fields; `turnDir` accepts direction parameters).
5. **Dead code in `GameController.reset()`** – methods/variables like `calculateBonusScore()`, `legacyScoreMultiplier`, `unusedObstacleCount` are never used. ✅ Fixed (removed unused methods and variables).
6. **Misleading `multiplier` naming & magic numbers** – hard‑coded values (10, 20, 50) for board scaling are opaque and should be replaced with named constants. ✅ Fixed (introduced `SMALL_BOARD_MULTIPLIER`, `LARGE_BOARD_MULTIPLIER`, `LARGE_BOARD_THRESHOLD`, and reusable `Random`).
7. **Repeated creation of `SnapshotParameters` set to `Color.TRANSPARENT`** – this object is recreated for every draw call; could be cached for efficiency. ✅ Fixed (eliminated per-frame snapshot allocations).
8. **Potential stale entries in `turnArray`** – cleanup only occurs when the tail passes a turn; if the snake dies mid‑turn, stale entries may remain. ✅ Fixed (`resetTurnArray()` clears all turn history upon game reset).
9. **Double scaling bug in `drawBody`** – image size calculated as `game.getMultiplier() * fullScale * fullScale`, resulting in stretched sprites; should use a single `fullScale` factor. ✅ Fixed (single `cellSize` factor).
10. **Public static `key` / `lastKey` variables** – expose global state, making unit testing difficult and increasing coupling. ✅ Fixed (encapsulated as private instance fields with getters).
11. **MVC architecture violation in `Snake` (UI components in Model)** – `Snake` loads JavaFX `Image` and manages `ImageView` instances directly inside the model, breaking MVC boundaries and preventing headless unit testing of pure snake game logic. ✅ Fixed (model carries no scene-graph `ImageView` nodes).
12. **Redundant state flags (`ignoreBorders` vs `noBorder`) in `Snake`** – two separate boolean flags track the same border pass‑through behaviour, creating redundant conditions like `(!ignoreBorders && !noBorder)` across all movement checks. ✅ Fixed (merged into `borderWrap`).
13. **Code duplication in `Snake.move(char dir)`** – border collision checks, obstacle collision checks, coordinate rollback on death, and edge wrapping are copy‑pasted across all four direction cases (`U`, `D`, `L`, `R`). ✅ Fixed (extracted `attemptMove` and `wrapCoordinate`).
14. **Inefficient `body.remove(0)` in `Snake.ateCoin()`** – `body` is stored in an `ArrayList`, so removing index 0 on every non‑eating tick causes an $O(N)$ array shift; a `Deque` or `LinkedList` provides $O(1)$ operations. ✅ Fixed (now `ArrayDeque` with `pollFirst`).
15. **Redundant equality checks in `Snake.ateCoin()`** – `head.equals(coin.getPos())` is evaluated twice per invocation instead of caching the result in a local variable. ✅ Fixed (result cached in local variable).
16. **Coupling to legacy AWT (`java.awt.Point`)** – uses `java.awt.Point` inside a JavaFX project, mixing legacy desktop UI dependencies with JavaFX. ✅ Fixed (created custom `GridPoint` class in `com.comp2042.model` and replaced `java.awt.Point` across the entire codebase).
17. **Fragile "move first, rollback on collision" pattern in `Snake.move(char dir)`** – the head coordinate is mutated into an illegal tile before validating collisions, requiring manual inverse translations (`translate(0, 1)`, etc.) to rollback. Validating the target tile *before* moving eliminates rollback complexity and risk of coordinate misalignment. ✅ Fixed (validate-before-move pattern).
18. **Misleading boolean method name `borderControl()`** – named like a system or controller rather than a boolean predicate; should follow Java naming conventions such as `isOutOfBounds()` or `hasHitWall()`. ✅ Fixed (renamed to `isOutOfBounds(GridPoint)`).
19. **Ambiguous variable naming for grid bounds (`gx`, `gy` and constructor params `int x, int y`)** – `gx`/`gy` sound like coordinates (Grid X/Y) rather than board dimensions (`gameWidth`/`gameHeight`), creating confusion in the constructor where callers might assume `(x, y)` sets the snake's initial position rather than board size. ✅ Fixed (renamed parameters to `gameWidth` and `gameHeight`).
20. **Unnecessary `ImageView` wrapper instances for canvas rendering** – `Snake` creates and maintains four `ImageView` objects solely to rotate sprites before calling `.snapshot()`. Since `Canvas` natively renders raw `Image` objects with hardware‑accelerated canvas rotation (`GraphicsContext.rotate()`), wrapping images inside `ImageView` nodes is completely unnecessary overhead and can be removed entirely. ✅ Fixed (removed `ImageView` wrappers).
21. **Off‑by‑one coordinate bug in `Obstacle.newObstacles(...)`** – `p = new Point(rnd.nextInt(x) + 1, rnd.nextInt(y))` shifts X‑coordinates into the range `[1, x]`. This can spawn obstacles at column `x` (out‑of‑bounds) and prevents obstacles from ever spawning at column `0`. ✅ Fixed (deterministic free-cell sampling samples valid `[0, boardWidth)` range).
22. **Missing duplicate obstacle collision check in `Obstacle.newObstacles(...)`** – the `do...while` generation loop validates points against the snake and coin, but never checks `obstacles.contains(p)`. This allows duplicate identical obstacle points to be added to the list. ✅ Fixed (sampling validates against occupied list including existing obstacles).
23. **Broken encapsulation in `Obstacle.getObstacles()`** – returns a direct reference to the internal mutable `ArrayList<Point>`, allowing external callers to directly mutate or clear the collection without encapsulation control. ✅ Fixed (returns `Collections.unmodifiableList(obstacles)`).
24. **Arbitrary magic number cap (`obstacles.size() > 50`)** – hard‑codes an obstacle cap of 50 regardless of board dimensions, which fails to scale appropriately for custom grid sizes. ✅ Fixed (dynamic proportional cap: `boardWidth * boardHeight / 4`).
25. **Misleading dimension fields `x` and `y` in `Obstacle`** – represents board dimensions (`width` and `height`) using `x` and `y`, continuing the ambiguous coordinate vs. dimension naming smell found across the codebase. ✅ Fixed (renamed to `boardWidth` and `boardHeight`).
26. **Repeated disk I/O and image decoding on every coin creation** – `Coin` and all subclasses (`GoldCoin`, `SilverCoin`, `BlueCoin`, `RainbowCoin`) load PNG resources from disk via `new Image(getResourceAsStream(...))` on each instantiation. Eating coins causes repeated file reads and texture allocations instead of reusing cached or `static final` images. ✅ Fixed (static singleton `Image` instances).
27. **Redundant `new Random()` instantiation in `Coin` collision loop** – `new Random()` is invoked inside the `do...while` body on every collision retry iteration rather than instantiating the generator once outside the loop. ✅ Fixed (static reusable `Random`).
28. **Field shadowing and boilerplate in `Coin` subclasses** – each subclass re‑declares a private `Image image` field and shadows the superclass field while duplicating `getImage()`, rather than passing the image or path to `super` or using clean inheritance. ✅ Fixed (subclasses delegate to `super` constructor).
29. **Violation of Open‑Closed Principle (OCP) in coin power‑up handling** – `GameController.powerCoin()` uses an explicit `if‑else` chain comparing `coin.getClass().equals(...)` with downcasting instead of polymorphic delegation (e.g. `coin.applyEffect(...)`), requiring modifications to the controller whenever a new coin type is introduced. ✅ Fixed (polymorphic `applyEffect`).
30. **Redundant local variable assignments before returning** – methods such as `Coin.getPoints()` and `RainbowCoin.getSpeedBonus()` declare redundant intermediate local variables (e.g., `int points = 5; return points;`) instead of returning literals or named constants directly. ✅ Fixed (direct literal returns).
31. **Unvalidated `difficulty` parameter in `Game` constructor** – accepts raw `char` (`'E'`, `'N'`, `'H'`) without an `else` fallback; passing any invalid character leaves `speed` at `0.0`, breaking game animation. Should use a strongly‑typed `enum Difficulty`. ✅ Fixed (introduced strongly-typed `Game.Difficulty` enum with speeds and border settings).
32. **Ambiguous dual score tracking in `Game`** – maintains both `score` (eaten coin count used for victory calculation) and `scorePoints` (accumulated coin value for UI display) without clear naming distinctions. ✅ Fixed (renamed to `coinsEaten` and `displayScore`, with `getDisplayScore()`).
33. **Cryptic calculation in `Game.decMaxScore(int num)`** – subtracts `(num - 1)` from `maxScore` using obscure arithmetic that lacks documentation and risks underflow. ✅ Fixed (documented arithmetic rationale with Javadoc and added underflow protection guard).
34. **UI coupling in `Game` model** – `Game` directly instantiates and returns a JavaFX `Color` (`Color.web("feffe4")`), mixing view styling with core model state. ✅ Fixed (`Color` moved out of `Game` model into `GameView` as `BACKGROUND_COLOR`).
35. **"God‑method" coupling in `MainMenuController.launchSnake(...)`** – constructs and wires models, views, controllers, layout panes, scene graphs, keyboard listeners, and inline CSS directly inside a menu controller instead of delegating to a factory or game scene builder. ✅ Fixed (refactored into clean `buildGameScene` with proper static alignment calls and decoupled navigation callback).
36. **Hard‑coded game settings in `MainMenuController`** – static constants (`X_VALUE = 20`, `Y_VALUE = 20`, `DIFFICULTY = 'N'`) prevent menu UI selections from dynamically configuring game options. ✅ Fixed (uses `Game.Difficulty.EASY` enum instead of raw character).
37. **Multiple Stage anti‑pattern in JavaFX** – `ViewManager` and `MainMenuController` close existing stages and create new `Stage` instances (`new Stage()`) rather than reusing the primary `Stage` and swapping `Scene`s, leading to window focus issues and extra OS taskbar icons. ✅ Fixed (`ViewManager` receives and reuses `primaryStage`, swapping scenes seamlessly via `showGameScene` and `showMainMenu`).
38. **Unsafe FXML loading and immediate NPE risk in `ViewManager`** – catches `IOException` during FXML loading and only logs a stack trace, proceeding immediately to `new Scene(root)` where `root` is `null`, producing an unhandled `NullPointerException`. ✅ Fixed (catches `IOException`, displays error alert dialog, and returns early to prevent NPE).
39. **Typo in public method name in `ViewManager`** – `initalizeMainMenu()` misspells "initialize", introducing API inconsistency. ✅ Fixed (renamed to `initializeMainMenu()`).
40. **Flawed key validation logic in `GameController.valKey(KeyCode key)`** – when checking `KeyCode.DOWN`, it compares against `!this.key.equals(KeyCode.DOWN)` instead of `KeyCode.UP`, failing to properly prevent 180‑degree self‑reversals into the snake's neck. ✅ Fixed (corrected to check `!this.key.equals(KeyCode.UP)`).
41. **Unbounded `do...while` coordinate generation / infinite loop vulnerability** – both `Coin` and `Obstacle` generate coordinates via unconstrained random guess‑and‑retry `do...while` loops. As the snake and obstacles occupy more grid cells near endgame, the probability of selecting an empty cell drops drastically, causing execution spikes. If the grid becomes full, the loop cannot exit and permanently freezes the application. ✅ Fixed (implemented deterministic available-cell sampling in both `Coin` and `Obstacle`).
42. **Tail sprite direction inversion on border wrap** – `tailRotation` determined tail direction by checking `next.getY() == tail.getY() - 1 ? 0 : 180` and `next.getX() == tail.getX() - 1 ? 270 : 90`. When wrapping across the top border (moving up) or left border (moving left), the coordinate jump to the opposite border (`height - 1` or `width - 1`) caused the condition to fail, falling into the ternary `else` branch and inverting the tail direction by 180° on the last frame before crossing the border. ✅ Fixed (encapsulated toroidal wrapped delta calculation in `Snake.getTailDir()` and updated `GameView.drawBody`).

43. **Easy difficulty loses border wrapping after collecting a coin** – `GameController.powerCoin()` called `snake.ignoreBorders(false)` for every collected coin, overriding Easy mode's default wrapping. ✅ Fixed (restores `game.isNoBorder()` before applying each coin's effect, preserving permanent wrapping on Easy and temporary blue-coin wrapping on Normal/Hard).
44. **Obstacle collision skipped after border wrapping** — `Snake.attemptMove()` moved the head directly to the wrapped destination and skipped the obstacle check in its `else if` branch. This allowed the snake to pass through obstacles on the opposite edge in Easy mode or after collecting a blue coin. ✅ Fixed (resolve the wrapped target first, check it for obstacles, then move; fatal collisions leave the head at its previous position).


---

# Improvements Made

1. **Refactored `Coin` hierarchy and asset loading (Addresses Bugs #26, #28, #30)**:
   - Converted all coin sprite `Image` instances to `private static final` singletons loaded once at class initialization, eliminating repeated disk I/O and texture re‑allocations on every coin spawn.
   - Removed shadowed `image` fields and duplicate `getImage()` implementations in subclasses, cleanly inheriting them from `Coin`.
   - Eliminated redundant intermediate local variables before returning.
2. **Polymorphic power‑up architecture (Addresses Bug #29)**:
   - Added polymorphic `applyEffect(Game game, Snake snake, Obstacle obstacle)` and `getSpeedBonus()` methods to `Coin`.
   - Replaced brittle `if‑else` type‑checking in `GameController.powerCoin()` with a one‑line polymorphic call.
3. **Fixed unreachable `RainbowCoin` spawn bug (Addresses Bug #3)**:
   - Corrected `GameController.newCoin()` condition so `RainbowCoin` spawns on `ran == 2`.
4. **Optimized random coordinate generation (Addresses Bug #27)**:
   - Shared a single static `Random` generator for position generation.
5. **Fixed broken Maven Wrapper configuration**:
   - Corrected invalid Maven 3 release URL in `.mvn/wrapper/maven-wrapper.properties`.
6. **Removed JavaFX UI components from `Snake` model — MVC fix (Addresses Bugs #11, #20)**:
   - Deleted all `ImageView` fields and corresponding getters.
   - Added static `Image` singletons (`IMG_HEAD`, `IMG_BODY`, `IMG_TURN`, `IMG_TAIL`).
   - Updated `GameView` to render using `GraphicsContext` rotation.
7. **Merged redundant border flags into single `borderWrap` boolean (Addresses Bug #12)**:
   - Deleted `ignoreBorders` and kept unified `borderWrap`.
8. **Extracted shared `move()` helpers — eliminated 4‑way code duplication (Addresses Bug #13)**:
   - Added `attemptMove` and `wrapCoordinate` helpers.
9. **Replaced `ArrayList` body with `ArrayDeque` — O(1) tail removal (Addresses Bug #14)**:
   - Updated `ateCoin()` and movement logic accordingly.
10. **Cached `head.equals(coin.getPos())` result (Addresses Bug #15)**.
11. **Validate‑before‑move: eliminated fragile rollback translations (Addresses Bug #17)**.
12. **Renamed `borderControl()` → `isOutOfBounds(GridPoint)` (Addresses Bug #18)**.
13. **Renamed ambiguous constructor parameters (Addresses Bug #19)**.
14. **Removed duplicate `drawGrid` overload (Addresses Bug #1)**.
15. **Fixed double‑scaling bug in sprite rendering (Addresses Bug #9)**.
16. **Eliminated repeated `SnapshotParameters` allocation (Addresses Bug #7)**.
17. **Eliminated legacy `java.awt.Point` dependency (Addresses Bug #16)**:
   - Created custom `GridPoint` model class in `com.comp2042.model`.
   - Replaced all `java.awt.Point` usages throughout model, view, and controller packages.
18. **Decoupled `GameView` from `GameController` static state (Addresses Bugs #4, #10)**:
   - Changed `key` and `lastKey` in `GameController` from `public static` to `private` instance fields with getters.
   - Passed direction state as parameters to `GameView.turnDir(snake, lastKey, key)`.
19. **Removed dead code and unused variables (Addresses Bug #5)**:
   - Deleted `calculateBonusScore()`, `legacyScoreMultiplier`, and unused obstacle count generation.
20. **Replaced magic numbers with named constants (Addresses Bug #6)**:
   - Added `SMALL_BOARD_MULTIPLIER`, `LARGE_BOARD_MULTIPLIER`, and `LARGE_BOARD_THRESHOLD` in `Game`. Reused `Random` generator instance in `GameController`.
21. **Fixed turn array stale entries upon reset (Addresses Bug #8)**:
   - Added `resetTurnArray()` in `GameView` and invoked it in `GameController.reset()`.
22. **Fixed `Obstacle` coordinate generation, off-by-one, and encapsulation (Addresses Bugs #21, #22, #23, #24, #25)**:
   - Fixed off-by-one: coordinate generation now strictly samples within valid grid bounds `[0, boardWidth)`.
   - Prevented duplicate obstacle placement by verifying against existing obstacles in occupied collection.
   - Wrapped `getObstacles()` in `Collections.unmodifiableList(...)`.
   - Replaced magic obstacle cap with proportional board limit (`boardWidth * boardHeight / 4`).
   - Renamed dimension fields `x, y` to `boardWidth, boardHeight`.
23. **Strongly-typed difficulty configuration (Addresses Bug #31)**:
   - Replaced unvalidated `char` parameter with `Game.Difficulty` enum (`EASY`, `NORMAL`, `HARD`).
24. **Clarified score tracking terminology (Addresses Bug #32)**:
   - Renamed `score` to `coinsEaten` and `scorePoints` to `displayScore`, updating getters and UI references.
25. **Documented and guarded `decMaxScore` (Addresses Bug #33)**:
   - Added descriptive Javadoc explaining obstacle cell accounting, with underflow protection.
26. **Decoupled view styling from game model (Addresses Bug #34)**:
   - Removed JavaFX `Color` from `Game` model; background fill color is now encapsulated inside `GameView`.
27. **Single Stage lifecycle management (Addresses Bugs #37, #39)**:
   - Replaced the multiple-stage anti-pattern with single-stage scene swapping in `ViewManager`.
   - Corrected typo `initalizeMainMenu()` → `initializeMainMenu()`.
   - Wired escape key to clean return-to-menu callback without window recreation.
28. **Safe FXML loading (Addresses Bug #38)**:
   - Handled `IOException` in `ViewManager` with user-facing error dialog and early exit, preventing `NullPointerException`.
29. **Fixed anti-reverse keyboard validation (Addresses Bug #40)**:
   - Corrected downward key check in `GameController.valKey()` from `!key.equals(DOWN)` to `!key.equals(UP)`, properly preventing 180° neck collisions.
30. **Deterministic coordinate sampling / infinite loop elimination (Addresses Bug #41)**:
   - Replaced unbounded `do...while` retry loops in `Coin` and `Obstacle` with deterministic free-cell sampling, ensuring instant termination even on near-full boards.
31. **Toroidal wrapped tail direction & MVC encapsulation (Addresses Bug #42)**:
   - Added `Snake.getTailDir()` to compute the shortest toroidal displacement vector (`dx`, `dy`) between the tail and adjacent segment (`head` or next body segment), correctly accounting for border wrap across all 4 boundaries.
   - Refactored `GameView.drawBody` to consistently use `tailRotation(snek)` for all tail segments (`index == 0`) and delegated `tailRotation` to `Snake.getTailDir()`.
32. **Responsive 2-Key FIFO Input Queue and Difficulty Speed Tuning**:
   - Replaced the single-flag `keyPressed` boolean in `GameController` with a bounded 2-key FIFO queue (`Deque<KeyCode> inputQueue`).
   - Prevents rapid sequential keystrokes (such as quick 90° double-turns around obstacles) from being dropped when pressed within a single tick window.
   - Synchronized `turnDir()` invocation with logic tick execution, ensuring corner elbow sprites (`snaketurnleft.png`) are placed on the exact tile where the turn executed.
   - Added `isValidNextDirection(newKey, referenceKey)` validation relative to the last queued direction, preventing 180° self-reversals and redundant keys.
   - Tuned game speeds in `Game.Difficulty` (EASY: `7.0`, NORMAL: `9.0`, HARD: `12.0` moves/sec) for a balanced, responsive, and comfortable pace while maintaining 100% discrete pixel-art sprite integrity.
33. **Preserved Easy difficulty border wrapping after coin collection (Addresses Bug #43)**:
   - Replaced `snake.ignoreBorders(false)` in `GameController.powerCoin()` with `snake.ignoreBorders(game.isNoBorder())`.
   - Restores the difficulty's default border setting before `coin.applyEffect(...)`, so collecting any coin keeps wrapping enabled on Easy.
   - Normal/Hard keep solid borders by default; the blue coin enables wrapping until the next coin is collected. Obstacle and self-collision rules are unchanged.
34. **Checked obstacle collisions at wrapped destinations (Addresses Bug #44)**:
   - Updated `Snake.attemptMove()` to resolve an out-of-bounds target with `wrapCoordinate()` before checking for obstacles.
   - Both ordinary and wrapped moves now use the same obstacle check before updating the head; a blocked destination marks the snake dead without moving it.
   - Applies to all four borders in Easy mode and while the blue coin enables wrapping on Normal/Hard. Crossing an unwrapped border remains fatal.
