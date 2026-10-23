package io.github.squoirelle.material3fx.controls;

import io.github.squoirelle.material3fx.skins.CarouselSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * Material carousel (multi-browse): a horizontally scrolling row of items with rounded corners.
 * Items at the trailing edge are compressed into a smaller "peek" item, hinting there is more
 * content, and scrolling snaps to item boundaries. Scroll with the mouse wheel, by dragging, or
 * with the keyboard (Left / Right, Home / End).
 */
public class Carousel extends Control {

    private final ObservableList<Node> items = FXCollections.observableArrayList();
    private final DoubleProperty itemWidth = new SimpleDoubleProperty(this, "itemWidth", 220);
    private final DoubleProperty smallItemWidth = new SimpleDoubleProperty(this, "smallItemWidth", 56);
    private final DoubleProperty spacing = new SimpleDoubleProperty(this, "spacing", 8);

    public Carousel() {
        getStyleClass().add("carousel");
        setFocusTraversable(true);
        setAccessibleRole(AccessibleRole.LIST_VIEW);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CarouselSkin(this);
    }

    public ObservableList<Node> getItems() {
        return items;
    }

    /** Preferred width of the large items; stretched so whole items plus one peek item fill the carousel. */
    public final DoubleProperty itemWidthProperty() {
        return itemWidth;
    }

    public final double getItemWidth() {
        return itemWidth.get();
    }

    public final void setItemWidth(double value) {
        itemWidth.set(value);
    }

    public final DoubleProperty smallItemWidthProperty() {
        return smallItemWidth;
    }

    public final double getSmallItemWidth() {
        return smallItemWidth.get();
    }

    public final void setSmallItemWidth(double value) {
        smallItemWidth.set(value);
    }

    public final DoubleProperty spacingProperty() {
        return spacing;
    }

    public final double getSpacing() {
        return spacing.get();
    }

    public final void setSpacing(double value) {
        spacing.set(value);
    }
}
