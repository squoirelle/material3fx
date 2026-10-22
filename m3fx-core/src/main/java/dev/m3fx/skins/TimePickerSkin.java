package dev.m3fx.skins;

import dev.m3fx.controls.TimePicker;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Skin of {@link TimePicker}: editable hour / minute boxes, AM / PM toggle, and a clock dial
 * (inner ring for 13-00 in 24-hour mode). The dial is focusable and keyboard-operable.
 */
public class TimePickerSkin extends SkinBase<TimePicker> {

    private enum Mode { HOUR, MINUTE }

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    private static final double DIAL = 256;
    private static final double CENTER = DIAL / 2;
    private static final double OUTER_RADIUS = 100;
    private static final double INNER_RADIUS = 64;
    private static final double SELECTOR_RADIUS = 24;

    private Mode mode = Mode.HOUR;
    private final VBox box = new VBox();
    private final TextField hourField = new TextField();
    private final TextField minuteField = new TextField();
    private final ToggleButton am = new ToggleButton("AM");
    private final ToggleButton pm = new ToggleButton("PM");
    private final VBox amPm = new VBox(am, pm);
    private final Pane dial = new Pane();
    private final Circle face = new Circle(CENTER, CENTER, CENTER);
    private final Line hand = new Line();
    private final Circle selector = new Circle(SELECTOR_RADIUS);
    private final Circle centerDot = new Circle(CENTER, CENTER, 4);
    private final Circle selectorDot = new Circle(2);
    private final List<Label> numbers = new ArrayList<>();

    public TimePickerSkin(TimePicker picker) {
        super(picker);
        box.getStyleClass().add("time-picker-content");
        box.setAlignment(Pos.TOP_CENTER);
        hourField.getStyleClass().addAll("time-picker-field", "hour");
        minuteField.getStyleClass().addAll("time-picker-field", "minute");
        hourField.setAccessibleText("Hour");
        minuteField.setAccessibleText("Minute");
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
            field.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) {
                    step(field == hourField ? Mode.HOUR : Mode.MINUTE, e.getCode() == KeyCode.UP ? 1 : -1);
                    e.consume();
                }
            });
        }
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

        HBox header = new HBox(hourField, colon, minuteField, amPm);
        header.getStyleClass().add("time-picker-header");
        header.setAlignment(Pos.CENTER);

        dial.getStyleClass().add("time-picker-dial");
        dial.setMinSize(DIAL, DIAL);
        dial.setPrefSize(DIAL, DIAL);
        dial.setMaxSize(DIAL, DIAL);
        dial.setFocusTraversable(true);
        dial.setAccessibleRole(AccessibleRole.SLIDER);
        face.getStyleClass().add("time-picker-face");
        hand.getStyleClass().add("time-picker-hand");
        selector.getStyleClass().add("time-picker-selector");
        centerDot.getStyleClass().add("time-picker-center");
        selectorDot.getStyleClass().add("time-picker-selector-dot");
        hand.setStartX(CENTER);
        hand.setStartY(CENTER);
        dial.getChildren().addAll(face, hand, selector, centerDot, selectorDot);
        dial.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onDial);
        dial.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onDial);
        dial.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (mode == Mode.HOUR) {
                setMode(Mode.MINUTE);
            }
        });
        dial.addEventHandler(KeyEvent.KEY_PRESSED, this::onDialKey);

        box.getChildren().addAll(header, dial);
        getChildren().add(box);
        registerChangeListener(picker.valueProperty(), o -> update());
        registerChangeListener(picker.is24HourProperty(), o -> {
            amPm.setVisible(!picker.is24Hour());
            amPm.setManaged(!picker.is24Hour());
            buildNumbers();
            update();
        });
        amPm.setVisible(!picker.is24Hour());
        amPm.setManaged(!picker.is24Hour());
        buildNumbers();
        update();
    }

    private LocalTime time() {
        LocalTime t = getSkinnable().getValue();
        return t == null ? LocalTime.NOON : t;
    }

    private void onDialKey(KeyEvent e) {
        switch (e.getCode()) {
            case UP, RIGHT -> step(mode, 1);
            case DOWN, LEFT -> step(mode, -1);
            case PAGE_UP -> step(mode, mode == Mode.MINUTE ? 5 : 3);
            case PAGE_DOWN -> step(mode, mode == Mode.MINUTE ? -5 : -3);
            case ENTER -> {
                if (mode == Mode.HOUR) {
                    setMode(Mode.MINUTE);
                } else {
                    return;   // let Enter reach the dialog's default button
                }
            }
            default -> {
                return;
            }
        }
        e.consume();
    }

    private void step(Mode unit, int amount) {
        LocalTime t = time();
        getSkinnable().setValue(unit == Mode.HOUR ? t.plusHours(amount) : t.plusMinutes(amount));
    }

    private void setMode(Mode newMode) {
        mode = newMode;
        buildNumbers();
        update();
    }

    private void setPeriod(boolean afternoon) {
        int hour = time().getHour();
        if (afternoon && hour < 12) {
            getSkinnable().setValue(time().plusHours(12));
        } else if (!afternoon && hour >= 12) {
            getSkinnable().setValue(time().minusHours(12));
        }
        update();
    }

    private void commitFields() {
        try {
            int h = Integer.parseInt(hourField.getText());
            int m = Integer.parseInt(minuteField.getText());
            if (!getSkinnable().is24Hour()) {
                h = h % 12 + (time().getHour() >= 12 ? 12 : 0);
            }
            if (h >= 0 && h < 24 && m >= 0 && m < 60) {
                getSkinnable().setValue(LocalTime.of(h, m));
            }
        } catch (NumberFormatException ignored) {
            // Keep the previous value.
        }
        update();
    }

    private void onDial(MouseEvent e) {
        dial.requestFocus();
        double dx = e.getX() - CENTER;
        double dy = e.getY() - CENTER;
        double angle = (Math.toDegrees(Math.atan2(dx, -dy)) + 360) % 360;
        double distance = Math.hypot(dx, dy);
        LocalTime t = time();
        if (mode == Mode.HOUR) {
            int position = (int) Math.round(angle / 30) % 12;
            int hour;
            if (getSkinnable().is24Hour()) {
                boolean inner = distance < (OUTER_RADIUS + INNER_RADIUS) / 2;
                hour = inner ? (position == 0 ? 0 : position + 12) : (position == 0 ? 12 : position);
            } else {
                hour = position + (t.getHour() >= 12 ? 12 : 0);
            }
            getSkinnable().setValue(t.withHour(hour));
        } else {
            getSkinnable().setValue(t.withMinute((int) Math.round(angle / 6) % 60));
        }
        e.consume();
    }

    private void buildNumbers() {
        dial.getChildren().removeAll(numbers);
        numbers.clear();
        boolean h24 = getSkinnable().is24Hour();
        for (int i = 0; i < 12; i++) {
            if (mode == Mode.HOUR) {
                int outer = i == 0 ? 12 : i;
                numbers.add(number(String.valueOf(outer), i * 30, OUTER_RADIUS, outer));
                if (h24) {
                    int inner = i == 0 ? 0 : i + 12;
                    numbers.add(number(String.format("%02d", inner), i * 30, INNER_RADIUS, inner));
                }
            } else {
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
        TimePicker picker = getSkinnable();
        LocalTime t = time();
        int hour = t.getHour();
        boolean h24 = picker.is24Hour();
        int displayHour = h24 ? hour : (hour % 12 == 0 ? 12 : hour % 12);
        if (!hourField.isFocused()) {
            hourField.setText(String.format("%02d", displayHour));
        }
        if (!minuteField.isFocused()) {
            minuteField.setText(String.format("%02d", t.getMinute()));
        }
        hourField.pseudoClassStateChanged(SELECTED, mode == Mode.HOUR);
        minuteField.pseudoClassStateChanged(SELECTED, mode == Mode.MINUTE);
        am.setSelected(hour < 12);
        pm.setSelected(hour >= 12);

        double angle;
        double radius = OUTER_RADIUS;
        int selectedValue;
        if (mode == Mode.HOUR) {
            angle = (hour % 12) * 30;
            if (h24 && (hour == 0 || hour > 12)) {
                radius = INNER_RADIUS;
            }
            selectedValue = h24 ? hour : displayHour;
            dial.setAccessibleText("Hour " + displayHour);
        } else {
            angle = t.getMinute() * 6;
            selectedValue = t.getMinute();
            dial.setAccessibleText("Minute " + t.getMinute());
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
        double r = radius;
        for (Label number : numbers) {
            boolean selected = Integer.valueOf(selectedValue).equals(number.getUserData())
                    && (mode != Mode.HOUR || !h24 || number.getStyleClass().contains("inner") == (r == INNER_RADIUS));
            number.pseudoClassStateChanged(SELECTED, selected);
            onNumber |= selected;
        }
        // Minutes between labels show a small dot in the selector.
        selectorDot.setVisible(!onNumber);
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
}
