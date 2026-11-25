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

    /** Restricts construction of this static utility class. */
    private GameModeSelector() {}

    /**
     * Configures mode display names, selection audio, and popup scaling.
     * Call once after FXML injection. Audio plays on changes after the initial selection.
     *
     * @param selector the dropdown to populate with all game modes
     * @param initialMode the mode to select when the screen opens
     * @param sounds the service that plays the selection sound
     * @throws NullPointerException if any argument is null
     */
    public static void configure(ComboBox<GameMode> selector, GameMode initialMode, SoundService sounds) {
        Objects.requireNonNull(selector);
        Objects.requireNonNull(initialMode);
        Objects.requireNonNull(sounds);

        // Each cell binds its font size and padding to this shared popup scale.
        DoubleProperty optionScale = new SimpleDoubleProperty(1);

        selector.setEditable(false);
        selector.getItems().setAll(GameMode.values());
        selector.setConverter(new StringConverter<>() {
            /**
             * Supplies the display label for the selected mode.
             *
             * @param mode the selected mode, or {@code null} when the selection is empty
             * @return the mode's display name or an empty string
             */
            @Override public String toString(GameMode mode) {
                return mode == null ? "" : mode.getDisplayName();
            }

            /**
             * Finds a mode by its display name, ignoring case.
             *
             * @param text the display name to match
             * @return the matching game mode
             * @throws IllegalArgumentException if no mode matches
             */
            @Override public GameMode fromString(String text) {
                return Arrays.stream(GameMode.values())
                        .filter(mode -> mode.getDisplayName().equalsIgnoreCase(text))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unknown game mode: " + text));
            }
        });

        // Restore the initial selection before attaching the sound listener.
        selector.setValue(initialMode);
        selector.valueProperty().addListener((observable, previous, selected) -> {
            if (selected != null) sounds.play(SoundEffect.BUTTON);
        });

        selector.setCellFactory(list -> {
            // The popup reuses this cell for multiple entries as the list changes.
            ListCell<GameMode> cell = new ListCell<>() {
                /**
                 * Updates a reused popup row and clears its label when the row is empty.
                 *
                 * @param mode the mode assigned to this row, or {@code null} for an empty row
                 * @param empty whether the row has no item
                 */
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
            // Compare the displayed width with the layout width to recover the scene's scale.
            double scale = selector.localToScreen(selector.getBoundsInLocal()).getWidth() / selector.getWidth();
            // The skin exposes the popup list that controls the row height and width.
            ListView<?> options = (ListView<?>) ((ComboBoxListViewSkin<?>) selector.getSkin()).getPopupContent();
            optionScale.set(scale);
            options.setFixedCellSize(32 * scale);
            options.setPrefWidth(selector.getWidth() * scale);
        });
    }
}
