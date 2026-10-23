package io.github.squoirelle.material3fx.controls;

import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Material modal date range picker: a {@link DateRangeCalendar} in a {@link MaterialDialog}, with
 * the selected range as headline.
 *
 * <pre>{@code
 * DateRangePickerDialog.show(owner, null, "Mission period").thenAccept(range -> ...);
 * }</pre>
 */
public final class DateRangePickerDialog {

    private DateRangePickerDialog() {
    }

    /** Completes with the chosen range, or null if cancelled. */
    public static CompletableFuture<DateRange> show(Node owner, DateRange initial, String title) {
        DateRangeCalendar calendar = new DateRangeCalendar(initial);
        Label supporting = new Label(title);
        supporting.getStyleClass().add("date-picker-dialog-title");
        Label headline = new Label();
        headline.getStyleClass().add("date-picker-dialog-headline");
        headline.textProperty().bind(Bindings.createStringBinding(() -> {
            if (calendar.getValue() != null) {
                return calendar.getValue().format(Locale.getDefault());
            }
            return calendar.getPendingStart() != null ? "Select end date" : "Select start date";
        }, calendar.valueProperty(), calendar.pendingStartProperty()));
        VBox header = new VBox(28, supporting, headline);
        header.getStyleClass().add("date-picker-dialog-header");
        VBox content = new VBox(header, new Divider(), calendar);
        content.getStyleClass().add("date-picker-dialog-content");

        MaterialDialog<DateRange> dialog = new MaterialDialog<>();
        dialog.getStyleClass().addAll("date-picker-dialog", "date-range-picker-dialog");
        dialog.setContent(content);
        ButtonType ok = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getButtonTypes().addAll(ButtonType.CANCEL, ok);
        dialog.setResultConverter(button -> button == ok ? calendar.getValue() : null);
        CompletableFuture<DateRange> result = dialog.show(owner);
        calendar.requestFocus();
        return result;
    }
}
