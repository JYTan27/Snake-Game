package com.comp2042.view;

import com.comp2042.model.GameMode;
import com.comp2042.service.audio.SoundEffect;
import com.comp2042.service.audio.SoundService;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.Objects;

/** Shared display names, selection audio, and fullscreen popup scaling for mode dropdowns. */
public final class GameModeSelector {
    private GameModeSelector() {}

    /** Configure once after FXML injection. Restoring the initial selection is silent. */
    public static void configure(ComboBox<GameMode> selector, GameMode initialMode, SoundService sounds) {
        Objects.requireNonNull(selector);
        Objects.requireNonNull(initialMode);
        Objects.requireNonNull(sounds);

        DoubleProperty optionScale = new SimpleDoubleProperty(1);

        selector.setEditable(false);
        selector.getItems().setAll(GameMode.values());
        selector.setConverter(new StringConverter<>() {
            @Override public String toString(GameMode mode) {
                return mode == null ? "" : mode.getDisplayName();
            }

            @Override public GameMode fromString(String text) {
                return Arrays.stream(GameMode.values())
                        .filter(mode -> mode.getDisplayName().equalsIgnoreCase(text))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unknown game mode: " + text));
            }
        });

        selector.setValue(initialMode);
        selector.valueProperty().addListener((observable, previous, selected) -> {
            if (selected != null) sounds.play(SoundEffect.BUTTON);
        });

        selector.setCellFactory(list -> {
            ListCell<GameMode> cell = new ListCell<>() {
                @Override
                protected void updateItem(GameMode mode, boolean empty) {
                    super.updateItem(mode, empty);
                    setText(empty || mode == null ? null : mode.getDisplayName());
                }
            };
            cell.styleProperty().bind(Bindings.concat("-fx-font-size: ", optionScale.multiply(18),
                    "px; -fx-padding: ", optionScale.multiply(5), " ", optionScale.multiply(10), ";"));
            return cell;
        });

        // A popup is a separate window, so it must scale independently of the scene's content.
        selector.setOnShowing(event -> {
            selector.applyCss();
            double scale = selector.localToScreen(selector.getBoundsInLocal()).getWidth() / selector.getWidth();
            ListView<?> options = (ListView<?>) ((ComboBoxListViewSkin<?>) selector.getSkin()).getPopupContent();
            optionScale.set(scale);
            options.setFixedCellSize(32 * scale);
            options.setPrefWidth(selector.getWidth() * scale);
        });
    }
}
