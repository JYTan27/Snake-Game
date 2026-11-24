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

/** Renders the selected mode's statistics; ranking and storage belong to the service. */
public final class LeaderboardController {
    private static final int DISPLAY_LIMIT = 10;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    private final LeaderboardService leaderboards;
    private final SoundService sounds;
    private final Runnable onBack;
    private final GameMode initialMode;
    @FXML private ComboBox<GameMode> modeSelector;
    @FXML private ScrollPane leaderboardScroll;
    @FXML private VBox modeContent;
    @FXML private Label storageWarning;

    public LeaderboardController(LeaderboardService leaderboards, SoundService sounds, GameMode initialMode, Runnable onBack) {
        this.leaderboards = Objects.requireNonNull(leaderboards);
        this.sounds = Objects.requireNonNull(sounds);
        this.onBack = Objects.requireNonNull(onBack);
        this.initialMode = Objects.requireNonNull(initialMode);
    }

    @FXML
    private void initialize() {
        GameModeSelector.configure(modeSelector, initialMode, sounds);
        modeSelector.valueProperty().addListener((observable, previous, selected) -> {
            if (selected != null) renderMode(selected);
        });
        renderMode(initialMode);
    }

    private void renderMode(GameMode mode) {
        String warning = leaderboards.getWarning().orElse("");
        storageWarning.setText(warning);
        storageWarning.setVisible(!warning.isEmpty());
        storageWarning.setManaged(!warning.isEmpty());
        Label title = label(mode.getDisplayName(), "helpModeTitle");
        ModeStatistics stats = leaderboards.getStatistics(mode);
        FlowPane summary = new FlowPane(14, 6);
        summary.getChildren().addAll(
                label("Runs: " + stats.runs(), "leaderboardCell"),
                label("Wins: " + stats.wins(), "leaderboardCell"),
                label("Losses: " + stats.losses(), "leaderboardCell"),
                label("Abandoned: " + stats.abandoned(), "leaderboardCell"),
                label("Best: " + stats.bestScore(), "leaderboardCell"),
                label(String.format(Locale.ROOT, "Average: %.1f", stats.averageScore()), "leaderboardCell"),
                label("Active time: " + formatTime(stats.totalActiveTimeMillis()), "leaderboardCell"));
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

    private GridPane createTable(List<RunResult> runs) {
        GridPane table = new GridPane();
        table.setVgap(7);
        for (double width : new double[]{7, 12, 20, 18, 13, 30}) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(width);
            table.getColumnConstraints().add(column);
        }
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

    private Label label(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        return String.format(Locale.ROOT, "%d:%02d:%02d", seconds / 3600, (seconds / 60) % 60, seconds % 60);
    }

    @FXML
    private void handleBack() {
        sounds.play(SoundEffect.BUTTON);
        onBack.run();
    }
}
