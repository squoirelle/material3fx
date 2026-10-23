package dev.m3fx.controls;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.AccessibleRole;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyEvent;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

/**
 * A numeric {@link MaterialTextField}: the text is parsed into a {@link #valueProperty() value}
 * (null when empty), constrained by {@link #minProperty() min}, {@link #maxProperty() max} and
 * {@link #decimalsProperty() decimals}. Units go in the {@link #setSuffix(String) suffix}.
 * <p>
 * Only numeric input is accepted (in the user's locale: "2,5" in French, "2.5" in English).
 * Up / Down add or remove one {@link #stepProperty() step}, Page Up / Page Down ten steps. The value
 * is normalized (clamped, rounded, formatted) on Enter and when the field loses focus; while typing,
 * an out-of-range value shows an error message.
 *
 * <pre>{@code
 * NumberField altitude = new NumberField("Altitude");
 * altitude.setSuffix("ft");
 * altitude.setMin(0);
 * altitude.setMax(45000);
 * altitude.setStep(500);
 * altitude.valueProperty().addListener((obs, old, value) -> ...);
 * }</pre>
 */
public class NumberField extends MaterialTextField {

    private final ObjectProperty<Double> value = new SimpleObjectProperty<>(this, "value");
    private final DoubleProperty min = new SimpleDoubleProperty(this, "min", Double.NEGATIVE_INFINITY);
    private final DoubleProperty max = new SimpleDoubleProperty(this, "max", Double.POSITIVE_INFINITY);
    private final DoubleProperty step = new SimpleDoubleProperty(this, "step", 1);
    private final IntegerProperty decimals = new SimpleIntegerProperty(this, "decimals", 2);
    private final ObjectProperty<Locale> locale = new SimpleObjectProperty<>(this, "locale", Locale.getDefault());
    private boolean updating;

    public NumberField() {
        this(null);
    }

    public NumberField(String label) {
        super(label);
        getStyleClass().add("number-field");
        setAccessibleRole(AccessibleRole.SPINNER);
        setTextFormatter(new TextFormatter<String>(change -> isNumericInput(change.getControlNewText()) ? change : null));
        textProperty().addListener(o -> {
            if (!updating) {
                parseWhileTyping();
                validate();
            }
        });
        value.addListener(o -> {
            if (!updating) {
                showValue();
            }
        });
        focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) {
                commit();
            }
        });
        addEventHandler(ActionEvent.ACTION, e -> commit());
        addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case UP, KP_UP -> increment(1);
                case DOWN, KP_DOWN -> increment(-1);
                case PAGE_UP -> increment(10);
                case PAGE_DOWN -> increment(-10);
                default -> {
                    return;
                }
            }
            e.consume();
        });
        locale.addListener(o -> showValue());
        decimals.addListener(o -> showValue());
        setValidator(text -> {
            Double v = parse(text);
            if (text.isBlank() || v == null) {
                return null;
            }
            if (v < getMin() || v > getMax()) {
                return "Must be between " + format(getMin()) + " and " + format(getMax());
            }
            return null;
        });
    }

    /** Adds {@code steps} times the step to the value (or starts from min / 0 when empty), clamped. */
    public void increment(int steps) {
        Double current = getValue();
        double base = current != null ? current : (Double.isFinite(getMin()) ? getMin() : 0);
        setValue(clamp(round(base + steps * getStep())));
        selectAll();
    }

    /** Clamps, rounds and reformats the current text, as on Enter or when focus leaves. */
    public void commit() {
        Double parsed = parse(getText());
        setValue(parsed == null ? null : clamp(round(parsed)));
        showValue();
        validate();
    }

    private void parseWhileTyping() {
        Double parsed = parse(getText());
        updating = true;
        try {
            value.set(parsed);
        } finally {
            updating = false;
        }
    }

    private void showValue() {
        updating = true;
        try {
            Double v = getValue();
            setText(v == null ? "" : format(v));
        } finally {
            updating = false;
        }
    }

    private double clamp(double v) {
        return Math.max(getMin(), Math.min(getMax(), v));
    }

    private double round(double v) {
        if (getDecimals() < 0) {
            return v;
        }
        double factor = Math.pow(10, getDecimals());
        return Math.round(v * factor) / factor;
    }

    private DecimalFormatSymbols symbols() {
        return DecimalFormatSymbols.getInstance(getLocale());
    }

    private boolean isNumericInput(String text) {
        char decimal = symbols().getDecimalSeparator();
        boolean allowNegative = getMin() < 0;
        boolean seenDecimal = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isDigit(c)) {
                continue;
            }
            if ((c == decimal || c == '.') && !seenDecimal && getDecimals() != 0) {
                seenDecimal = true;
                continue;
            }
            if (c == '-' && i == 0 && allowNegative) {
                continue;
            }
            return false;
        }
        return true;
    }

    /** Parses text in the field's locale ("." is also accepted as decimal separator); null if not a number. */
    public Double parse(String text) {
        if (text == null || text.isBlank() || text.equals("-")) {
            return null;
        }
        String normalized = text.trim().replace('.', symbols().getDecimalSeparator());
        NumberFormat format = NumberFormat.getNumberInstance(getLocale());
        format.setGroupingUsed(false);
        ParsePosition position = new ParsePosition(0);
        Number number = format.parse(normalized, position);
        return number == null || position.getIndex() != normalized.length() ? null : number.doubleValue();
    }

    /** Formats a number in the field's locale with at most {@link #getDecimals()} decimals. */
    public String format(double v) {
        DecimalFormat format = (DecimalFormat) NumberFormat.getNumberInstance(getLocale());
        format.setGroupingUsed(false);
        format.setMaximumFractionDigits(getDecimals() < 0 ? 340 : getDecimals());
        return format.format(v);
    }

    /** The number, or null when the field is empty or not (yet) a number. */
    public final ObjectProperty<Double> valueProperty() {
        return value;
    }

    public final Double getValue() {
        return value.get();
    }

    public final void setValue(Double newValue) {
        value.set(newValue);
    }

    public final DoubleProperty minProperty() {
        return min;
    }

    public final double getMin() {
        return min.get();
    }

    public final void setMin(double v) {
        min.set(v);
    }

    public final DoubleProperty maxProperty() {
        return max;
    }

    public final double getMax() {
        return max.get();
    }

    public final void setMax(double v) {
        max.set(v);
    }

    /** Amount added by Up / Down (ten times for Page Up / Page Down). Default 1. */
    public final DoubleProperty stepProperty() {
        return step;
    }

    public final double getStep() {
        return step.get();
    }

    public final void setStep(double v) {
        step.set(v);
    }

    /** Maximum number of decimals kept; 0 for integers, -1 for unlimited. Default 2. */
    public final IntegerProperty decimalsProperty() {
        return decimals;
    }

    public final int getDecimals() {
        return decimals.get();
    }

    public final void setDecimals(int v) {
        decimals.set(v);
    }

    /** Locale for parsing and formatting (decimal separator). Defaults to the system locale. */
    public final ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    public final Locale getLocale() {
        return locale.get();
    }

    public final void setLocale(Locale v) {
        locale.set(v);
    }
}
