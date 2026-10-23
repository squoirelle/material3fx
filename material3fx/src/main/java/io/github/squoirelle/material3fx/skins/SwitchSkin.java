package io.github.squoirelle.material3fx.skins;

import io.github.squoirelle.material3fx.Motion;
import io.github.squoirelle.material3fx.controls.Icon;
import io.github.squoirelle.material3fx.controls.StateLayer;
import io.github.squoirelle.material3fx.controls.Switch;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.scene.control.skin.ToggleButtonSkin;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * Skin of {@link Switch}: label on the leading side, track and animated thumb on the trailing
 * side. Behavior (mouse, keyboard, toggle groups) comes from {@link ToggleButtonSkin}.
 */
public class SwitchSkin extends ToggleButtonSkin {

    private static final double TRACK_WIDTH = 52;
    private static final double TRACK_HEIGHT = 32;
    private static final double GAP = 16;
    private static final PseudoClass WITH_ICON = PseudoClass.getPseudoClass("with-icon");

    private final Pane track = new Pane();
    private final Region thumb = new Region();
    private final Icon icon = new Icon(null, 16);
    private final StateLayer stateLayer;
    private final DoubleProperty position = new SimpleDoubleProperty(this, "position");
    private final Timeline animation = new Timeline();

    public SwitchSkin(Switch control) {
        super(control);
        track.getStyleClass().add("switch-track");
        thumb.getStyleClass().add("switch-thumb");
        icon.getStyleClass().add("switch-icon");
        icon.setFollowTextFill(false);
        thumb.setManaged(false);
        icon.setManaged(false);
        track.setManaged(false);
        stateLayer = new StateLayer(control, StateLayer.ClipShape.CIRCLE);
        stateLayer.getStyleClass().add("switch-state-layer");
        track.getChildren().addAll(stateLayer, thumb, icon);
        getChildren().add(track);

        position.set(control.isSelected() ? 1 : 0);
        registerChangeListener(control.selectedProperty(), o -> {
            animateTo(control.isSelected() ? 1 : 0);
            updateIcon();
        });
        registerChangeListener(control.showIconsProperty(), o -> updateIcon());
        registerChangeListener(control.showOnlySelectedIconProperty(), o -> updateIcon());
        registerChangeListener(control.pressedProperty(), o -> control.requestLayout());
        position.addListener(o -> control.requestLayout());
        updateIcon();
    }

    /** LabeledSkinBase rebuilds its children when text or graphic change: keep the track. */
    @Override
    protected void updateChildren() {
        super.updateChildren();
        if (track != null && !getChildren().contains(track)) {
            getChildren().add(track);
        }
    }

    private void animateTo(double target) {
        animation.stop();
        if (!Motion.shouldAnimate(getSkinnable())) {
            position.set(target);
            return;
        }
        animation.getKeyFrames().setAll(new KeyFrame(Motion.MEDIUM_1, new KeyValue(position, target, Motion.EMPHASIZED)));
        animation.playFromStart();
    }

    private void updateIcon() {
        Switch control = (Switch) getSkinnable();
        boolean visible = control.isShowIcons() && (control.isSelected() || !control.isShowOnlySelectedIcon());
        icon.setVisible(visible);
        icon.setName(control.isSelected() ? "check" : "close");
        control.pseudoClassStateChanged(WITH_ICON, visible);
        control.requestLayout();
    }

    private double labelWidth(double height) {
        String text = getSkinnable().getText();
        boolean hasLabel = (text != null && !text.isEmpty()) || getSkinnable().getGraphic() != null;
        return hasLabel ? super.computePrefWidth(height, 0, 0, 0, 0) + GAP : 0;
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        // Text on the leading side, switch at the trailing edge (like a settings row).
        double labelSpace = Math.max(0, w - TRACK_WIDTH - GAP);
        String text = getSkinnable().getText();
        if ((text != null && !text.isEmpty()) || getSkinnable().getGraphic() != null) {
            super.layoutChildren(x, y, labelSpace, h);
        }
        double trackX = x + w - TRACK_WIDTH;
        double trackY = y + (h - TRACK_HEIGHT) / 2;
        track.resizeRelocate(trackX, trackY, TRACK_WIDTH, TRACK_HEIGHT);

        double p = position.get();
        double base = icon.isVisible() ? 24 : 16;
        boolean pressed = getSkinnable().isPressed() && !getSkinnable().isDisabled();
        double size = pressed ? 28 : base + (24 - base) * p;
        double cx = TRACK_HEIGHT / 2 + (TRACK_WIDTH - TRACK_HEIGHT) * p;
        double cy = TRACK_HEIGHT / 2;
        thumb.resizeRelocate(cx - size / 2, cy - size / 2, size, size);
        Bounds b = icon.getLayoutBounds();
        icon.relocate(cx - b.getWidth() / 2, cy - b.getHeight() / 2);
        stateLayer.resizeRelocate(cx - 20, cy - 20, 40, 40);
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return left + labelWidth(height) + TRACK_WIDTH + right;
    }

    @Override
    protected double computeMinWidth(double height, double top, double right, double bottom, double left) {
        return left + TRACK_WIDTH + right;
    }

    /** A labeled switch can grow, like a settings row: label at the start, switch at the end. */
    @Override
    protected double computeMaxWidth(double height, double top, double right, double bottom, double left) {
        String text = getSkinnable().getText();
        boolean labeled = (text != null && !text.isEmpty()) || getSkinnable().getGraphic() != null;
        return labeled ? Double.MAX_VALUE : computePrefWidth(height, top, right, bottom, left);
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        return Math.max(TRACK_HEIGHT, super.computePrefHeight(width, 0, 0, 0, 0)) + top + bottom;
    }

    @Override
    protected double computeMinHeight(double width, double top, double right, double bottom, double left) {
        return computePrefHeight(width, top, right, bottom, left);
    }

    @Override
    public void dispose() {
        animation.stop();
        getChildren().remove(track);
        super.dispose();
    }
}
