# Bugs / Unoptimised Sections

[Back to README](README.md)

The numbered entries follow the project's maintenance history. Earlier entries use the original coin and difficulty names where applicable.

1. **Duplicate `drawGrid` methods** — Consolidated drawing into one method that handles boards with or without obstacles. ✅ Fixed.
2. **Per-segment snapshots** — Replaced snapshots with canvas rotation to avoid creating an image for each snake segment. ✅ Fixed.
3. **Unreachable `RainbowCoin` branch** — Corrected the duplicate condition in the old spawner. The current weighted selector can select the equivalent bolt in Power-ups. ✅ Fixed.
4. **View depends on controller state** — Pass direction information to `GameView` when needed so it can render turns without reading controller fields. ✅ Fixed.
5. **Unused reset code** — Removed unused scoring methods and fields from `GameController`. ✅ Fixed.
6. **Unclear scaling values** — Named the board-size thresholds and scale constants to clarify their purpose. ✅ Fixed.
7. **Repeated snapshot parameters** — Removed the per-draw allocation when sprite snapshots were replaced with canvas drawing. ✅ Fixed.
8. **Stale turn history** — Clear stored turns on restart so the old run cannot affect the new snake's sprites. ✅ Fixed.
9. **Sprites scaled twice** — Use one cell size for sprite positions and dimensions. ✅ Fixed.
10. **Global direction fields** — Made the current and previous directions private to each game controller. ✅ Fixed.
11. **Scene-graph nodes in the snake model** — Removed `ImageView` objects from the model and left sprite drawing to the view. ✅ Fixed.
12. **Duplicate border flags** — Use one snake wrapping flag, with the current mode supplying the default. ✅ Fixed.
13. **Repeated movement code** — Shared collision and wrapping logic across the four directions. ✅ Fixed.
14. **Expensive tail removal** — Replaced the body list with a deque to remove the tail without shifting the rest of the body. ✅ Fixed.
15. **Repeated pickup collision check** — Calculate whether the snake reached a pickup once per move. ✅ Fixed.
16. **AWT dependency in board coordinates** — Replaced `java.awt.Point` with `GridPoint`. ✅ Fixed.
17. **Invalid movement needed rollback** — Check the destination before committing a move. ✅ Fixed.
18. **Unclear border check name** — Renamed `borderControl` to `isOutOfBounds`. ✅ Fixed.
19. **Ambiguous snake dimensions** — Renamed constructor parameters and fields to distinguish dimensions from positions. ✅ Fixed.
20. **Unnecessary `ImageView` wrappers** — Draw sprites directly on the canvas. ✅ Fixed.
21. **Obstacle off-by-one error** — Include column zero and restrict placement to in-bounds cells. ✅ Fixed.
22. **Duplicate obstacle positions** — Exclude existing obstacles when selecting a new obstacle cell. ✅ Fixed.
23. **Obstacle list exposed for changes** — Return an unmodifiable list so placement remains under `Obstacle`'s control. ✅ Fixed.
24. **Hard-coded obstacle cap** — Base the maximum obstacle count on board area. ✅ Fixed.
25. **Ambiguous obstacle dimensions** — Renamed the width and height fields to reflect their role. ✅ Fixed.
26. **Repeated coin image loading** — Cache pickup sprites and reuse them across instances. ✅ Fixed.
27. **Repeated random generator creation** — Reuse a random source for placement. ✅ Fixed.
28. **Duplicated image fields in coin subclasses** — Use the shared pickup constructor to store each sprite. ✅ Fixed.
29. **Type checks for applying effects** — Give each pickup its own effect method so the controller can use a common interface. ✅ Fixed.
30. **Redundant temporary variables** — Return constant score and speed values directly. ✅ Fixed.
31. **Invalid difficulty values** — Replaced raw characters with an enum. The current implementation uses `GameMode`. ✅ Fixed.
32. **Ambiguous score tracking** — Separated the display score from the old collection counter. Completion now uses board occupancy. ✅ Fixed.
33. **Unclear numeric target adjustment** — Clarified and guarded the original calculation before the full-board win rule removed that target. ✅ Fixed.
34. **Board colours in the model** — Moved visual styling to the view layer. ✅ Fixed.
35. **Overloaded menu handler** — Moved game scene construction into a dedicated method to make the start action easier to follow. ✅ Fixed.
36. **Hard-coded difficulty in the menu** — Read the user's mode selection when creating a game. ✅ Fixed.
37. **Multiple application windows** — Reuse the primary stage to avoid focus problems and extra taskbar icons. ✅ Fixed.
38. **Unsafe FXML failure handling** — Show an error and stop loading the screen if FXML cannot be read. ✅ Fixed.
39. **Misspelled menu initialisation method** — Renamed the method to `initializeMainMenu`. ✅ Fixed.
40. **Incorrect reverse-direction validation** — Block every 180-degree turn, including a downward turn while moving up. ✅ Fixed.
41. **Placement could loop indefinitely** — Choose from available cells so pickup and obstacle placement can finish on a crowded board. ✅ Fixed.
42. **Tail flipped at wrapped borders** — Calculate the tail orientation across the wrap so it faces the correct direction. ✅ Fixed.
43. **Pickup collection disabled default wrapping** — Restore the mode's border rule before applying a pickup effect. Wraparound stays enabled. ✅ Fixed.
44. **Obstacles skipped at wrapped destinations** — Check the destination on the opposite edge before moving. ✅ Fixed.
45. **Redundant wrapping pickup in Easy difficulty** — Restricted wrapping pickups under the original difficulty rules. In the current modes, rings appear only in Power-ups. ✅ Fixed.
46. **Plus shortcut failed to enter fullscreen** — Replaced the size shortcuts with **F** to toggle fullscreen. ✅ Fixed.
47. **Head overlapped the neck after a blocked fatal move** — Advance body segments only after a valid move, preserving the last valid pose. ✅ Fixed.
48. **Invalid pickup and incomplete win on a full board** — Report that no cell is available, stop the run, and show the final score and win message. ✅ Fixed.
49. **Fullscreen dropdown grew after selection** — Keep the control height fixed while scaling the popup items. ✅ Fixed.
50. **Fullscreen was disrupted when switching screens** — Keep the same scene and replace its contents so fullscreen survives navigation. ✅ Fixed.
51. **Short clips could be delayed on Windows** — Keep audio output initialised between effects and use ordinary playback if initialisation fails. ✅ Fixed.
52. **Blocks could spawn after obstacle placement reached its limit** — Placement and spawning now use the same 5% obstacle cap. Once it is reached, selected blocks become diamonds. ✅ Fixed.

# Improvements Made

1. **Refactored the coin hierarchy and asset loading (Addresses #26, #28, #30)**
   - Shared sprite handling across subclasses and cached assets to reduce repeated loading.

2. **Delegated power-up effects to pickups (Addresses #29)**
   - Each pickup defines its own effect. Collection uses the shared pickup interface.

3. **Fixed unreachable `RainbowCoin` spawning (Addresses #3)**
   - Corrected the original random branch before weighted pickup selection replaced it.

4. **Reused the random generator (Addresses #27)**
   - Removed repeated generator creation during spawn attempts.

5. **Repaired the Maven Wrapper configuration**
   - Updated its distribution URL to Maven 3.9.6.

6. **Removed scene-graph nodes from `Snake` (Addresses #11, #20)**
   - The view draws the snake using sprites shared by the model.

7. **Merged border flags (Addresses #12)**
   - A single wrapping flag represents the snake's current border behaviour.

8. **Shared movement helpers (Addresses #13)**
   - All directions use the same destination and collision checks.

9. **Changed the body to a deque (Addresses #14)**
   - Removing the tail no longer shifts the other segments.

10. **Reused the pickup collision result (Addresses #15)**
   - Store one comparison result per move.

11. **Validated movement before updating the snake (Addresses #17)**
   - Prevent illegal positions from being committed and remove rollback code.

12. **Clarified the border predicate (Addresses #18)**
   - Renamed the check to `isOutOfBounds`.

13. **Clarified constructor parameters (Addresses #19)**
   - Use descriptive names for board dimensions.

14. **Consolidated grid drawing (Addresses #1)**
   - Use one drawing method for all boards.

15. **Corrected sprite scaling (Addresses #9)**
   - Use a shared cell size for positioning and sizing.

16. **Removed snapshot allocations (Addresses #2, #7)**
   - Rotate and draw sprites on the canvas.

17. **Replaced AWT coordinates (Addresses #16)**
   - Use `GridPoint` across the game.

18. **Decoupled the view from global direction state (Addresses #4, #10)**
   - Store direction per controller and pass it to the view for turn rendering.

19. **Removed dead code (Addresses #5)**
   - Deleted unused reset and scoring logic.

20. **Named scaling constants (Addresses #6)**
   - Clarified what dimensions and thresholds control.

21. **Cleared turn history on restart (Addresses #8)**
   - Start each run with fresh sprite orientation data.

22. **Corrected obstacle placement and storage (Addresses #21–#25)**
   - Choose distinct cells within the board, scale the cap with board area, and restrict changes to the obstacle model.

23. **Introduced typed difficulty configuration (Addresses #31)**
   - Replaced raw difficulty characters with an enum. This later became `GameMode`.

24. **Clarified score tracking (Addresses #32)**
   - Distinguished display points from the original collection count.

25. **Clarified and guarded `decMaxScore` (Addresses #33)**
   - Documented the original target adjustment and guarded its range. The numeric target was later removed.

26. **Moved styling out of the model (Addresses #34)**
   - The view controls the board's appearance.

27. **Reused the primary stage (Addresses #37, #39)**
   - Removed secondary windows and corrected the menu initialisation name.

28. **Handled FXML loading errors (Addresses #38)**
   - Show a user-facing error and stop loading the failed screen.

29. **Fixed reverse turns (Addresses #40)**
   - Apply the opposite-direction rule consistently.

30. **Bounded pickup and obstacle placement (Addresses #41)**
   - Choose among free cells, including on near-full boards.

31. **Corrected wrapped tail orientation (Addresses #42)**
   - Use wrapped distance when determining the direction of the tail.

32. **Added buffered input and tuned movement speed**
   - Apply up to two queued turns in sequence. Speed is now defined by the selected mode.

33. **Preserved default wrapping through collection (Addresses #43)**
   - Restore default borders before applying a pickup's temporary effect.

34. **Checked obstacles after wrapping (Addresses #44)**
   - Validate the destination on the opposite edge.

35. **Extracted capability-aware coin selection (Addresses #45)**
   - Moved the original wrapping-pickup restriction to the spawner. Current mode weights control availability.

36. **Added the fullscreen toggle (Addresses #46)**
   - Use **F** to enter or exit fullscreen.

37. **Preserved the last valid collision pose (Addresses #47)**
   - Leave the head and body in their valid positions when a wall or obstacle blocks movement.

38. **Handled exhausted placement and victory (Addresses #48)**
   - Report when placement is exhausted and finish the run with the final score and win message.

39. **Added menu difficulty selection**
   - Allowed configuration before starting a run. The menu now offers five game modes.

40. **Added How to Play**
   - Summarises the current modes and keyboard controls.

41. **Improved menu presentation**
   - Centred the title and controls and clarified button and selection states.

42. **Enabled fullscreen across screens**
   - Apply proportional scaling to menus and gameplay and use **Esc** for in-app navigation.

43. **Stabilised dropdown height (Addresses #49)**
   - Keep the selection control the same size in fullscreen.

44. **Added Exit**
   - Close the application from the main menu.

45. **Configured allowed coins per difficulty**
   - Defined pickup availability in the difficulty configuration. Mode-specific weights now supply those rules.

46. **Added distinct pickup sprites**
   - Give each pickup a recognisable appearance and centre it within its cell.

47. **Renamed the pickup model**
   - Replaced legacy coin names with names matching the sprites and their effects.

48. **Added modular checkerboard rendering**
   - Separated the drawing pattern from the colour palette so colours can change independently.

49. **Removed unused drawing parameters**
   - Simplified the grid drawing method and its callers.

50. **Switched movement to WASD**
   - Use **WASD** for starting, moving, or resuming. Arrow keys remain available for menu navigation.

51. **Made How to Play scrollable**
   - Keep the heading and Back button visible as the rules and controls scroll.

52. **Centralised pickup collection rules**
   - Apply points, speed, default borders, and pickup effects in `Game`.

53. **Reduced repeated body copying**
   - Reuse one body list per redraw and read tail orientation directly from the snake.

54. **Simplified end-message visibility**
   - Show win and loss messages directly from the model state.

55. **Simplified full-board win and restart logic**
   - Win when no pickup cell remains. Restart clears points, obstacles, effects, and completion.

56. **Replaced difficulty with five game modes**
   - Classic, Obstacles, Hyperspeed, Power-ups, and Wraparound have their own speed, border rules, and pickup availability.

57. **Added shared sound effects**
   - Cache clips and map menu, pickup, win, and loss events in `SoundEffect`.

58. **Added weighted pickup selection**
   - Use mode-specific weights. In Obstacles, blocks can recur and become diamonds at the cap.

59. **Removed duplicate rule checks**
   - Use `GameMode` for speed, default borders, and pickup availability. Remove the separate power-up toggle.

60. **Added pause, resume, and restart sounds**
   - Play cues for a requested pause, resuming from that pause, or restarting a run.

61. **Reduced Windows audio delay (Addresses #51)**
   - Keep the output line open between effects and release it on shutdown.

62. **Added persistent per-mode leaderboards**
   - Record score, outcome, active time, length, and date in local XML. Include wins, losses, and abandoned runs in the rankings and full-history summaries.

63. **Added a fixed leaderboard mode selector**
   - Switch the visible rankings and summary independently of the main-menu choice.

64. **Simplified leaderboard saving**
   - Save when each run is recorded. Preserve unreadable history and show storage warnings if an issue occurs.

65. **Preserved fullscreen across navigation (Addresses #50)**
   - Reuse the scene while switching its content and keep the board scaled to the current window.

66. **Organised pickups and resources**
   - Moved pickup classes into `model.pickup` and grouped their sprites in `images/pickups`. Removed unused legacy coin and audio assets.

67. **Added rotating menu splash messages**
   - Show the next message each time the main menu opens during a session.

68. **Added saved board colour presets**
   - Preview and apply one of six checkerboard palettes. The selection is restored at the next launch.

69. **Added menu background music**
   - Loop three tracks across menu screens, pause during play, and resume at the same position on return.

70. **Documented fields and methods**
   - Added descriptions of responsibilities across game, persistence, audio, and view classes.

71. **Updated the restart clip**
   - Replaced the audio played on **Tab** and removed the unused alternate file.

72. **Shared the obstacle cap (Addresses #52)**
   - Calculate the placement limit once in `Obstacle` and use it for both adding barriers and checking block eligibility.
