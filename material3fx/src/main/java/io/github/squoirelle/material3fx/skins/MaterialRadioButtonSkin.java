package io.github.squoirelle.material3fx.skins;

import io.github.squoirelle.material3fx.controls.StateLayer;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.skin.RadioButtonSkin;

/** Radio button skin with the circular Material state layer around the radio. */
public class MaterialRadioButtonSkin extends RadioButtonSkin {

    private final StateLayer stateLayer;

    public MaterialRadioButtonSkin(RadioButton radioButton) {
        super(radioButton);
        stateLayer = new StateLayer(radioButton, StateLayer.ClipShape.CIRCLE);
        stateLayer.getStyleClass().add("md-box-state-layer");
        getChildren().addFirst(stateLayer);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        SkinSupport.keepAtBottom(getChildren(), stateLayer);
        super.layoutChildren(x, y, w, h);
        Node radio = SkinSupport.findChild(getChildren(), "radio");
        if (radio != null) {
            Bounds b = radio.getBoundsInParent();
            double t = MaterialCheckBoxSkin.TARGET;
            stateLayer.resizeRelocate(b.getCenterX() - t / 2, b.getCenterY() - t / 2, t, t);
        }
    }
}
