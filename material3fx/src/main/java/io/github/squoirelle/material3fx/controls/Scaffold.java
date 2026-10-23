package io.github.squoirelle.material3fx.controls;

import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

/**
 * Material app layout: top app bar, navigation (rail or standard drawer), content, bottom bar
 * (navigation bar or bottom app bar) and a floating action button.
 * <p>
 * The scaffold also hosts overlays: {@link MaterialDialog dialogs}, {@link BottomSheet bottom
 * sheets}, {@link SideSheet side sheets}, {@link Snackbar snackbars} and modal
 * {@link NavigationDrawer drawers} shown from any node inside it.
 */
@DefaultProperty("content")
public class Scaffold extends StackPane {

    public enum FabPosition { END, CENTER }

    private final BorderPane layout = new BorderPane();
    private final StackPane center = new StackPane();
    private final ObjectProperty<Node> topBar = new SimpleObjectProperty<>(this, "topBar");
    private final ObjectProperty<Node> bottomBar = new SimpleObjectProperty<>(this, "bottomBar");
    private final ObjectProperty<Node> navigation = new SimpleObjectProperty<>(this, "navigation");
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");
    private final ObjectProperty<Node> fab = new SimpleObjectProperty<>(this, "fab");
    private final ObjectProperty<FabPosition> fabPosition = new SimpleObjectProperty<>(this, "fabPosition", FabPosition.END);

    public Scaffold() {
        this(null);
    }

    public Scaffold(Node content) {
        getStyleClass().add("scaffold");
        center.getStyleClass().add("scaffold-content");
        layout.setCenter(center);
        getChildren().add(layout);
        topBar.addListener((obs, old, node) -> layout.setTop(node));
        bottomBar.addListener((obs, old, node) -> layout.setBottom(node));
        navigation.addListener((obs, old, node) -> layout.setLeft(node));
        this.content.addListener((obs, old, node) -> {
            center.getChildren().remove(old);
            if (node != null) {
                center.getChildren().addFirst(node);
            }
        });
        fab.addListener((obs, old, node) -> {
            center.getChildren().remove(old);
            if (node != null) {
                center.getChildren().add(node);
                updateFabPosition();
            }
        });
        fabPosition.addListener(o -> updateFabPosition());
        setContent(content);
    }

    private void updateFabPosition() {
        Node node = getFab();
        if (node != null) {
            StackPane.setAlignment(node, getFabPosition() == FabPosition.CENTER ? Pos.BOTTOM_CENTER : Pos.BOTTOM_RIGHT);
            StackPane.setMargin(node, new Insets(16));
        }
    }

    public final ObjectProperty<Node> topBarProperty() {
        return topBar;
    }

    public final Node getTopBar() {
        return topBar.get();
    }

    public final void setTopBar(Node value) {
        topBar.set(value);
    }

    public final ObjectProperty<Node> bottomBarProperty() {
        return bottomBar;
    }

    public final Node getBottomBar() {
        return bottomBar.get();
    }

    public final void setBottomBar(Node value) {
        bottomBar.set(value);
    }

    /** Leading navigation: a {@link NavigationRail} or a standard {@link NavigationDrawer}. */
    public final ObjectProperty<Node> navigationProperty() {
        return navigation;
    }

    public final Node getNavigation() {
        return navigation.get();
    }

    public final void setNavigation(Node value) {
        navigation.set(value);
    }

    public final ObjectProperty<Node> contentProperty() {
        return content;
    }

    public final Node getContent() {
        return content.get();
    }

    public final void setContent(Node value) {
        content.set(value);
    }

    /** Floating action button, placed over the content's bottom-right (or bottom-center) corner. */
    public final ObjectProperty<Node> fabProperty() {
        return fab;
    }

    public final Node getFab() {
        return fab.get();
    }

    public final void setFab(Node value) {
        fab.set(value);
    }

    public final ObjectProperty<FabPosition> fabPositionProperty() {
        return fabPosition;
    }

    public final FabPosition getFabPosition() {
        return fabPosition.get();
    }

    public final void setFabPosition(FabPosition value) {
        fabPosition.set(value);
    }
}
