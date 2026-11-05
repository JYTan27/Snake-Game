# Refactoring & Maintenance Documentation
**Module:** COMP2042 — Developing Maintainable Software  
**Project:** Snake Game (JavaFX)

---

## 1. Project Overview & Architectural Goal
The objective of this project is to take a legacy JavaFX Snake game codebase and refactor it into an industry-standard, maintainable, and extensible architecture following Object-Oriented Design principles (SOLID) and design patterns.

---

## 2. Identified Problems & Code Smells (Code Audit)

### A. Architectural & Structural Smells
* **Flat Package Structure (Previously):**
  * All 13 classes were dumped into a single root package (`com.comp2042`), violating the **Separation of Concerns (SoC)**.
* **Tangled MVC Responsibilities:**
  * **Domain Logic coupled to JavaFX UI:** Domain models ([`Snake.java`](file:///c:/Users/jytan/OneDrive/Documents/Devoloping%20Maintanable%20Software/Coursework/snake-game/src/main/java/com/comp2042/model/Snake.java), [`Coin.java`](file:///c:/Users/jytan/OneDrive/Documents/Devoloping%20Maintanable%20Software/Coursework/snake-game/src/main/java/com/comp2042/model/Coin.java), [`Obstacle.java`](file:///c:/Users/jytan/OneDrive/Documents/Devoloping%20Maintanable%20Software/Coursework/snake-game/src/main/java/com/comp2042/model/Obstacle.java)) imported and instantiated UI components like `javafx.scene.image.Image`, `ImageView`, and `Color`.
  * **Untestable Domain Logic:** Because models were coupled to JavaFX classes, writing headless unit tests (e.g. testing snake movement or collision with JUnit) triggers JavaFX `Toolkit not initialized` exceptions.
* **Static Tight Coupling:**
  * [`GameView`](file:///c:/Users/jytan/OneDrive/Documents/Devoloping%20Maintanable%20Software/Coursework/snake-game/src/main/java/com/comp2042/view/GameView.java) directly reads `public static` fields from controllers (`GameController.key`, `GameController.lastKey`, `MainMenuController.getxValue()`), creating hidden global dependencies.

### B. Logic & Implementation Smells
* **Bug in Coin Spawning ([`GameController.java`](file:///c:/Users/jytan/OneDrive/Documents/Devoloping%20Maintanable%20Software/Coursework/snake-game/src/main/java/com/comp2042/controller/GameController.java#L190-L194)):**
  ```java
  if (ran == 1) {
      coin = new GoldCoin(...);
  } else if (ran == 1) { // BUG: ran == 1 is repeated!
      coin = new RainbowCoin(...); // This branch is completely unreachable (dead code)
  }
  ```
  `ran == 1` is evaluated twice; `RainbowCoin` can never spawn through this path.
* **Violation of Open/Closed Principle (OCP) in Power-Ups:**
  * In `GameController.powerCoin()`, coin behavior is evaluated using explicit type checking:
    ```java
    if (coin.getClass().equals(RainbowCoin.class)) { ... }
    else if (coin.getClass().equals(SilverCoin.class)) { ... }
    else if (coin.getClass().equals(BlueCoin.class)) { ... }
    ```
    Adding a new coin requires modifying `GameController` instead of leveraging polymorphism.
* **Dead / Unused Code:**
  * `calculateBonusScore()` and `legacyScoreMultiplier` in `GameController` are never used during gameplay.
  * In `GameController.reset()`, `int unusedObstacleCount` is generated but never passed anywhere.
* **Resource Inefficiency:**
  * Entities instantiate `new Image(...)` on every construction rather than retrieving cached assets.
  * Paths were originally hardcoded strings (`"resources/views/res/..."`).

---

## 3. Improvements Implemented So Far

- [x] **Separation into MVC Layers:**
  - `com.comp2042.model`: Business logic and entities (`Game`, `Snake`, `Obstacle`, `Coin`, `BlueCoin`, `GoldCoin`, `RainbowCoin`, `SilverCoin`).
  - `com.comp2042.view`: Display and canvas drawing (`GameView`, `ViewManager`).
  - `com.comp2042.controller`: Input management and game loop (`GameController`, `MainMenuController`).
- [x] **Maven Resource Directory Normalization:**
  - Removed duplicate `src/main/resources/resources/views/res` nesting.
  - Organized assets into standard Maven namespaces:
    - `/com/comp2042/view/` (FXML & CSS)
    - `/com/comp2042/images/` (subfolders for `coins/`, `snake/`, `ui/`)
    - `/com/comp2042/audio/`
    - `/com/comp2042/fonts/`
- [x] **Classpath Resource Loading:**
  - Replaced brittle relative file paths with `getClass().getResourceAsStream(...)` and `getResource(...).toExternalForm()` to guarantee packaging compatibility into a JAR.

---

## 4. Planned Future Improvements & Design Patterns

### Priority 1: Bug Fixes & Code Cleanup
- [ ] Fix the duplicate `ran == 1` bug in `GameController.newCoin()` so `RainbowCoin` spawns properly.
- [ ] Remove unused variables (`unusedObstacleCount`, `legacyScoreMultiplier`, `calculateBonusScore`).
- [ ] Replace public static fields (`key`, `lastKey`) with proper instance encapsulation or event passing.

### Priority 2: Design Pattern Refactoring
- [ ] **Strategy Pattern / Polymorphic Power-Ups:**
  - Give `Coin` an `applyEffect(Snake snake, Game game, Obstacle obstacle)` method.
  - Each coin subclass overrides this method, removing the chain of `getClass().equals(...)` from `GameController`.
- [ ] **Observer Pattern (Loose Coupling for Events):**
  - Implement an event/listener system so `Game` and `Snake` fire events (`onScoreChanged`, `onSnakeDied`, `onCoinCollected`) that `GameView` and audio players subscribe to, removing circular references.
- [ ] **Factory Pattern (Coin Spawner):**
  - Extract coin creation into a dedicated `CoinFactory` to clean up `GameController`.
- [ ] **Asset Manager / Cache (Flyweight Pattern):**
  - Centralize image and audio loading so assets are only loaded into memory once and reused across instances.

### Priority 3: Testing & Quality Assurance
- [ ] Decouple `Snake` and `Coin` coordinates from JavaFX UI elements to allow pure JUnit 5 test cases for:
  - Snake movement in all 4 directions.
  - Collision detection with boundaries, self-body, and obstacles.
  - Score calculations and win/loss states.

---

## 5. Notes & Observations Log

*Use this section as you work through the project to log notes for your coursework report:*

| Date | Component | Problem / Task | Solution Implemented |
| :--- | :--- | :--- | :--- |
| *(Auto)* | File Structure | Flat package smell | Reorganized into MVC (`model`, `view`, `controller`) |
| *(Auto)* | Resources | Nested `resources/views/res` path | Flattened to `/com/comp2042/{images,audio,fonts,view}` |
| *(Pending)* | `GameController` | Duplicate `ran == 1` condition | Allocate unique random thresholds for each coin |
| *(Pending)* | `Coin` hierarchy | OCP violation in `powerCoin()` | Implement Strategy Pattern via `coin.onCollect(...)` |

