package dev.m3fx.skins;

import dev.m3fx.controls.Badge;
import dev.m3fx.controls.NavigationBar;
import dev.m3fx.controls.NavigationItem;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/** Skin of {@link NavigationBar}: equal-width destinations in a row, indicator above the label. */
public class NavigationBarSkin extends NavigationViewSkin<NavigationBar> {

    private final HBox box = new HBox();

    public NavigationBarSkin(NavigationBar control) {
        super(control);
        box.getStyleClass().add("navigation-bar-content");
        box.setAlignment(Pos.CENTER);
        getChildren().add(box);
        rebuild();
    }

    @Override
    protected Node createNode(NavigationItem item) {
        if (!item.isDestination()) {
            return null;
        }
        ItemNode node = new ItemNode(getSkinnable(), item);
        node.indicator.getChildren().addAll(node.stateLayer, Badge.attach(node.icon, node.badge));
        VBox content = new VBox(4, node.indicator, node.label);
        content.setAlignment(Pos.TOP_CENTER);
        content.setMouseTransparent(true);
        node.getChildren().add(content);
        node.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(node, Priority.ALWAYS);
        node.setMaxWidth(Double.MAX_VALUE);
        return node;
    }

    @Override
    protected void setItemNodes(List<Node> nodes) {
        box.getChildren().setAll(nodes);
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
}
