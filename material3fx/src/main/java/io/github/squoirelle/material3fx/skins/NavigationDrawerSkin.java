package io.github.squoirelle.material3fx.skins;

import io.github.squoirelle.material3fx.controls.Divider;
import io.github.squoirelle.material3fx.controls.NavigationDrawer;
import io.github.squoirelle.material3fx.controls.NavigationItem;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Skin of {@link NavigationDrawer}: optional header and title, then destinations, headlines and
 * dividers in a scroll pane. Each headline after the first gets a divider above it.
 */
public class NavigationDrawerSkin extends NavigationViewSkin<NavigationDrawer> {

    private final VBox box = new VBox();
    private final VBox itemsBox = new VBox();
    private final ScrollPane scroll = new ScrollPane(box);
    private final Label titleLabel = new Label();

    public NavigationDrawerSkin(NavigationDrawer control) {
        super(control);
        box.getStyleClass().add("navigation-drawer-content");
        itemsBox.getStyleClass().add("navigation-drawer-items");
        titleLabel.getStyleClass().add("navigation-drawer-title");
        scroll.getStyleClass().add("navigation-drawer-scroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        titleLabel.textProperty().bind(control.titleProperty());
        getChildren().add(scroll);
        registerChangeListener(control.titleProperty(), o -> rebuildBox());
        registerChangeListener(control.headerProperty(), o -> rebuildBox());
        rebuildBox();
        rebuild();
    }

    private void rebuildBox() {
        NavigationDrawer drawer = getSkinnable();
        box.getChildren().clear();
        if (drawer.getHeader() != null) {
            box.getChildren().add(drawer.getHeader());
        }
        if (drawer.getTitle() != null && !drawer.getTitle().isEmpty()) {
            box.getChildren().add(titleLabel);
        }
        box.getChildren().add(itemsBox);
    }

    @Override
    protected Node createNode(NavigationItem item) {
        return switch (item.getKind()) {
            case HEADLINE -> {
                Label label = new Label();
                label.textProperty().bind(item.textProperty());
                label.getStyleClass().add("navigation-drawer-headline");
                yield label;
            }
            case DIVIDER -> divider();
            case DESTINATION -> {
                ItemNode node = new ItemNode(getSkinnable(), item);
                node.getStyleClass().add("drawer-item");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Label badgeLabel = new Label();
                badgeLabel.getStyleClass().add("navigation-drawer-badge");
                badgeLabel.textProperty().bind(Bindings.createStringBinding(() -> {
                    if (item.getBadgeText() != null) {
                        return item.getBadgeText();
                    }
                    return item.getBadgeCount() > 0 ? String.valueOf(item.getBadgeCount()) : "";
                }, item.badgeTextProperty(), item.badgeCountProperty()));
                HBox content = new HBox(12, node.icon, node.label, spacer, badgeLabel);
                content.setAlignment(Pos.CENTER_LEFT);
                content.getStyleClass().add("navigation-drawer-item-content");
                content.setMouseTransparent(true);
                // In the drawer the indicator is the whole item background.
                node.indicator.getChildren().add(node.stateLayer);
                node.indicator.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                node.getChildren().addAll(node.indicator, content);
                yield node;
            }
        };
    }

    private static Divider divider() {
        Divider divider = new Divider();
        divider.getStyleClass().add("navigation-drawer-divider");
        return divider;
    }

    @Override
    protected void setItemNodes(List<Node> nodes) {
        // A divider above every headline except a leading one, unless there already is one.
        List<Node> result = new ArrayList<>();
        for (Node node : nodes) {
            boolean headline = node.getStyleClass().contains("navigation-drawer-headline");
            boolean afterDivider = !result.isEmpty() && result.getLast().getStyleClass().contains("navigation-drawer-divider");
            if (headline && !result.isEmpty() && !afterDivider) {
                Divider divider = divider();
                VBox.setMargin(divider, new Insets(8, 0, 0, 0));
                result.add(divider);
            }
            result.add(node);
        }
        itemsBox.getChildren().setAll(result);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        scroll.resizeRelocate(x, y, w, h);
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return 360;
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        return top + box.prefHeight(-1) + bottom;
    }
}
