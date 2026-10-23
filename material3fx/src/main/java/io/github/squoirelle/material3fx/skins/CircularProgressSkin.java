package io.github.squoirelle.material3fx.skins;

import javafx.animation.AnimationTimer;
import javafx.beans.InvalidationListener;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SkinBase;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;

/**
 * Material 3 circular progress indicator: an active arc over a track, separated by gaps.
 * Indeterminate mode uses the Material expanding / collapsing arc.
 * <p>
 * Size comes from the control's pref size ({@code -fx-pref-width}); the stroke width is
 * {@code -fx-stroke-width} on {@code .md-progress-indicator}.
 */
public class CircularProgressSkin extends SkinBase<ProgressIndicator> {

    private static final double CYCLE_NANOS = 1_333_000_000.0;
    private static final double GAP_DEGREES = 14;

    private final Arc track = new Arc();
    private final Arc indicator = new Arc();
    private final AnimationTimer timer;
    private double phase;
    private long last = -1;

    public CircularProgressSkin(ProgressIndicator control) {
        super(control);
        for (Arc arc : new Arc[] {track, indicator}) {
            arc.setType(ArcType.OPEN);
            arc.setFill(null);
            arc.setStrokeLineCap(StrokeLineCap.ROUND);
            arc.setManaged(false);
        }
        track.getStyleClass().add("md-progress-track");
        indicator.getStyleClass().add("md-progress-indicator");
        getChildren().addAll(track, indicator);

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (last > 0) {
                    phase += (now - last) / CYCLE_NANOS;
                }
                last = now;
                updateArcs();
            }
        };
        InvalidationListener runningListener = o -> updateRunning();
        control.indeterminateProperty().addListener(runningListener);
        control.sceneProperty().addListener(runningListener);
        control.visibleProperty().addListener(runningListener);
        control.progressProperty().addListener(o -> updateArcs());
        updateRunning();
    }

    private void updateRunning() {
        ProgressIndicator c = getSkinnable();
        if (c.isIndeterminate() && c.getScene() != null && c.isVisible()) {
            last = -1;
            timer.start();
        } else {
            timer.stop();
        }
        updateArcs();
    }

    private void updateArcs() {
        ProgressIndicator c = getSkinnable();
        double start;
        double length;
        if (c.isIndeterminate()) {
            // Head and tail chase each other: the head leads during the first half of the cycle,
            // the tail catches up during the second half, while the whole arc keeps rotating.
            double p = phase % 1.0;
            double head = ease(Math.min(1, p * 2)) * 270;
            double tail = ease(Math.max(0, p * 2 - 1)) * 270;
            double rotation = (phase * 286) % 360 + (Math.floor(phase) * 270) % 360;
            start = 90 - rotation - tail;
            length = -Math.max(head - tail, 12);
            track.setVisible(false);
        } else {
            double progress = Math.clamp(c.getProgress(), 0, 1);
            start = 90;
            length = -360 * progress;
            track.setVisible(true);
            double trackStart = progress == 0 ? 90 : 90 - 360 * progress - GAP_DEGREES;
            double trackLength = progress == 0 ? 360 : -(360 * (1 - progress) - 2 * GAP_DEGREES);
            track.setStartAngle(trackStart);
            track.setLength(Math.min(0, trackLength));
            track.setVisible(progress < 1 && trackLength < 0 || progress == 0);
        }
        indicator.setStartAngle(start);
        indicator.setLength(length);
        indicator.setVisible(c.isIndeterminate() || c.getProgress() > 0);
    }

    private static double ease(double t) {
        // Material "standard" easing approximation.
        return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        double stroke = indicator.getStrokeWidth();
        double radius = Math.max(1, (Math.min(w, h) - stroke) / 2);
        for (Arc arc : new Arc[] {track, indicator}) {
            arc.setCenterX(x + w / 2);
            arc.setCenterY(y + h / 2);
            arc.setRadiusX(radius);
            arc.setRadiusY(radius);
        }
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return 48 + left + right;
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        return 48 + top + bottom;
    }

    @Override
    protected double computeMaxWidth(double height, double top, double right, double bottom, double left) {
        return getSkinnable().prefWidth(height);
    }

    @Override
    protected double computeMinHeight(double width, double top, double right, double bottom, double left) {
        return getSkinnable().prefHeight(width);
    }

    @Override
    protected double computeMaxHeight(double width, double top, double right, double bottom, double left) {
        return getSkinnable().prefHeight(width);
    }

    @Override
    public void dispose() {
        timer.stop();
        super.dispose();
    }
}
