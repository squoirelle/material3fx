package dev.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Material 3 time picker with a clock dial. The hour and minute boxes in the header are also
 * editable with the keyboard.
 *
 * <pre>{@code
 * TimePicker.showDialog(owner, LocalTime.of(9, 30)).thenAccept(time -> ...);
 * }</pre>
 */
public class TimePicker extends VBox {

    private enum Mode { HOUR, MINUTE }

    private static final double DIAL = 256;
    private static final double CENTER = DIAL / 2;
    private static final double OUTER_RADIUS = 100;
    private static final double INNER_RADIUS = 64;
    private static final double SELECTOR_RADIUS = 24;

    private final ObjectProperty<LocalTime> time = new SimpleObjectProperty<>(this, "time", LocalTime.of(12, 0));
    private final BooleanProperty is24Hour = new SimpleBooleanProperty(this, "is24Hour");
    private Mode mode = Mode.HOUR;

    private final TextField hourField = new TextField();
    private final TextField minuteField = new TextField();
    private final ToggleButton am = new ToggleButton("AM");
    private final ToggleButton pm = new ToggleButton("PM");
    private final VBox amPm = new VBox(am, pm);
    private final Pane dial = new Pane();
    private final Circle dialFace = new Circle(CENTER, CENTER, CENTER);
    private final Line hand = new Line();
    private final Circle selector = new Circle(SELECTOR_RADIUS);
    private final Circle centerDot = new Circle(CENTER, CENTER, 4);
    private final Circle selectorDot = new Circle(2);
    private final List<Label> numbers = new ArrayList<>();

    public TimePicker() {
        this(LocalTime.now().withSecond(0).withNano(0));
    }

    public TimePicker(LocalTime initial) {
        getStyleClass().add("time-picker");
        setAlignment(Pos.TOP_CENTER);

        hourField.getStyleClass().addAll("time-picker-field", "hour");
        minuteField.getStyleClass().addAll("time-picker-field", "minute");
        for (TextField field : List.of(hourField, minuteField)) {
            field.setAlignment(Pos.CENTER);
            field.setTextFormatter(new TextFormatter<String>(c -> c.getControlNewText().matches("\\d{0,2}") ? c : null));
            field.focusedProperty().addListener((obs, old, focused) -> {
                if (focused) {
                    setMode(field == hourField ? Mode.HOUR : Mode.MINUTE);
                } else {
                    commitFields();
                }
            });
            field.setOnAction(e -> commitFields());
        }
        hourField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) {
                setTime(getTime().plusHours(e.getCode() == KeyCode.UP ? 1 : -1));
                e.consume();
            }
        });
        minuteField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) {
                setTime(getTime().plusMinutes(e.getCode() == KeyCode.UP ? 1 : -1));
                e.consume();
            }
        });
        Label colon = new Label(":");
        colon.getStyleClass().add("time-picker-separator");

        ToggleGroup period = new ToggleGroup();
        am.setToggleGroup(period);
        pm.setToggleGroup(period);
        am.getStyleClass().addAll("time-picker-period", "am");
        pm.getStyleClass().addAll("time-picker-period", "pm");
        amPm.getStyleClass().add("time-picker-am-pm");
        am.setMaxWidth(Double.MAX_VALUE);
        pm.setMaxWidth(Double.MAX_VALUE);
        am.setOnAction(e -> setPeriod(false));
        pm.setOnAction(e -> setPeriod(true));

        HBox header = new HBox(colon);
        header.getStyleClass().add("time-picker-header");
        header.setAlignment(Pos.CENTER);
        header.getChildren().setAll(hourField, colon, minuteField, amPm);

        dial.getStyleClass().add("time-picker-dial");
        dial.setMinSize(DIAL, DIAL);
        dial.setPrefSize(DIAL, DIAL);
        dial.setMaxSize(DIAL, DIAL);
        dialFace.getStyleClass().add("time-picker-face");
        hand.getStyleClass().add("time-picker-hand");
        selector.getStyleClass().add("time-picker-selector");
        centerDot.getStyleClass().add("time-picker-center");
        selectorDot.getStyleClass().add("time-picker-selector-dot");
        hand.setStartX(CENTER);
        hand.setStartY(CENTER);
        dial.getChildren().addAll(dialFace, hand, selector, centerDot, selectorDot);
        dial.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onDial);
        dial.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onDial);
        dial.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (mode == Mode.HOUR) {
                setMode(Mode.MINUTE);
            }
        });

        getChildren().addAll(header, dial);
        time.addListener(o -> update());
        is24Hour.addListener(o -> {
            amPm.setVisible(!is24Hour.get());
            amPm.setManaged(!is24Hour.get());
            buildNumbers();
            update();
        });
        setTime(initial);
        buildNumbers();
        update();
    }

    /** Shows a time picker dialog; completes with the chosen time, or null if cancelled. */
    public static CompletableFuture<LocalTime> showDialog(Node owner, LocalTime initial) {
        TimePicker picker = new TimePicker(initial == null ? LocalTime.now().withSecond(0).withNano(0) : initial);
        MaterialDialog dialog = new MaterialDialog("Select time", null);
        dialog.getStyleClass().add("time-picker-dialog");
        dialog.setContent(picker);
        dialog.addAction("Cancel");
        dialog.addPrimaryAction("OK");
        return dialog.show(owner).thenApply(result -> "OK".equals(result) ? picker.getTime() : null);
    }

    private void setMode(Mode newMode) {
        mode = newMode;
        buildNumbers();
        update();
    }

    private void setPeriod(boolean afternoon) {
        int hour = getTime().getHour();
        if (afternoon && hour < 12) {
            setTime(getTime().plusHours(12));
        } else if (!afternoon && hour >= 12) {
            setTime(getTime().minusHours(12));
        }
        update();
    }

    private void commitFields() {
        try {
            int h = Integer.parseInt(hourField.getText());
            int m = Integer.parseInt(minuteField.getText());
            if (!is24Hour()) {
                h = h % 12 + (getTime().getHour() >= 12 ? 12 : 0);
            }
            if (h >= 0 && h < 24 && m >= 0 && m < 60) {
                setTime(LocalTime.of(h, m));
            }
        } catch (NumberFormatException ignored) {
            // Keep the previous value.
        }
        update();
    }

    private void onDial(MouseEvent e) {
        double dx = e.getX() - CENTER;
        double dy = e.getY() - CENTER;
        double angle = (Math.toDegrees(Math.atan2(dx, -dy)) + 360) % 360;
        double distance = Math.hypot(dx, dy);
        LocalTime t = getTime();
        if (mode == Mode.HOUR) {
            int position = (int) Math.round(angle / 30) % 12;
            int hour;
            if (is24Hour()) {
                boolean inner = distance < (OUTER_RADIUS + INNER_RADIUS) / 2;
                hour = inner ? (position == 0 ? 0 : position + 12) : (position == 0 ? 12 : position);
            } else {
                hour = position + (t.getHour() >= 12 ? 12 : 0);
            }
            setTime(t.withHour(hour));
        } else {
            int minute = (int) Math.round(angle / 6) % 60;
            setTime(t.withMinute(minute));
        }
        e.consume();
    }

    private void buildNumbers() {
        dial.getChildren().removeAll(numbers);
        numbers.clear();
        if (mode == Mode.HOUR) {
            for (int i = 0; i < 12; i++) {
                int outer = i == 0 ? 12 : i;
                numbers.add(number(String.valueOf(outer), i * 30, OUTER_RADIUS, outer));
                if (is24Hour()) {
                    int inner = i == 0 ? 0 : i + 12;
                    numbers.add(number(String.format("%02d", inner), i * 30, INNER_RADIUS, inner));
                }
            }
        } else {
            for (int i = 0; i < 12; i++) {
                numbers.add(number(String.format("%02d", i * 5), i * 30, OUTER_RADIUS, i * 5));
            }
        }
        dial.getChildren().addAll(numbers);
    }

    private Label number(String text, double angle, double radius, int value) {
        Label label = new Label(text);
        label.getStyleClass().add("time-picker-number");
        if (radius == INNER_RADIUS) {
            label.getStyleClass().add("inner");
        }
        label.setUserData(value);
        label.setMouseTransparent(true);
        label.setMinSize(48, 48);
        label.setPrefSize(48, 48);
        label.setAlignment(Pos.CENTER);
        double rad = Math.toRadians(angle);
        label.relocate(CENTER + radius * Math.sin(rad) - 24, CENTER - radius * Math.cos(rad) - 24);
        return label;
    }

    private void update() {
        LocalTime t = getTime();
        int hour = t.getHour();
        int displayHour = is24Hour() ? hour : (hour % 12 == 0 ? 12 : hour % 12);
        if (!hourField.isFocused()) {
            hourField.setText(String.format("%02d", displayHour));
        }
        if (!minuteField.isFocused()) {
            minuteField.setText(String.format("%02d", t.getMinute()));
        }
        hourField.pseudoClassStateChanged(PseudoClasses.SELECTED, mode == Mode.HOUR);
        minuteField.pseudoClassStateChanged(PseudoClasses.SELECTED, mode == Mode.MINUTE);
        am.setSelected(hour < 12);
        pm.setSelected(hour >= 12);

        double angle;
        double radius = OUTER_RADIUS;
        int selectedValue;
        if (mode == Mode.HOUR) {
            angle = (hour % 12) * 30;
            if (is24Hour() && (hour == 0 || hour > 12)) {
                radius = INNER_RADIUS;
            }
            selectedValue = is24Hour() ? hour : displayHour;
        } else {
            angle = t.getMinute() * 6;
            selectedValue = t.getMinute();
        }
        double rad = Math.toRadians(angle);
        double x = CENTER + radius * Math.sin(rad);
        double y = CENTER - radius * Math.cos(rad);
        selector.setCenterX(x);
        selector.setCenterY(y);
        selectorDot.setCenterX(x);
        selectorDot.setCenterY(y);
        hand.setEndX(CENTER + (radius - SELECTOR_RADIUS) * Math.sin(rad));
        hand.setEndY(CENTER - (radius - SELECTOR_RADIUS) * Math.cos(rad));
        boolean onNumber = false;
        for (Label number : numbers) {
            boolean selected = Integer.valueOf(selectedValue).equals(number.getUserData())
                    && (mode != Mode.HOUR || !is24Hour() || number.getStyleClass().contains("inner") == (radius == INNER_RADIUS));
            number.pseudoClassStateChanged(PseudoClasses.SELECTED, selected);
            onNumber |= selected;
        }
        // Minutes between labels show a small dot in the selector.
        selectorDot.setVisible(!onNumber);
    }

    public final ObjectProperty<LocalTime> timeProperty() {
        return time;
    }

    public final LocalTime getTime() {
        return time.get();
    }

    public final void setTime(LocalTime value) {
        time.set(value.withSecond(0).withNano(0));
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
