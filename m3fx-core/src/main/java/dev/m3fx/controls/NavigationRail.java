package dev.m3fx.controls;

import dev.m3fx.skins.NavigationRailSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Skin;

/**
 * Material navigation rail: 3 to 7 destinations on the side of medium and expanded windows,
 * with an optional header (menu button, FAB) on top.
 */
public class NavigationRail extends NavigationView {

    private final ObjectProperty<Node> header = new SimpleObjectProperty<>(this, "header");
    private final ObjectProperty<Pos> itemsAlignment = new SimpleObjectProperty<>(this, "itemsAlignment", Pos.TOP_CENTER);

    public NavigationRail() {
        getStyleClass().add("navigation-rail");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new NavigationRailSkin(this);
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
