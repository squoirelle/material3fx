package dev.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Material navigation drawer. Use it inline in a layout (standard drawer), or show it over the
 * content with {@link #showModal(Node)} (modal drawer, with a scrim, closed by Escape, a click
 * outside or a selection).
 * <p>
 * Besides destinations, it renders {@link NavigationItem#headline(String) headlines} and
 * {@link NavigationItem#divider() dividers}.
 */
public class NavigationDrawer extends NavigationView {

    private final VBox box = new VBox();
    private final VBox itemsBox = new VBox();
    private final ScrollPane scroll = new ScrollPane(box);
    private final Label titleLabel = new Label();
    private final StringProperty title = titleLabel.textProperty();
    private final ObjectProperty<Node> header = new SimpleObjectProperty<>(this, "header");
    private Overlay modalOverlay;

    public NavigationDrawer() {
        getStyleClass().add("navigation-drawer");
        box.getStyleClass().add("navigation-drawer-content");
        itemsBox.getStyleClass().add("navigation-drawer-items");
        titleLabel.getStyleClass().add("navigation-drawer-title");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("navigation-drawer-scroll");
        title.addListener(o -> rebuildBox());
        header.addListener(o -> rebuildBox());
        rebuildBox();
        getChildren().add(scroll);
        selectedItemProperty().addListener(o -> {
            if (modalOverlay != null) {
                modalOverlay.hide();
            }
        });
    }

    private void rebuildBox() {
        box.getChildren().clear();
        if (getHeader() != null) {
            box.getChildren().add(getHeader());
        }
        if (getTitle() != null && !getTitle().isEmpty()) {
            box.getChildren().add(titleLabel);
        }
        box.getChildren().add(itemsBox);
    }

    /** Shows this drawer as a modal drawer over the content of {@code owner}'s window. */
    public Overlay showModal(Node owner) {
        getStyleClass().add("modal");
        modalOverlay = Overlay.show(owner, this, Overlay.Position.START, true, true);
        modalOverlay.setOnHidden(() -> {
            getStyleClass().remove("modal");
            modalOverlay = null;
        });
        return modalOverlay;
    }

    /** Hides the modal drawer, if shown. */
    public void hideModal() {
        if (modalOverlay != null) {
            modalOverlay.hide();
        }
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
            case DIVIDER -> {
                Divider divider = new Divider();
                divider.getStyleClass().add("navigation-drawer-divider");
                yield divider;
            }
            case DESTINATION -> {
                ItemNode node = new ItemNode(item, null);
                node.getStyleClass().add("drawer-item");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Label badgeLabel = new Label();
                badgeLabel.getStyleClass().add("navigation-drawer-badge");
                badgeLabel.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(() -> {
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
                node.getChildren().addAll(node.indicator, content);
                node.indicator.setMaxWidth(Double.MAX_VALUE);
                node.indicator.setMaxHeight(Double.MAX_VALUE);
                yield node;
            }
        };
    }

    @Override
    protected void setItemNodes(List<Node> nodes) {
        // A divider above every headline except a leading one, unless the items already have one.
        java.util.List<Node> withDividers = new java.util.ArrayList<>();
        for (Node node : nodes) {
            boolean headline = node.getStyleClass().contains("navigation-drawer-headline");
            boolean afterDivider = !withDividers.isEmpty()
                    && withDividers.getLast().getStyleClass().contains("navigation-drawer-divider");
            if (headline && !withDividers.isEmpty() && !afterDivider) {
                Divider divider = new Divider();
                divider.getStyleClass().add("navigation-drawer-divider");
                VBox.setMargin(divider, new javafx.geometry.Insets(8, 0, 0, 0));
                withDividers.add(divider);
            }
            withDividers.add(node);
        }
        itemsBox.getChildren().setAll(withDividers);
    }

    @Override
    protected void layoutChildren() {
        scroll.resizeRelocate(snappedLeftInset(), snappedTopInset(),
                getWidth() - snappedLeftInset() - snappedRightInset(),
                getHeight() - snappedTopInset() - snappedBottomInset());
    }

    @Override
    protected double computePrefWidth(double height) {
        return 360;
    }

    @Override
    protected double computePrefHeight(double width) {
        return snappedTopInset() + box.prefHeight(-1) + snappedBottomInset();
    }

    /** Optional title shown above the items (e.g. the app name). */
    public final StringProperty titleProperty() {
        return title;
    }

    public final String getTitle() {
        return title.get();
    }

    public final void setTitle(String value) {
        title.set(value);
    }

    /** Optional header node shown above the title. */
    public final ObjectProperty<Node> headerProperty() {
        return header;
    }

    public final Node getHeader() {
        return header.get();
    }

    public final void setHeader(Node value) {
        header.set(value);
    }

}
