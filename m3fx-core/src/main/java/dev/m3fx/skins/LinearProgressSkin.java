package dev.m3fx.skins;

import javafx.animation.AnimationTimer;
import javafx.beans.InvalidationListener;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;

/**
 * Material 3 linear progress indicator: active indicator, gap, track and stop dot.
 * Indeterminate mode runs two bars across the track like the Material spec.
 */
public class LinearProgressSkin extends SkinBase<ProgressBar> {

    private static final double GAP = 4;
    private static final double CYCLE_NANOS = 1_800_000_000.0;

    private final Region trackStart = new Region();
    private final Region track = new Region();
    private final Region trackEnd = new Region();
    private final Region bar1 = new Region();
    private final Region bar2 = new Region();
    private final Region stop = new Region();
    private final AnimationTimer timer;
    private double phase;
    private long last = -1;

    public LinearProgressSkin(ProgressBar control) {
        super(control);
        track.getStyleClass().add("md-progress-track");
        trackStart.getStyleClass().add("md-progress-track");
        trackEnd.getStyleClass().add("md-progress-track");
        bar1.getStyleClass().add("md-progress-bar");
        bar2.getStyleClass().add("md-progress-bar");
        stop.getStyleClass().add("md-progress-stop");
        for (Region r : new Region[] {trackStart, track, trackEnd, bar1, bar2, stop}) {
            r.setManaged(false);
        }
        getChildren().addAll(trackStart, track, trackEnd, bar1, bar2, stop);

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (last > 0) {
                    phase += (now - last) / CYCLE_NANOS;
                }
                last = now;
                getSkinnable().requestLayout();
            }
        };
        InvalidationListener runningListener = o -> updateRunning();
        control.indeterminateProperty().addListener(runningListener);
        control.sceneProperty().addListener(runningListener);
        control.visibleProperty().addListener(runningListener);
        control.progressProperty().addListener(o -> control.requestLayout());
        updateRunning();
    }

    private void updateRunning() {
        ProgressBar c = getSkinnable();
        if (c.isIndeterminate() && c.getScene() != null && c.isVisible()) {
            last = -1;
            timer.start();
        } else {
            timer.stop();
        }
        c.requestLayout();
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        ProgressBar c = getSkinnable();
        if (c.isIndeterminate()) {
            stop.setVisible(false);
            double p = phase % 1.0;
            // Bar 1: long bar sweeping across; bar 2: shorter, faster bar following it.
            double[] s1 = segment(p, 0.0, 0.75, -0.35, 1.0, 0.35, 0.9);
            double[] s2 = segment(p, 0.4, 1.0, -0.6, 1.0, 0.15, 0.55);
            layoutBar(bar1, x, y, w, h, s1);
            layoutBar(bar2, x, y, w, h, s2);
            // The track fills the space around the bars, with a gap on each side of every bar.
            java.util.List<double[]> bars = new java.util.ArrayList<>();
            for (double[] seg : new double[][] {s1, s2}) {
                double from = Math.clamp(seg[0], 0, 1);
                double to = Math.clamp(seg[1], 0, 1);
                if (to - from > 0.001) {
                    bars.add(new double[] {from, to});
                }
            }
            bars.sort(java.util.Comparator.comparingDouble(b -> b[0]));
            Region[] pieces = {trackStart, track, trackEnd};
            double cursor = 0;
            int piece = 0;
            for (double[] bar : bars) {
                layoutTrack(pieces[piece++], x, y, w, h, cursor, bar[0]);
                cursor = Math.max(cursor, bar[1]);
            }
            layoutTrack(pieces[piece++], x, y, w, h, cursor, 1);
            while (piece < pieces.length) {
                pieces[piece++].setVisible(false);
            }
        } else {
            bar2.setVisible(false);
            double progress = Math.clamp(c.getProgress(), 0, 1);
            double barWidth = w * progress;
            bar1.setVisible(progress > 0);
            bar1.resizeRelocate(x, y, barWidth, h);
            trackStart.setVisible(false);
            trackEnd.setVisible(false);
            double trackX = progress > 0 ? barWidth + GAP : 0;
            track.setVisible(trackX < w);
            track.resizeRelocate(x + trackX, y, Math.max(0, w - trackX), h);
            stop.setVisible(progress < 1);
            stop.resizeRelocate(x + w - h, y, h, h);
        }
    }

    private void layoutBar(Region bar, double x, double y, double w, double h, double[] s) {
        double from = Math.clamp(s[0], 0, 1);
        double to = Math.clamp(s[1], 0, 1);
        bar.setVisible(to - from > 0.001);
        bar.resizeRelocate(x + from * w, y, (to - from) * w, h);
    }

    private void layoutTrack(Region r, double x, double y, double w, double h, double from, double to) {
        double start = Math.clamp(from, 0, 1) * w + (from > 0 ? GAP : 0);
        double end = Math.clamp(to, 0, 1) * w - (to < 1 ? GAP : 0);
        r.setVisible(end - start > 1);
        r.resizeRelocate(x + start, y, Math.max(0, end - start), h);
    }

    /** A bar moving from {@code startPos} to {@code endPos} between cycle times {@code t0} and {@code t1}. */
    private static double[] segment(double p, double t0, double t1, double startPos, double endPos,
                                    double minLength, double maxLength) {
        if (p < t0 || p > t1) {
            return new double[] {-1, -1};
        }
        double t = (p - t0) / (t1 - t0);
        double eased = t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
        double length = minLength + (maxLength - minLength) * Math.sin(Math.PI * t);
        double head = startPos + (endPos - startPos + length) * eased;
        return new double[] {head - length, head};
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return 240 + left + right;
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        return 4 + top + bottom;
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
