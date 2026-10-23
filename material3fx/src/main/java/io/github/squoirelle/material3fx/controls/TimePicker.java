package io.github.squoirelle.material3fx.controls;

import io.github.squoirelle.material3fx.skins.TimePickerSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;

/**
 * Material 3 time picker with a clock dial. Like {@link javafx.scene.control.DatePicker}, the
 * selection is the {@link #valueProperty() value}.
 * <p>
 * Keyboard: the hour and minute boxes are editable; on the dial, arrows change the current unit,
 * Page Up / Page Down jump by 5 minutes (or 3 hours) and Enter moves from hours to minutes.
 *
 * <pre>{@code
 * TimePicker.showDialog(owner, LocalTime.of(9, 30)).thenAccept(time -> ...);
 * }</pre>
 */
public class TimePicker extends Control {

    private final ObjectProperty<LocalTime> value = new SimpleObjectProperty<>(this, "value") {
        @Override
        public void set(LocalTime newValue) {
            super.set(newValue == null ? null : newValue.withSecond(0).withNano(0));
        }
    };
    private final BooleanProperty is24Hour = new SimpleBooleanProperty(this, "is24Hour");

    public TimePicker() {
        this(LocalTime.now());
    }

    public TimePicker(LocalTime initial) {
        getStyleClass().add("time-picker");
        setFocusTraversable(false);
        value.set(initial == null ? LocalTime.NOON : initial);
    }

    /** Shows a time picker dialog; completes with the chosen time, or null if cancelled. */
    public static CompletableFuture<LocalTime> showDialog(Node owner, LocalTime initial) {
        TimePicker picker = new TimePicker(initial == null ? LocalTime.now() : initial);
        MaterialDialog<LocalTime> dialog = new MaterialDialog<>("Select time", null);
        dialog.getStyleClass().add("time-picker-dialog");
        dialog.setContent(picker);
        ButtonType ok = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getButtonTypes().addAll(ButtonType.CANCEL, ok);
        dialog.setResultConverter(button -> button == ok ? picker.getValue() : null);
        return dialog.show(owner);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TimePickerSkin(this);
    }

    public final ObjectProperty<LocalTime> valueProperty() {
        return value;
    }

    public final LocalTime getValue() {
        return value.get();
    }

    public final void setValue(LocalTime newValue) {
        value.set(newValue);
    }

    public final BooleanProperty is24HourProperty() {
        return is24Hour;
    }

    public final boolean is24Hour() {
        return is24Hour.get();
    }

    public final void set24Hour(boolean value) {
        is24Hour.set(value);
    }
}
