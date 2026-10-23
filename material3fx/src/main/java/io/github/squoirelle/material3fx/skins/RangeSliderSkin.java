package io.github.squoirelle.material3fx.skins;

import io.github.squoirelle.material3fx.controls.RangeSlider;
import io.github.squoirelle.material3fx.controls.StateLayer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * Skin of {@link RangeSlider}: track, active segment between the thumbs, optional tick dots, two
 * focusable thumbs with state layers, and a value label above the dragged thumb.
 */
public class RangeSliderSkin extends SkinBase<RangeSlider> {

    private static final double THUMB = 20;
    private static final double TRACK = 4;
    private static final PseudoClass PRESSED = PseudoClass.getPseudoClass("pressed");

    private final Region track = new Region();
    private final Region active = new Region();
    private final Thumb low;
    private final Thumb high;
    private final Label valueLabel = new Label();
    private final List<Region> ticks = new ArrayList<>();
    private Thumb dragging;

    public RangeSliderSkin(RangeSlider slider) {
        super(slider);
        track.getStyleClass().add("range-slider-track");
        active.getStyleClass().add("range-slider-active-track");
        valueLabel.getStyleClass().add("md-value-label");
        for (Node n : List.of(track, active, valueLabel)) {
            n.setManaged(false);
            n.setMouseTransparent(true);
        }
        valueLabel.setVisible(false);
        low = new Thumb(slider.lowValueProperty(), slider.lowValueChangingProperty(), true);
        high = new Thumb(slider.highValueProperty(), slider.highValueChangingProperty(), false);
        getChildren().addAll(track, active);
        getChildren().addAll(low.layer, high.layer, low, high, valueLabel);

        slider.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onPressed);
        slider.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onDragged);
        slider.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onReleased);
        for (var p : List.of(slider.lowValueProperty(), slider.highValueProperty(), slider.minProperty(), slider.maxProperty())) {
            registerChangeListener(p, o -> slider.requestLayout());
        }
        registerChangeListener(slider.showTickMarksProperty(), o -> rebuildTicks());
        registerChangeListener(slider.majorTickUnitProperty(), o -> rebuildTicks());
        rebuildTicks();
    }

    private void rebuildTicks() {
        getChildren().removeAll(ticks);
        ticks.clear();
        RangeSlider s = getSkinnable();
        if (s.isShowTickMarks() && s.getMajorTickUnit() > 0) {
            int count = (int) Math.floor((s.getMax() - s.getMin()) / s.getMajorTickUnit());
            for (int i = 0; i <= Math.min(count, 200); i++) {
                Region tick = new Region();
                tick.getStyleClass().add("range-slider-tick");
                tick.setManaged(false);
                tick.setMouseTransparent(true);
                ticks.add(tick);
            }
            getChildren().addAll(2, ticks);
        }
        s.requestLayout();
    }

    private double trackStart() {
        return getSkinnable().snappedLeftInset() + THUMB / 2;
    }

    private double trackLength() {
        RangeSlider s = getSkinnable();
        return Math.max(1, s.getWidth() - s.snappedLeftInset() - s.snappedRightInset() - THUMB);
    }

    private double toPosition(double value) {
        RangeSlider s = getSkinnable();
        double span = s.getMax() - s.getMin();
        return trackStart() + (span <= 0 ? 0 : (value - s.getMin()) / span * trackLength());
    }

    private double toValue(double x) {
        RangeSlider s = getSkinnable();
        double ratio = Math.clamp((x - trackStart()) / trackLength(), 0, 1);
        return s.getMin() + ratio * (s.getMax() - s.getMin());
    }

    private void onPressed(MouseEvent e) {
        if (getSkinnable().isDisabled()) {
            return;
        }
        double lowX = toPosition(getSkinnable().getLowValue());
        double highX = toPosition(getSkinnable().getHighValue());
        // The nearest thumb; when both are at the same spot, the one that can move that way.
        if (Math.abs(e.getX() - lowX) < Math.abs(e.getX() - highX)) {
            dragging = low;
        } else if (Math.abs(e.getX() - lowX) > Math.abs(e.getX() - highX)) {
            dragging = high;
        } else {
            dragging = e.getX() < lowX ? low : high;
        }
        dragging.requestFocus();
        dragging.changing.set(true);
        dragging.pseudoClassStateChanged(PRESSED, true);
        dragging.set(toValue(e.getX()));
        e.consume();
    }

    private void onDragged(MouseEvent e) {
        if (dragging != null) {
            dragging.set(toValue(e.getX()));
            e.consume();
        }
    }

    private void onReleased(MouseEvent e) {
        if (dragging != null) {
            dragging.changing.set(false);
            dragging.pseudoClassStateChanged(PRESSED, false);
            dragging = null;
            getSkinnable().requestLayout();
        }
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        RangeSlider s = getSkinnable();
        double cy = y + h / 2;
        double start = trackStart();
        double end = start + trackLength();
        track.resizeRelocate(start, cy - TRACK / 2, end - start, TRACK);
        double lowX = toPosition(s.getLowValue());
        double highX = toPosition(s.getHighValue());
        active.resizeRelocate(lowX, cy - TRACK / 2, Math.max(0, highX - lowX), TRACK);
        for (int i = 0; i < ticks.size(); i++) {
            double tx = toPosition(s.getMin() + i * s.getMajorTickUnit());
            ticks.get(i).resizeRelocate(tx - 1, cy - 1, 2, 2);
        }
        low.resizeRelocate(lowX - THUMB / 2, cy - THUMB / 2, THUMB, THUMB);
        high.resizeRelocate(highX - THUMB / 2, cy - THUMB / 2, THUMB, THUMB);
        low.layer.resizeRelocate(lowX - 20, cy - 20, 40, 40);
        high.layer.resizeRelocate(highX - 20, cy - 20, 40, 40);

        Thumb shown = low.changing.get() ? low : high.changing.get() ? high : null;
        valueLabel.setVisible(shown != null);
        if (shown != null) {
            valueLabel.setText(format(shown.value.get()));
            double lw = Math.max(28, valueLabel.prefWidth(-1));
            double lh = valueLabel.prefHeight(lw);
            double tx = shown == low ? lowX : highX;
            valueLabel.resizeRelocate(tx - lw / 2, cy - THUMB / 2 - lh - 8, lw, lh);
        }
    }

    private static String format(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.format("%.1f", value);
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return left + 240 + right;
    }

    /** Baseline on the track, so a label next to the slider (e.g. in a form row) lines up with it. */
    @Override
    public double computeBaselineOffset(double top, double right, double bottom, double left) {
        return top + (getSkinnable().getHeight() > 0 ? getSkinnable().getHeight() - top - bottom : 40) / 2 + 5;
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        return top + 40 + bottom;
    }

    @Override
    protected double computeMinWidth(double height, double top, double right, double bottom, double left) {
        return left + THUMB * 3 + right;
    }

    @Override
    protected double computeMinHeight(double width, double top, double right, double bottom, double left) {
        return computePrefHeight(width, top, right, bottom, left);
    }

    @Override
    protected double computeMaxHeight(double width, double top, double right, double bottom, double left) {
        return computePrefHeight(width, top, right, bottom, left);
    }

    /** A focusable thumb, reported to accessibility as a slider for its value. */
    private final class Thumb extends Region {
        final DoubleProperty value;
        final BooleanProperty changing;
        final boolean isLow;
        final StateLayer layer;

        Thumb(DoubleProperty value, BooleanProperty changing, boolean isLow) {
            this.value = value;
            this.changing = changing;
            this.isLow = isLow;
            getStyleClass().addAll("range-slider-thumb", isLow ? "low" : "high");
            setFocusTraversable(true);
            setManaged(false);
            setAccessibleRole(AccessibleRole.SLIDER);
            setAccessibleText(isLow ? "Minimum" : "Maximum");
            layer = new StateLayer(this, StateLayer.ClipShape.CIRCLE);
            layer.getStyleClass().add("range-slider-state-layer");
            layer.setRippleEnabled(false);
            hoverProperty().addListener(o -> layer.setForceHover(isHover()));
            value.addListener(o -> notifyAccessibleAttributeChanged(AccessibleAttribute.VALUE));
            addEventHandler(KeyEvent.KEY_PRESSED, this::onKey);
        }

        void set(double v) {
            if (isLow) {
                getSkinnable().adjustLowValue(v);
            } else {
                getSkinnable().adjustHighValue(v);
            }
        }

        private void onKey(KeyEvent e) {
            RangeSlider s = getSkinnable();
            double step = s.getBlockIncrement();
            double page = (s.getMax() - s.getMin()) / 10;
            switch (e.getCode()) {
                case RIGHT, UP, KP_RIGHT, KP_UP -> set(value.get() + step);
                case LEFT, DOWN, KP_LEFT, KP_DOWN -> set(value.get() - step);
                case PAGE_UP -> set(value.get() + page);
                case PAGE_DOWN -> set(value.get() - page);
                case HOME -> set(s.getMin());
                case END -> set(s.getMax());
                default -> {
                    return;
                }
            }
            e.consume();
        }

        @Override
        public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
            RangeSlider s = getSkinnable();
            return switch (attribute) {
                case VALUE -> value.get();
                case MIN_VALUE -> isLow ? s.getMin() : s.getLowValue();
                case MAX_VALUE -> isLow ? s.getHighValue() : s.getMax();
                default -> super.queryAccessibleAttribute(attribute, parameters);
            };
        }

        @Override
        public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
            switch (action) {
                case INCREMENT -> set(value.get() + getSkinnable().getBlockIncrement());
                case DECREMENT -> set(value.get() - getSkinnable().getBlockIncrement());
                case SET_VALUE -> set((Double) parameters[0]);
                default -> super.executeAccessibleAction(action, parameters);
            }
        }
    }
}
