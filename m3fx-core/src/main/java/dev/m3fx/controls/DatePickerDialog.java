package dev.m3fx.controls;

import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Material modal date picker: the JavaFX calendar (styled by m3fx) in a {@link MaterialDialog}
 * with the Material header showing the selected date.
 *
 * <pre>{@code
 * DatePickerDialog.show(owner, LocalDate.now()).thenAccept(date -> ...);
 * }</pre>
 */
public final class DatePickerDialog {

    private static final DateTimeFormatter HEADLINE = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault());

    private DatePickerDialog() {
    }

    /** Completes with the chosen date, or null if cancelled. */
    public static CompletableFuture<LocalDate> show(Node owner, LocalDate initial) {
        return show(owner, initial, "Select date");
    }

    public static CompletableFuture<LocalDate> show(Node owner, LocalDate initial, String title) {
        DatePicker picker = new DatePicker(initial == null ? LocalDate.now() : initial);
        Node calendar = new DatePickerSkin(picker).getPopupContent();
        calendar.getStyleClass().add("date-picker-dialog-calendar");

        Label supporting = new Label(title);
        supporting.getStyleClass().add("date-picker-dialog-title");
        Label headline = new Label();
        headline.getStyleClass().add("date-picker-dialog-headline");
        headline.textProperty().bind(picker.valueProperty().map(d -> d == null ? "" : HEADLINE.format(d)));
        VBox header = new VBox(28, supporting, headline);
        header.getStyleClass().add("date-picker-dialog-header");

        VBox content = new VBox(header, new Divider(), calendar);
        content.getStyleClass().add("date-picker-dialog-content");

        MaterialDialog<LocalDate> dialog = new MaterialDialog<>();
        dialog.getStyleClass().add("date-picker-dialog");
        dialog.setContent(content);
        ButtonType ok = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getButtonTypes().addAll(ButtonType.CANCEL, ok);
        dialog.setResultConverter(button -> button == ok ? picker.getValue() : null);
        return dialog.show(owner);
    }
}
