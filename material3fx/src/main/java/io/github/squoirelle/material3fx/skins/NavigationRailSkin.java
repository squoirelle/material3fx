package io.github.squoirelle.material3fx.skins;

import io.github.squoirelle.material3fx.controls.Badge;
import io.github.squoirelle.material3fx.controls.NavigationItem;
import io.github.squoirelle.material3fx.controls.NavigationRail;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/** Skin of {@link NavigationRail}: header, then destinations stacked vertically. */
public class NavigationRailSkin extends NavigationViewSkin<NavigationRail> {

    private final VBox box = new VBox();
    private final VBox headerBox = new VBox();
    private final VBox itemsBox = new VBox();
    private final Region topSpacer = new Region();
    private final Region bottomSpacer = new Region();

    public NavigationRailSkin(NavigationRail control) {
        super(control);
        box.getStyleClass().add("navigation-rail-content");
        headerBox.getStyleClass().add("navigation-rail-header");
        itemsBox.getStyleClass().add("navigation-rail-items");
        box.setAlignment(Pos.TOP_CENTER);
        headerBox.setAlignment(Pos.TOP_CENTER);
        itemsBox.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);
        getChildren().add(box);
        registerChangeListener(control.headerProperty(), o -> rebuildBox());
        registerChangeListener(control.itemsAlignmentProperty(), o -> rebuildBox());
        rebuildBox();
        rebuild();
    }

    private void rebuildBox() {
        NavigationRail rail = getSkinnable();
        box.getChildren().clear();
        if (rail.getHeader() != null) {
            headerBox.getChildren().setAll(rail.getHeader());
            box.getChildren().add(headerBox);
        }
        VPos vpos = rail.getItemsAlignment().getVpos();
        if (vpos != VPos.TOP) {
            box.getChildren().add(topSpacer);
        }
        box.getChildren().add(itemsBox);
        if (vpos == VPos.CENTER) {
            box.getChildren().add(bottomSpacer);
        }
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
        return node;
    }

    @Override
    protected void setItemNodes(List<Node> nodes) {
        itemsBox.getChildren().setAll(nodes);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        box.resizeRelocate(x, y, w, h);
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return left + box.prefWidth(-1) + right;
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        return top + box.prefHeight(-1) + bottom;
    }
}
