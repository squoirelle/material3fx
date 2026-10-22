package dev.m3fx.skins;

import dev.m3fx.controls.StateLayer;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.skin.CheckBoxSkin;

/** Checkbox skin with the circular Material state layer around the box. */
public class MaterialCheckBoxSkin extends CheckBoxSkin {

    static final double TARGET = 40;

    private final StateLayer stateLayer;

    public MaterialCheckBoxSkin(CheckBox checkBox) {
        super(checkBox);
        stateLayer = new StateLayer(checkBox, StateLayer.ClipShape.CIRCLE);
        stateLayer.getStyleClass().add("md-box-state-layer");
        getChildren().addFirst(stateLayer);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        SkinSupport.keepAtBottom(getChildren(), stateLayer);
        super.layoutChildren(x, y, w, h);
        Node box = SkinSupport.findChild(getChildren(), "box");
        if (box != null) {
            Bounds b = box.getBoundsInParent();
            stateLayer.resizeRelocate(b.getCenterX() - TARGET / 2, b.getCenterY() - TARGET / 2, TARGET, TARGET);
        }
    }
}
