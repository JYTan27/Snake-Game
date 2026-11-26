package com.comp2042.controller;

import com.comp2042.model.GameMode;
import com.comp2042.model.stats.ModeStatistics;
import com.comp2042.model.stats.RunResult;
import com.comp2042.service.audio.SoundEffect;
import com.comp2042.service.audio.SoundService;
import com.comp2042.service.leaderboard.LeaderboardService;
import com.comp2042.view.GameModeSelector;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Displays the selected mode's rankings, run statistics, and any storage warning. */
public final class LeaderboardController {

    /** Maximum number of ranked results shown per mode. */
    private static final int DISPLAY_LIMIT = 10;
    /** Formats result dates in the user's local time zone. */
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    /** Shared source of rankings, statistics, and storage warnings. */
    private final LeaderboardService leaderboards;
    /** Sound effects for selection changes and the Back button. */
    private final SoundService sounds;
    /** Navigation action invoked by the Back button. */
    private final Runnable onBack;
    /** Mode selected when the leaderboard opens. */
    private final GameMode initialMode;
    /** FXML dropdown that filters the displayed results by mode. */
    @FXML private ComboBox<GameMode> modeSelector;
    /** FXML scroll pane reset when the mode changes. */
    @FXML private ScrollPane leaderboardScroll;
    /** FXML container replaced with the currently selected mode's content. */
    @FXML private VBox modeContent;
    /** FXML label for a storage warning, shown when the service reports one. */
    @FXML private Label storageWarning;

    /**
     * Creates the leaderboard controller before FXML injects its controls.
     *
     * @param leaderboards the shared source of rankings and statistics
     * @param sounds the service used for menu sound effects
     * @param initialMode the mode displayed when the screen opens
     * @param onBack the action that returns to the main menu
     * @throws NullPointerException if any argument is null
     */
    public LeaderboardController(LeaderboardService leaderboards, SoundService sounds, GameMode initialMode, Runnable onBack) {
        this.leaderboards = Objects.requireNonNull(leaderboards);
        this.sounds = Objects.requireNonNull(sounds);
        this.onBack = Objects.requireNonNull(onBack);
        this.initialMode = Objects.requireNonNull(initialMode);
    }

    /** Populates the selector and renders the initial mode after FXML injection. */
    @FXML
    private void initialize() {
        GameModeSelector.configure(modeSelector, initialMode, sounds);
        modeSelector.valueProperty().addListener((observable, previous, selected) -> {
            if (selected != null) renderMode(selected);
        });
        renderMode(initialMode);
    }

    /**
     * Refreshes the warning, summary, and top runs and resets the scroll position.
     *
     * @param mode the mode to display
     */
    private void renderMode(GameMode mode) {
        // An empty warning keeps the label out of the layout.
        String warning = leaderboards.getWarning().orElse("");
        storageWarning.setText(warning);
        storageWarning.setVisible(!warning.isEmpty());
        storageWarning.setManaged(!warning.isEmpty());
        // Pair the mode title with summary values computed from its full history.
        Label title = label(mode.getDisplayName(), "helpModeTitle");
        ModeStatistics stats = leaderboards.getStatistics(mode);
        FlowPane summary = new FlowPane(14, 6);
        summary.getChildren().addAll(
                label("Runs: " + stats.runs(), "leaderboardCell"),
                label("Wins: " + stats.wins(), "leaderboardCell"),
                label("Losses: " + stats.losses(), "leaderboardCell"),
                label("Abandoned: " + stats.abandoned(), "leaderboardCell"),
                label("Best score: " + stats.bestScore(), "leaderboardCell"),
                label(String.format(Locale.ROOT, "Average score: %.1f", stats.averageScore()), "leaderboardCell"),
                label("Total active time: " + formatTime(stats.totalActiveTimeMillis()), "leaderboardCell"));
        // Place the ranked subset below the mode's summary in the same section.
        VBox section = new VBox(9, title, summary);
        section.getStyleClass().add("leaderboardSection");
        List<RunResult> runs = leaderboards.getTopRuns(mode, DISPLAY_LIMIT);
        if (runs.isEmpty()) {
            section.getChildren().add(label("No runs yet. Start a game in this mode!", "leaderboardEmpty"));
        } else {
            section.getChildren().addAll(label("Top runs", "leaderboardCaption"), createTable(runs));
        }
        modeContent.getChildren().setAll(section);
        leaderboardScroll.setVvalue(0);
    }

    /**
     * Builds the results grid in leaderboard order.
     *
     * @param runs the results already ranked by the service
     * @return the grid containing one header row and one row per result
     */
    private GridPane createTable(List<RunResult> runs) {
        // Use percentage widths so the table columns resize together.
        GridPane table = new GridPane();
        table.setVgap(7);
        for (double width : new double[]{7, 12, 20, 18, 13, 30}) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(width);
            table.getColumnConstraints().add(column);
        }
        // Reserve the first row for headings and use one-based numbers for rank labels.
        String[] headings = {"#", "Score", "Outcome", "Time", "Length", "Date"};
        for (int column = 0; column < headings.length; column++) {
            table.add(label(headings[column], "leaderboardColumn"), column, 0);
        }
        for (int index = 0; index < runs.size(); index++) {
            RunResult run = runs.get(index);
            table.addRow(index + 1,
                    label(Integer.toString(index + 1), "leaderboardCell"),
                    label(Integer.toString(run.score()), "leaderboardCell"),
                    label(run.outcome().getDisplayName(), "leaderboardCell"),
                    label(formatTime(run.activeTimeMillis()), "leaderboardCell"),
                    label(Integer.toString(run.snakeLength()), "leaderboardCell"),
                    label(DATE.format(run.recordedAt()), "leaderboardCell")
            );
        }
        return table;
    }

    /**
     * Creates a wrapping label that can shrink to its column width.
     *
     * @param text the label's visible text
     * @param styleClass the CSS class to apply
     * @return the configured label
     */
    private Label label(String text, String styleClass) {
        // Allow cell text to wrap within the width assigned by the table or summary.
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    /**
     * Formats a play duration as hours, minutes, and seconds.
     *
     * @param milliseconds the elapsed time in milliseconds
     * @return the duration truncated to whole seconds
     */
    private String formatTime(long milliseconds) {
        // Drop fractional seconds before splitting the duration into hours, minutes, and seconds.
        long seconds = milliseconds / 1000;
        return String.format(Locale.ROOT, "%d:%02d:%02d", seconds / 3600, (seconds / 60) % 60, seconds % 60);
    }

    /** Plays the button effect and returns through the supplied navigation action. */
    @FXML
    private void handleBack() {
        sounds.play(SoundEffect.BUTTON);
        onBack.run();
    }
}
