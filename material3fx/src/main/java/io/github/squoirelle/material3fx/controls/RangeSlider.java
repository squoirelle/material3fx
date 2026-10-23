package io.github.squoirelle.material3fx.controls;

import io.github.squoirelle.material3fx.skins.RangeSliderSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * A slider with two thumbs selecting a range ({@link #lowValueProperty() low} to
 * {@link #highValueProperty() high}) between {@link #minProperty() min} and {@link #maxProperty() max},
 * with the same properties as {@link javafx.scene.control.Slider} where they apply.
 * <p>
 * The values are kept ordered: min &le; low &le; high &le; max. Each thumb is focusable: arrows
 * change it by the {@link #blockIncrementProperty() block increment}, Page Up / Page Down by a tenth
 * of the range, Home / End move it as far as possible. Clicking the track moves the nearest thumb.
 *
 * <pre>{@code
 * RangeSlider altitude = new RangeSlider(0, 40000, 5000, 25000);
 * altitude.setBlockIncrement(500);
 * altitude.setSnapToTicks(true);
 * altitude.setMajorTickUnit(1000);
 * }</pre>
 */
public class RangeSlider extends Control {

    private final DoubleProperty min = new SimpleDoubleProperty(this, "min", 0);
    private final DoubleProperty max = new SimpleDoubleProperty(this, "max", 100);
    private final DoubleProperty lowValue = new SimpleDoubleProperty(this, "lowValue", 25);
    private final DoubleProperty highValue = new SimpleDoubleProperty(this, "highValue", 75);
    private final DoubleProperty blockIncrement = new SimpleDoubleProperty(this, "blockIncrement", 1);
    private final DoubleProperty majorTickUnit = new SimpleDoubleProperty(this, "majorTickUnit", 10);
    private final BooleanProperty snapToTicks = new SimpleBooleanProperty(this, "snapToTicks");
    private final BooleanProperty showTickMarks = new SimpleBooleanProperty(this, "showTickMarks");
    private final BooleanProperty lowValueChanging = new SimpleBooleanProperty(this, "lowValueChanging");
    private final BooleanProperty highValueChanging = new SimpleBooleanProperty(this, "highValueChanging");

    public RangeSlider() {
        this(0, 100, 25, 75);
    }

    public RangeSlider(double min, double max, double low, double high) {
        getStyleClass().add("range-slider");
        setFocusTraversable(false);
        this.min.set(min);
        this.max.set(Math.max(min, max));
        this.highValue.set(Math.clamp(high, min, Math.max(min, max)));
        this.lowValue.set(Math.clamp(low, min, this.highValue.get()));
        this.min.addListener(o -> keepOrdered());
        this.max.addListener(o -> keepOrdered());
    }

    private void keepOrdered() {
        if (getMax() < getMin()) {
            setMax(getMin());
        }
        highValue.set(Math.clamp(getHighValue(), getMin(), getMax()));
        lowValue.set(Math.clamp(getLowValue(), getMin(), getHighValue()));
    }

    /** Sets the low value, snapped if requested and clamped to [min, high]. */
    public void adjustLowValue(double value) {
        lowValue.set(Math.clamp(snap(value), getMin(), getHighValue()));
    }

    /** Sets the high value, snapped if requested and clamped to [low, max]. */
    public void adjustHighValue(double value) {
        highValue.set(Math.clamp(snap(value), getLowValue(), getMax()));
    }

    private double snap(double value) {
        if (!isSnapToTicks() || getMajorTickUnit() <= 0) {
            return value;
        }
        double unit = getMajorTickUnit();
        return getMin() + Math.round((value - getMin()) / unit) * unit;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new RangeSliderSkin(this);
    }

    public final DoubleProperty minProperty() {
        return min;
    }

    public final double getMin() {
        return min.get();
    }

    public final void setMin(double value) {
        min.set(value);
    }

    public final DoubleProperty maxProperty() {
        return max;
    }

    public final double getMax() {
        return max.get();
    }

    public final void setMax(double value) {
        max.set(value);
    }

    /** Lower bound of the selected range. Prefer {@link #adjustLowValue(double)}, which keeps the order. */
    public final DoubleProperty lowValueProperty() {
        return lowValue;
    }

    public final double getLowValue() {
        return lowValue.get();
    }

    public final void setLowValue(double value) {
        adjustLowValue(value);
    }

    /** Upper bound of the selected range. Prefer {@link #adjustHighValue(double)}, which keeps the order. */
    public final DoubleProperty highValueProperty() {
        return highValue;
    }

    public final double getHighValue() {
        return highValue.get();
    }

    public final void setHighValue(double value) {
        adjustHighValue(value);
    }

    public final DoubleProperty blockIncrementProperty() {
        return blockIncrement;
    }

    public final double getBlockIncrement() {
        return blockIncrement.get();
    }

    public final void setBlockIncrement(double value) {
        blockIncrement.set(value);
    }

    public final DoubleProperty majorTickUnitProperty() {
        return majorTickUnit;
    }

    public final double getMajorTickUnit() {
        return majorTickUnit.get();
    }

    public final void setMajorTickUnit(double value) {
        majorTickUnit.set(value);
    }

    public final BooleanProperty snapToTicksProperty() {
        return snapToTicks;
    }

    public final boolean isSnapToTicks() {
        return snapToTicks.get();
    }

    public final void setSnapToTicks(boolean value) {
        snapToTicks.set(value);
    }

    /** Shows a dot at every major tick on the track (Material discrete slider). */
    public final BooleanProperty showTickMarksProperty() {
        return showTickMarks;
    }

    public final boolean isShowTickMarks() {
        return showTickMarks.get();
    }

    public final void setShowTickMarks(boolean value) {
        showTickMarks.set(value);
    }

    /** True while the low thumb is being dragged, like {@code Slider.valueChanging}. */
    public final BooleanProperty lowValueChangingProperty() {
        return lowValueChanging;
    }

    public final boolean isLowValueChanging() {
        return lowValueChanging.get();
    }

    public final BooleanProperty highValueChangingProperty() {
        return highValueChanging;
    }

    public final boolean isHighValueChanging() {
        return highValueChanging.get();
    }
}
