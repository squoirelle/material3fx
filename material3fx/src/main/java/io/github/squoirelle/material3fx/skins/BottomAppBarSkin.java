package io.github.squoirelle.material3fx.skins;

import io.github.squoirelle.material3fx.controls.BottomAppBar;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/** Skin of {@link BottomAppBar}: actions, spacer, FAB. */
public class BottomAppBarSkin extends SkinBase<BottomAppBar> {

    private final HBox box = new HBox();
    private final HBox actionsBox = new HBox();
    private final Region spacer = new Region();
    private final ListChangeListener<Node> actionsListener = c -> actionsBox.getChildren().setAll(getSkinnable().getActions());

    public BottomAppBarSkin(BottomAppBar bar) {
        super(bar);
        box.getStyleClass().add("bottom-app-bar-content");
        actionsBox.getStyleClass().add("bottom-app-bar-actions");
        box.setAlignment(Pos.CENTER_LEFT);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.getActions().addListener(actionsListener);
        actionsBox.getChildren().setAll(bar.getActions());
        getChildren().add(box);
        registerChangeListener(bar.fabProperty(), o -> rebuild());
        rebuild();
    }

    private void rebuild() {
        box.getChildren().setAll(actionsBox, spacer);
        if (getSkinnable().getFab() != null) {
            box.getChildren().add(getSkinnable().getFab());
        }
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        box.resizeRelocate(x, y, w, h);
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        return top + box.prefHeight(-1) + bottom;
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return left + box.prefWidth(-1) + right;
    }

    @Override
    public void dispose() {
        getSkinnable().getActions().removeListener(actionsListener);
        super.dispose();
    }
}
