package io.github.squoirelle.material3fx.controls;

import io.github.squoirelle.material3fx.skins.TopAppBarSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;

/**
 * Material top app bar: navigation icon, title and actions.
 * <p>
 * When the content below is scrolled, the bar switches to its "scrolled" color (surface
 * container); link it to a scroll pane with {@link #bindScroll(ScrollPane)}.
 */
public class TopAppBar extends Control {

    public enum Variant { SMALL, CENTER_ALIGNED, MEDIUM, LARGE }

    private static final PseudoClass SCROLLED = PseudoClass.getPseudoClass("scrolled");

    private final StringProperty title = new SimpleStringProperty(this, "title");
    private final ObjectProperty<Node> navigationIcon = new SimpleObjectProperty<>(this, "navigationIcon");
    private final ObservableList<Node> actions = FXCollections.observableArrayList();
    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant");
    private final BooleanProperty scrolled = new SimpleBooleanProperty(this, "scrolled") {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SCROLLED, get());
        }
    };

    public TopAppBar() {
        this(null);
    }

    public TopAppBar(String title) {
        getStyleClass().add("top-app-bar");
        setFocusTraversable(false);
        variant.addListener((obs, old, value) -> {
            if (old != null) {
                getStyleClass().remove(styleClass(old));
            }
            if (value != null) {
                getStyleClass().add(styleClass(value));
            }
        });
        variant.set(Variant.SMALL);
        this.title.set(title);
    }

    private static String styleClass(Variant v) {
        return v.name().toLowerCase().replace('_', '-');
    }

    /** Convenience: sets a navigation icon button and returns it. */
    public IconButton setNavigationIcon(String icon) {
        IconButton button = new IconButton(icon);
        setNavigationIcon(button);
        return button;
    }

    /** Convenience: adds an action icon button and returns it. */
    public IconButton addAction(String icon, String tooltip) {
        IconButton button = new IconButton(icon).tooltip(tooltip);
        actions.add(button);
        return button;
    }

    /** Marks the bar as scrolled whenever {@code scrollPane} is not at the top. */
    public void bindScroll(ScrollPane scrollPane) {
        scrolled.bind(scrollPane.vvalueProperty().greaterThan(scrollPane.getVmin()));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TopAppBarSkin(this);
    }

    public final StringProperty titleProperty() {
        return title;
    }

    public final String getTitle() {
        return title.get();
    }

    public final void setTitle(String value) {
        title.set(value);
    }

    public final ObjectProperty<Node> navigationIconProperty() {
        return navigationIcon;
    }

    public final Node getNavigationIcon() {
        return navigationIcon.get();
    }

    public final void setNavigationIcon(Node value) {
        navigationIcon.set(value);
    }

    /** Trailing action nodes, typically {@link IconButton}s (at most three, then use an overflow menu). */
    public ObservableList<Node> getActions() {
        return actions;
    }

    public final ObjectProperty<Variant> variantProperty() {
        return variant;
    }

    public final Variant getVariant() {
        return variant.get();
    }

    public final void setVariant(Variant value) {
        variant.set(value);
    }

    public final BooleanProperty scrolledProperty() {
        return scrolled;
    }

    public final boolean isScrolled() {
        return scrolled.get();
    }

    public final void setScrolled(boolean value) {
        scrolled.set(value);
    }
}
