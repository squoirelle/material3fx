package dev.m3fx.controls;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Material navigation bar: 3 to 5 destinations at the bottom of compact windows.
 *
 * <pre>{@code
 * NavigationBar bar = new NavigationBar();
 * bar.getItems().addAll(new NavigationItem("Home", "home"), new NavigationItem("Search", "search"));
 * bar.selectedItemProperty().addListener((obs, old, item) -> showPage(item));
 * }</pre>
 */
public class NavigationBar extends NavigationView {

    private final HBox box = new HBox();

    public NavigationBar() {
        getStyleClass().add("navigation-bar");
        box.getStyleClass().add("navigation-bar-content");
        box.setAlignment(Pos.CENTER);
        box.setFillHeight(true);
        getChildren().add(box);
    }

    @Override
    protected Node createNode(NavigationItem item) {
        if (!item.isDestination()) {
            return null;
        }
        ItemNode node = new ItemNode(item, null);
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
    protected void layoutChildren() {
        box.resizeRelocate(snappedLeftInset(), snappedTopInset(),
                getWidth() - snappedLeftInset() - snappedRightInset(),
                getHeight() - snappedTopInset() - snappedBottomInset());
    }

    @Override
    protected double computePrefHeight(double width) {
        return snappedTopInset() + box.prefHeight(-1) + snappedBottomInset();
    }

    @Override
    protected double computePrefWidth(double height) {
        return snappedLeftInset() + box.prefWidth(-1) + snappedRightInset();
    }
}
