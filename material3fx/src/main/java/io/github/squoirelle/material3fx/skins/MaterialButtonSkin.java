package io.github.squoirelle.material3fx.skins;

import io.github.squoirelle.material3fx.controls.StateLayer;
import javafx.scene.control.Button;
import javafx.scene.control.skin.ButtonSkin;

/**
 * Button skin adding the Material state layer and ripple. Installed on every {@code .button}
 * by the Material3FX stylesheet.
 */
public class MaterialButtonSkin extends ButtonSkin {

    private final StateLayer stateLayer;

    public MaterialButtonSkin(Button button) {
        super(button);
        stateLayer = new StateLayer(button);
        getChildren().addFirst(stateLayer);
    }

    public StateLayer getStateLayer() {
        return stateLayer;
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        SkinSupport.keepAtBottom(getChildren(), stateLayer);
        super.layoutChildren(x, y, w, h);
        stateLayer.resizeRelocate(0, 0, getSkinnable().getWidth(), getSkinnable().getHeight());
    }
}
