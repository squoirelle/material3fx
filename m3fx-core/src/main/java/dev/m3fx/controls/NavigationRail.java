package dev.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Material navigation rail: 3 to 7 destinations on the side of medium and expanded windows,
 * with an optional header (menu button, FAB) on top.
 */
public class NavigationRail extends NavigationView {

    private final VBox box = new VBox();
    private final VBox headerBox = new VBox();
    private final VBox itemsBox = new VBox();
    private final Region topSpacer = new Region();
    private final Region bottomSpacer = new Region();
    private final ObjectProperty<Node> header = new SimpleObjectProperty<>(this, "header");
    private final ObjectProperty<Pos> itemsAlignment = new SimpleObjectProperty<>(this, "itemsAlignment", Pos.TOP_CENTER);

    public NavigationRail() {
        getStyleClass().add("navigation-rail");
        box.getStyleClass().add("navigation-rail-content");
        headerBox.getStyleClass().add("navigation-rail-header");
        itemsBox.getStyleClass().add("navigation-rail-items");
        box.setAlignment(Pos.TOP_CENTER);
        headerBox.setAlignment(Pos.TOP_CENTER);
        itemsBox.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);
        header.addListener(o -> rebuildBox());
        itemsAlignment.addListener(o -> rebuildBox());
        rebuildBox();
        getChildren().add(box);
    }

    private void rebuildBox() {
        box.getChildren().clear();
        if (getHeader() != null) {
            headerBox.getChildren().setAll(getHeader());
            box.getChildren().add(headerBox);
        }
        Pos alignment = getItemsAlignment();
        if (alignment.getVpos() != javafx.geometry.VPos.TOP) {
            box.getChildren().add(topSpacer);
        }
        box.getChildren().add(itemsBox);
        if (alignment.getVpos() == javafx.geometry.VPos.CENTER) {
            box.getChildren().add(bottomSpacer);
        }
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
        return node;
    }

    @Override
    protected void setItemNodes(List<Node> nodes) {
        itemsBox.getChildren().setAll(nodes);
    }

    @Override
    protected void layoutChildren() {
        box.resizeRelocate(snappedLeftInset(), snappedTopInset(),
                getWidth() - snappedLeftInset() - snappedRightInset(),
                getHeight() - snappedTopInset() - snappedBottomInset());
    }

    @Override
    protected double computePrefWidth(double height) {
        return snappedLeftInset() + box.prefWidth(-1) + snappedRightInset();
    }

    @Override
    protected double computePrefHeight(double width) {
        return snappedTopInset() + box.prefHeight(-1) + snappedBottomInset();
    }

    /** Optional header: typically a menu {@link IconButton} and/or a {@link Fab}. */
    public final ObjectProperty<Node> headerProperty() {
        return header;
    }

    public final Node getHeader() {
        return header.get();
    }

    public final void setHeader(Node value) {
        header.set(value);
    }

    /** Vertical placement of the destinations: TOP_CENTER (default), CENTER or BOTTOM_CENTER. */
    public final ObjectProperty<Pos> itemsAlignmentProperty() {
        return itemsAlignment;
    }

    public final Pos getItemsAlignment() {
        return itemsAlignment.get();
    }

    public final void setItemsAlignment(Pos value) {
        itemsAlignment.set(value);
    }
}
