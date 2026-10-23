package io.github.squoirelle.material3fx.skins;

import io.github.squoirelle.material3fx.Motion;
import io.github.squoirelle.material3fx.controls.StatusLight;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.util.Duration;

/**
 * Skin of {@link StatusLight}: dot, optional label, and a pulsing ring. The ring is unmanaged and
 * only its scale and opacity animate, so pulsing never triggers layout.
 */
public class StatusLightSkin extends SkinBase<StatusLight> {

    private static final double DOT = 10;
    private static final double GAP = 8;

    private final Region dot = new Region();
    private final Region ring = new Region();
    private final Label label = new Label();
    private final Timeline pulse = new Timeline();

    public StatusLightSkin(StatusLight light) {
        super(light);
        dot.getStyleClass().add("status-light-dot");
        ring.getStyleClass().add("status-light-ring");
        label.getStyleClass().add("status-light-label");
        ring.setManaged(false);
        ring.setMouseTransparent(true);
        ring.setOpacity(0);
        label.textProperty().bind(light.textProperty());
        getChildren().addAll(ring, dot, label);
        pulse.getKeyFrames().setAll(
                new KeyFrame(Duration.ZERO, new KeyValue(ring.scaleXProperty(), 1), new KeyValue(ring.scaleYProperty(), 1),
                        new KeyValue(ring.opacityProperty(), 0.6)),
                new KeyFrame(Duration.millis(1400), new KeyValue(ring.scaleXProperty(), 2.6, Interpolator.EASE_OUT),
                        new KeyValue(ring.scaleYProperty(), 2.6, Interpolator.EASE_OUT),
                        new KeyValue(ring.opacityProperty(), 0, Interpolator.EASE_OUT)));
        pulse.setCycleCount(Animation.INDEFINITE);
        registerChangeListener(light.pulsingProperty(), o -> updatePulse());
        registerChangeListener(light.sceneProperty(), o -> updatePulse());
        registerChangeListener(light.visibleProperty(), o -> updatePulse());
        updatePulse();
    }

    private void updatePulse() {
        StatusLight light = getSkinnable();
        boolean run = light.isPulsing() && light.isVisible() && light.getScene() != null && Motion.isEnabled();
        if (run && pulse.getStatus() != Animation.Status.RUNNING) {
            pulse.playFromStart();
        } else if (!run) {
            pulse.stop();
            ring.setOpacity(0);
        }
    }

    private boolean hasLabel() {
        String text = getSkinnable().getText();
        return text != null && !text.isEmpty();
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        double dotY = y + (h - DOT) / 2;
        dot.resizeRelocate(x, dotY, DOT, DOT);
        ring.resizeRelocate(x, dotY, DOT, DOT);
        label.setVisible(hasLabel());
        if (hasLabel()) {
            layoutInArea(label, x + DOT + GAP, y, Math.max(0, w - DOT - GAP), h, 0, HPos.LEFT, VPos.CENTER);
        }
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return left + DOT + (hasLabel() ? GAP + label.prefWidth(-1) : 0) + right;
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        return top + Math.max(DOT, hasLabel() ? label.prefHeight(-1) : 0) + bottom;
    }

    @Override
    protected double computeMinWidth(double height, double top, double right, double bottom, double left) {
        return left + DOT + right;
    }

    @Override
    protected double computeMaxWidth(double height, double top, double right, double bottom, double left) {
        return computePrefWidth(height, top, right, bottom, left);
    }

    @Override
    protected double computeMaxHeight(double width, double top, double right, double bottom, double left) {
        return computePrefHeight(width, top, right, bottom, left);
    }

    @Override
    public void dispose() {
        pulse.stop();
        label.textProperty().unbind();
        super.dispose();
    }
}
