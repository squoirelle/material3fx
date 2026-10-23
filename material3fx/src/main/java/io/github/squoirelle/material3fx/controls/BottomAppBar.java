package io.github.squoirelle.material3fx.controls;

import io.github.squoirelle.material3fx.skins.BottomAppBarSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/** Material bottom app bar: up to four actions on the leading side and an optional FAB at the end. */
public class BottomAppBar extends Control {

    private final ObservableList<Node> actions = FXCollections.observableArrayList();
    private final ObjectProperty<Fab> fab = new SimpleObjectProperty<>(this, "fab");

    public BottomAppBar() {
        getStyleClass().add("bottom-app-bar");
        setFocusTraversable(false);
    }

    public IconButton addAction(String icon, String tooltip) {
        IconButton button = new IconButton(icon).tooltip(tooltip);
        actions.add(button);
        return button;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new BottomAppBarSkin(this);
    }

    public ObservableList<Node> getActions() {
        return actions;
    }

    /** The FAB, shown without elevation at the trailing end. */
    public final ObjectProperty<Fab> fabProperty() {
        return fab;
    }

    public final Fab getFab() {
        return fab.get();
    }

    public final void setFab(Fab value) {
        fab.set(value);
    }
}
