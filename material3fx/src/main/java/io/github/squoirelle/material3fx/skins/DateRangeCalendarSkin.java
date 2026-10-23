package io.github.squoirelle.material3fx.skins;

import io.github.squoirelle.material3fx.controls.DateRange;
import io.github.squoirelle.material3fx.controls.DateRangeCalendar;
import io.github.squoirelle.material3fx.controls.IconButton;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Skin of {@link DateRangeCalendar}: month header with navigation, weekday names, and a 6x7 grid
 * of day cells. Range endpoints are filled circles; days between sit on a band.
 */
public class DateRangeCalendarSkin extends SkinBase<DateRangeCalendar> {

    private static final PseudoClass START = PseudoClass.getPseudoClass("range-start");
    private static final PseudoClass END = PseudoClass.getPseudoClass("range-end");
    private static final PseudoClass IN_RANGE = PseudoClass.getPseudoClass("in-range");
    private static final PseudoClass TODAY = PseudoClass.getPseudoClass("today");
    private static final PseudoClass OTHER_MONTH = PseudoClass.getPseudoClass("other-month");
    private static final PseudoClass FOCUSED_DAY = PseudoClass.getPseudoClass("focused-day");
    private static final PseudoClass PENDING = PseudoClass.getPseudoClass("pending");

    private final VBox box = new VBox();
    private final Label monthLabel = new Label();
    private final GridPane grid = new GridPane();
    private final List<DayCell> cells = new ArrayList<>();
    private final Locale locale = Locale.getDefault();
    private final DayOfWeek firstDay = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
    private final EventHandler<KeyEvent> keyHandler = this::onKeyPressed;

    public DateRangeCalendarSkin(DateRangeCalendar calendar) {
        super(calendar);
        box.getStyleClass().add("date-range-calendar-content");
        monthLabel.getStyleClass().add("date-range-calendar-month");
        grid.getStyleClass().add("date-range-calendar-grid");
        IconButton previous = new IconButton("chevron_left").tooltip("Previous month");
        IconButton next = new IconButton("chevron_right").tooltip("Next month");
        previous.setFocusTraversable(false);
        next.setFocusTraversable(false);
        previous.setOnAction(e -> calendar.setFocusedDate(calendar.getFocusedDate().minusMonths(1)));
        next.setOnAction(e -> calendar.setFocusedDate(calendar.getFocusedDate().plusMonths(1)));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(monthLabel, spacer, previous, next);
        header.getStyleClass().add("date-range-calendar-header");
        header.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < 7; i++) {
            DayOfWeek day = firstDay.plus(i);
            Label name = new Label(day.getDisplayName(TextStyle.NARROW, locale));
            name.getStyleClass().add("date-range-calendar-weekday");
            name.setMaxWidth(Double.MAX_VALUE);
            name.setAlignment(Pos.CENTER);
            grid.add(name, i, 0);
        }
        for (int i = 0; i < 42; i++) {
            DayCell cell = new DayCell();
            cells.add(cell);
            grid.add(cell, i % 7, 1 + i / 7);
        }
        box.getChildren().addAll(header, grid);
        getChildren().add(box);
        calendar.addEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
        for (ObservableValue<?> p : List.of(calendar.valueProperty(), calendar.pendingStartProperty(),
                calendar.focusedDateProperty(), calendar.monthProperty(), calendar.focusedProperty())) {
            registerChangeListener(p, o -> update());
        }
        update();
    }

    private void onKeyPressed(KeyEvent e) {
        DateRangeCalendar c = getSkinnable();
        LocalDate d = c.getFocusedDate();
        switch (e.getCode()) {
            case LEFT, KP_LEFT -> c.setFocusedDate(d.minusDays(1));
            case RIGHT, KP_RIGHT -> c.setFocusedDate(d.plusDays(1));
            case UP, KP_UP -> c.setFocusedDate(d.minusWeeks(1));
            case DOWN, KP_DOWN -> c.setFocusedDate(d.plusWeeks(1));
            case PAGE_UP -> c.setFocusedDate(d.minusMonths(1));
            case PAGE_DOWN -> c.setFocusedDate(d.plusMonths(1));
            case HOME -> c.setFocusedDate(d.minusDays(Math.floorMod(d.getDayOfWeek().getValue() - firstDay.getValue(), 7)));
            case END -> c.setFocusedDate(d.plusDays(6 - Math.floorMod(d.getDayOfWeek().getValue() - firstDay.getValue(), 7)));
            case ENTER, SPACE -> {
                if (c.getPendingStart() == null && e.getCode() == javafx.scene.input.KeyCode.ENTER && c.getValue() != null
                        && c.getValue().contains(d)) {
                    return;   // let Enter confirm a dialog when re-pressing on the selected range
                }
                c.select(d);
            }
            default -> {
                return;
            }
        }
        e.consume();
    }

    private void update() {
        DateRangeCalendar c = getSkinnable();
        YearMonth month = c.getMonth();
        monthLabel.setText(month.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy", locale)));
        LocalDate first = month.atDay(1);
        LocalDate gridStart = first.minusDays(Math.floorMod(first.getDayOfWeek().getValue() - firstDay.getValue(), 7));
        DateRange range = c.getValue();
        LocalDate pending = c.getPendingStart();
        LocalDate today = LocalDate.now();
        boolean focusVisible = c.isFocused();
        for (int i = 0; i < cells.size(); i++) {
            LocalDate date = gridStart.plusDays(i);
            DayCell cell = cells.get(i);
            cell.date = date;
            cell.label.setText(String.valueOf(date.getDayOfMonth()));
            boolean start = range != null && date.equals(range.start()) || date.equals(pending);
            boolean end = range != null && date.equals(range.end());
            boolean in = range != null && range.contains(date) && !range.start().equals(range.end());
            cell.pseudoClassStateChanged(START, start);
            cell.pseudoClassStateChanged(END, end);
            cell.pseudoClassStateChanged(IN_RANGE, in);
            cell.pseudoClassStateChanged(PENDING, date.equals(pending));
            cell.pseudoClassStateChanged(TODAY, date.equals(today));
            cell.pseudoClassStateChanged(OTHER_MONTH, !YearMonth.from(date).equals(month));
            cell.pseudoClassStateChanged(FOCUSED_DAY, focusVisible && date.equals(c.getFocusedDate()));
            cell.setAccessibleText(date.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", locale)));
        }
        c.notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_ITEM);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        box.resizeRelocate(x, y, w, h);
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return left + box.prefWidth(-1) + right;
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        return top + box.prefHeight(-1) + bottom;
    }

    @Override
    public void dispose() {
        getSkinnable().removeEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
        super.dispose();
    }

    /** One day: band (for in-range days) behind a circle with the number. */
    private final class DayCell extends StackPane {
        LocalDate date;
        final Region band = new Region();
        final StackPane circle = new StackPane();
        final Label label = new Label();

        DayCell() {
            getStyleClass().add("date-range-day");
            band.getStyleClass().add("date-range-band");
            circle.getStyleClass().add("date-range-circle");
            label.getStyleClass().add("date-range-day-label");
            circle.getChildren().add(label);
            circle.setMaxSize(40, 40);
            getChildren().addAll(band, circle);
            setAccessibleRole(AccessibleRole.BUTTON);
            addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                if (e.getButton() == MouseButton.PRIMARY && date != null) {
                    getSkinnable().requestFocus();
                    getSkinnable().select(date);
                }
            });
        }
    }
}
