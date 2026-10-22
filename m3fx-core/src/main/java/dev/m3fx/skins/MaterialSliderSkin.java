package dev.m3fx.skins;

import dev.m3fx.controls.StateLayer;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.skin.SliderSkin;
import javafx.scene.layout.Region;

/**
 * Slider skin adding the Material active track (primary color from the start to the thumb),
 * a state layer around the thumb, and a value label shown while dragging.
 */
public class MaterialSliderSkin extends SliderSkin {

    private final Region activeTrack = new Region();
    private final Label valueLabel = new Label();
    private StateLayer thumbLayer;

    public MaterialSliderSkin(Slider slider) {
        super(slider);
        activeTrack.getStyleClass().add("md-active-track");
        activeTrack.setManaged(false);
        activeTrack.setMouseTransparent(true);
        valueLabel.getStyleClass().add("md-value-label");
        valueLabel.setManaged(false);
        valueLabel.setMouseTransparent(true);
        valueLabel.setVisible(false);
        slider.valueProperty().addListener(o -> updateLabel());
        slider.valueChangingProperty().addListener(o -> valueLabel.setVisible(slider.isValueChanging()));
        updateLabel();
    }

    private void updateLabel() {
        double value = getSkinnable().getValue();
        valueLabel.setText(value == Math.rint(value) ? String.valueOf((long) value) : String.format("%.1f", value));
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        Node track = SkinSupport.findChild(getChildren(), "track");
        Node thumb = SkinSupport.findChild(getChildren(), "thumb");
        if (track == null || thumb == null) {
            return;
        }
        if (thumbLayer == null && thumb instanceof Region thumbRegion) {
            thumbLayer = new StateLayer(thumbRegion, StateLayer.ClipShape.CIRCLE);
            thumbLayer.getStyleClass().add("md-thumb-state-layer");
            thumbLayer.setRippleEnabled(false);
        }
        if (!getChildren().contains(activeTrack)) {
            int trackIndex = getChildren().indexOf(track);
            getChildren().add(trackIndex + 1, activeTrack);
        }
        if (thumbLayer != null && !getChildren().contains(thumbLayer)) {
            getChildren().add(getChildren().indexOf(thumb), thumbLayer);
        }
        if (!getChildren().contains(valueLabel)) {
            getChildren().add(valueLabel);
        }
        Bounds t = track.getBoundsInParent();
        Bounds th = thumb.getBoundsInParent();
        if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
            activeTrack.resizeRelocate(t.getMinX(), t.getMinY(), Math.max(0, th.getCenterX() - t.getMinX()), t.getHeight());
        } else {
            activeTrack.resizeRelocate(t.getMinX(), th.getCenterY(), t.getWidth(), Math.max(0, t.getMaxY() - th.getCenterY()));
        }
        if (thumbLayer != null) {
            double size = 40;
            thumbLayer.resizeRelocate(th.getCenterX() - size / 2, th.getCenterY() - size / 2, size, size);
        }
        double lw = Math.max(28, valueLabel.prefWidth(-1));
        double lh = valueLabel.prefHeight(lw);
        valueLabel.resizeRelocate(th.getCenterX() - lw / 2, th.getMinY() - lh - 8, lw, lh);
    }
}
