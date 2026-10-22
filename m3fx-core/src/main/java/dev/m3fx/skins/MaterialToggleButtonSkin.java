package dev.m3fx.skins;

import dev.m3fx.controls.StateLayer;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.skin.ToggleButtonSkin;

/** Toggle button skin adding the Material state layer and ripple. */
public class MaterialToggleButtonSkin extends ToggleButtonSkin {

    private final StateLayer stateLayer;

    public MaterialToggleButtonSkin(ToggleButton button) {
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
