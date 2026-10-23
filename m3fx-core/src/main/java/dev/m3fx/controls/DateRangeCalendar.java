package dev.m3fx.controls;

import dev.m3fx.skins.DateRangeCalendarSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * A month calendar for picking a {@link DateRange}: the first selection sets the start, the second
 * the end (in either order). Days between are highlighted.
 * <p>
 * Keyboard: arrows move the focused day (Left / Right a day, Up / Down a week), Page Up / Page Down
 * change month, Home / End go to the start / end of the week, Enter or Space selects.
 */
public class DateRangeCalendar extends Control {

    private final ObjectProperty<DateRange> value = new SimpleObjectProperty<>(this, "value");
    private final ReadOnlyObjectWrapper<LocalDate> pendingStart = new ReadOnlyObjectWrapper<>(this, "pendingStart");
    private final ObjectProperty<LocalDate> focusedDate = new SimpleObjectProperty<>(this, "focusedDate", LocalDate.now());
    private final ObjectProperty<YearMonth> month = new SimpleObjectProperty<>(this, "month", YearMonth.now());

    public DateRangeCalendar() {
        this(null);
    }

    public DateRangeCalendar(DateRange initial) {
        getStyleClass().add("date-range-calendar");
        setFocusTraversable(true);
        setAccessibleRole(AccessibleRole.NODE);
        value.set(initial);
        if (initial != null) {
            focusedDate.set(initial.start());
        }
        month.set(YearMonth.from(focusedDate.get()));
        // The displayed month follows the focused day.
        focusedDate.addListener((obs, old, date) -> {
            if (date != null && !YearMonth.from(date).equals(month.get())) {
                month.set(YearMonth.from(date));
            }
        });
    }

    /**
     * Selects {@code date}: starts a new range, or completes the pending one. A completed range is
     * written to {@link #valueProperty() value}.
     */
    public void select(LocalDate date) {
        LocalDate start = pendingStart.get();
        if (start == null) {
            pendingStart.set(date);
            value.set(null);
        } else {
            pendingStart.set(null);
            value.set(new DateRange(start, date));
        }
        focusedDate.set(date);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DateRangeCalendarSkin(this);
    }

    /** The selected range, or null while only the start is picked. */
    public final ObjectProperty<DateRange> valueProperty() {
        return value;
    }

    public final DateRange getValue() {
        return value.get();
    }

    public final void setValue(DateRange v) {
        pendingStart.set(null);
        value.set(v);
    }

    /** The start picked so far, while waiting for the end. */
    public final ReadOnlyObjectProperty<LocalDate> pendingStartProperty() {
        return pendingStart.getReadOnlyProperty();
    }

    public final LocalDate getPendingStart() {
        return pendingStart.get();
    }

    /** The day with keyboard focus. */
    public final ObjectProperty<LocalDate> focusedDateProperty() {
        return focusedDate;
    }

    public final LocalDate getFocusedDate() {
        return focusedDate.get();
    }

    public final void setFocusedDate(LocalDate v) {
        focusedDate.set(v);
    }

    /** The displayed month. */
    public final ObjectProperty<YearMonth> monthProperty() {
        return month;
    }

    public final YearMonth getMonth() {
        return month.get();
    }

    public final void setMonth(YearMonth v) {
        month.set(v);
    }
}
