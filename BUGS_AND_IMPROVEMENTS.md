# Bugs / Unoptimised Sections

1. **Duplicate `drawGrid` methods** – there are two `drawGrid` implementations (one handling obstacles, one without) causing dead code and maintenance overhead.
2. **`drawBody` snapshot per segment** – `ImageView.snapshot` is called for every snake body segment each tick, creating many temporary `Image` objects and leading to GC spikes and performance degradation.
3. **Duplicate `if (ran == 1)` in `GameController.newCoin`** – logical error prevents the `RainbowCoin` branch from ever executing.
4. **Static coupling of `GameView` to `GameController.key`** – `GameView` reads controller static fields directly, violating MVC separation and making testing harder.
5. **Dead code in `GameController.reset()`** – methods/variables like `calculateBonusScore()`, `legacyScoreMultiplier`, `unusedObstacleCount` are never used.
6. **Misleading `multiplier` naming & magic numbers** – hard‑coded values (10, 20, 50) for board scaling are opaque and should be replaced with named constants.
7. **Repeated creation of `SnapshotParameters` set to `Color.TRANSPARENT`** – this object is recreated for every draw call; could be cached for efficiency.
8. **Potential stale entries in `turnArray`** – cleanup only occurs when the tail passes a turn; if the snake dies mid‑turn, stale entries may remain.
9. **Double scaling bug in `drawBody`** – image size calculated as `game.getMultiplier() * fullScale * fullScale`, resulting in stretched sprites; should use a single `fullScale` factor.
10. **Public static `key` / `lastKey` variables** – expose global state, making unit testing difficult and increasing coupling.
11. **MVC architecture violation in `Snake` (UI components in Model)** – `Snake` loads JavaFX `Image` and manages `ImageView` instances directly inside the model, breaking MVC boundaries and preventing headless unit testing of pure snake game logic.
12. **Redundant state flags (`ignoreBorders` vs `noBorder`) in `Snake`** – two separate boolean flags track the same border pass-through behaviour, creating redundant conditions like `(!ignoreBorders && !noBorder)` across all movement checks.
13. **Code duplication in `Snake.move(char dir)`** – border collision checks, obstacle collision checks, coordinate rollback on death, and edge wrapping are copy-pasted across all four direction cases (`U`, `D`, `L`, `R`).
14. **Inefficient `body.remove(0)` in `Snake.ateCoin()`** – `body` is stored in an `ArrayList`, so removing index 0 on every non-eating tick causes an $O(N)$ array shift; a `Deque` or `LinkedList` provides $O(1)$ operations.
15. **Redundant equality checks in `Snake.ateCoin()`** – `head.equals(coin.getPos())` is evaluated twice per invocation instead of caching the result in a local variable.
16. **Coupling to legacy AWT (`java.awt.Point`)** – uses `java.awt.Point` inside a JavaFX project, mixing legacy desktop UI dependencies with JavaFX.
17. **Fragile "move first, rollback on collision" pattern in `Snake.move(char dir)`** – the head coordinate is mutated into an illegal tile before validating collisions, requiring manual inverse translations (`translate(0, 1)`, etc.) to rollback. Validating the target tile *before* moving eliminates rollback complexity and risk of coordinate misalignment.
18. **Misleading boolean method name `borderControl()`** – named like a system or controller rather than a boolean predicate; should follow Java naming conventions such as `isOutOfBounds()` or `hasHitWall()`.
19. **Ambiguous variable naming for grid bounds (`gx`, `gy` and constructor params `int x, int y`)** – `gx`/`gy` sound like coordinates (Grid X/Y) rather than board dimensions (`gridWidth`/`gridHeight`), creating confusion in the constructor where callers might assume `(x, y)` sets the snake's initial position rather than board size.

20. **Unnecessary `ImageView` wrapper instances for canvas rendering** – `Snake` creates and maintains four `ImageView` objects solely to rotate sprites before calling `.snapshot()`. Since `Canvas` natively renders raw `Image` objects with hardware-accelerated canvas rotation (`GraphicsContext.rotate()`), wrapping images inside `ImageView` nodes is completely unnecessary overhead and can be removed entirely.
21. **Off-by-one coordinate bug in `Obstacle.newObstacles(...)`** – `p = new Point(rnd.nextInt(x) + 1, rnd.nextInt(y))` shifts X-coordinates into the range `[1, x]`. This can spawn obstacles at column `x` (out-of-bounds outside the right edge) and prevents obstacles from ever spawning at column `0` (the leftmost edge).
22. **Missing duplicate obstacle collision check in `Obstacle.newObstacles(...)`** – the `do...while` generation loop validates points against the snake and coin, but never checks `obstacles.contains(p)`. This allows duplicate identical obstacle points to be added to the list.
23. **Broken encapsulation in `Obstacle.getObstacles()`** – returns a direct reference to the internal mutable `ArrayList<Point>`, allowing external callers to directly mutate or clear the collection without encapsulation control.
24. **Arbitrary magic number cap (`obstacles.size() > 50`)** – hard-codes an obstacle cap of 50 regardless of board dimensions, which fails to scale appropriately for custom grid sizes.
25. **Misleading dimension fields `x` and `y` in `Obstacle`** – represents board dimensions (`width` and `height`) using `x` and `y`, continuing the ambiguous coordinate vs. dimension naming smell found across the codebase.
26. **Repeated disk I/O and image decoding on every coin creation** – `Coin` and all subclasses (`GoldCoin`, `SilverCoin`, `BlueCoin`, `RainbowCoin`) load PNG resources from disk via `new Image(getResourceAsStream(...))` on each instantiation. Eating coins causes repeated file reads and texture allocations instead of reusing cached or `static final` images.
27. **Redundant `new Random()` instantiation in `Coin` collision loop** – `new Random()` is invoked inside the `do...while` body on every collision retry iteration rather than instantiating the generator once outside the loop.
28. **Field shadowing and boilerplate in `Coin` subclasses** – each subclass re-declares a private `Image image` field and shadows the superclass field while duplicating `getImage()`, rather than passing the image or path to `super` or using clean inheritance.
29. **Violation of Open-Closed Principle (OCP) in coin power-up handling** – `GameController.powerCoin()` uses an explicit `if-else` chain comparing `coin.getClass().equals(...)` with downcasting instead of polymorphic delegation (e.g. `coin.applyEffect(...)`), requiring modifications to the controller whenever a new coin type is introduced.
30. **Redundant local variable assignments before returning** – methods such as `Coin.getPoints()` and `RainbowCoin.getSpeedBonus()` declare redundant intermediate local variables (e.g., `int points = 5; return points;`) instead of returning literals or named constants directly.
31. **Unvalidated `difficulty` parameter in `Game` constructor** – accepts raw `char` (`'E'`, `'N'`, `'H'`) without an `else` fallback; passing any invalid character leaves `speed` at `0.0`, breaking game animation. Should use a strongly-typed `enum Difficulty`.
32. **Ambiguous dual score tracking in `Game`** – maintains both `score` (eaten coin count used for victory calculation) and `scorePoints` (accumulated coin value for UI display) without clear naming distinctions.
33. **Cryptic calculation in `Game.decMaxScore(int num)`** – subtracts `(num - 1)` from `maxScore` using obscure arithmetic that lacks documentation and risks underflow.
34. **UI coupling in `Game` model** – `Game` directly instantiates and returns a JavaFX `Color` (`Color.web("feffe4")`), mixing view styling with core model state.
35. **"God-method" coupling in `MainMenuController.launchSnake(...)`** – constructs and wires models, views, controllers, layout panes, scene graphs, keyboard listeners, and inline CSS directly inside a menu controller instead of delegating to a factory or game scene builder.
36. **Hard-coded game settings in `MainMenuController`** – static constants (`X_VALUE = 20`, `Y_VALUE = 20`, `DIFFICULTY = 'N'`) prevent menu UI selections from dynamically configuring game options.
37. **Multiple Stage anti-pattern in JavaFX** – `ViewManager` and `MainMenuController` close existing stages and create new `Stage` instances (`new Stage()`) rather than reusing the primary `Stage` and swapping `Scene`s, leading to window focus issues and extra OS taskbar icons.
38. **Unsafe FXML loading and immediate NPE risk in `ViewManager`** – catches `IOException` during FXML loading and only logs a stack trace, proceeding immediately to `new Scene(root)` where `root` is `null`, producing an unhandled `NullPointerException`.
39. **Typo in public method name in `ViewManager`** – `initalizeMainMenu()` misspells "initialize", introducing API inconsistency.
40. **Flawed key validation logic in `GameController.valKey(KeyCode key)`** – when checking `KeyCode.DOWN`, it compares against `!this.key.equals(KeyCode.DOWN)` instead of `KeyCode.UP`, failing to properly prevent 180-degree self-reversals into the snake's neck.

# Improvements Made

1. **Refactored `Coin` hierarchy and asset loading (Addresses Bugs #26, #28, #30):**
   - Converted all coin sprite `Image` instances to `private static final` singletons loaded once at class initialization, eliminating repeated disk I/O and texture re-allocations on every coin spawn.
   - Removed shadowed `image` fields and duplicate `getImage()` implementations in `GoldCoin`, `SilverCoin`, `BlueCoin`, and `RainbowCoin`, cleanly inheriting them from `Coin`.
   - Eliminated redundant intermediate local variables before returning (e.g. `int points = 5; return points;`).
2. **Polymorphic power-up architecture (Addresses Bug #29 - Open-Closed Principle):**
   - Added polymorphic `applyEffect(Game game, Snake snake, Obstacle obstacle)` and `getSpeedBonus()` methods to `Coin`.
   - Replaced the brittle `if (coin.getClass().equals(...))` type-checking and downcasting chain in `GameController.powerCoin()` with a clean, polymorphic one-line call: `coin.applyEffect(game, snek, obstacle)`.
3. **Fixed unreachable `RainbowCoin` spawn bug (Addresses Bug #3):**
   - Corrected `GameController.newCoin()` condition so `RainbowCoin` correctly spawns on `ran == 2` instead of the unreachable duplicate `ran == 1`.
4. **Optimized random coordinate generation (Addresses Bug #27):**
   - Shared a single static `Random` generator for position generation rather than allocating `new Random()` on every collision retry in the `do...while` loop.
5. **Fixed broken Maven Wrapper configuration:**
   - Corrected invalid Maven 3 release URL in `.mvn/wrapper/maven-wrapper.properties` to official Maven 3.9.6 binary.

