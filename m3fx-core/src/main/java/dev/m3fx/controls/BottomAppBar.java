package dev.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Material bottom app bar: up to four actions on the leading side and an optional FAB at the end.
 */
public class BottomAppBar extends HBox {

    private final HBox actionsBox = new HBox();
    private final Region spacer = new Region();
    private final ObservableList<Node> actions = FXCollections.observableArrayList();
    private final ObjectProperty<Fab> fab = new SimpleObjectProperty<>(this, "fab");

    public BottomAppBar() {
        getStyleClass().add("bottom-app-bar");
        actionsBox.getStyleClass().add("bottom-app-bar-actions");
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(actionsBox, spacer);
        actions.addListener((ListChangeListener<Node>) c -> actionsBox.getChildren().setAll(actions));
        fab.addListener((obs, old, value) -> {
            if (old != null) {
                getChildren().remove(old);
                old.getStyleClass().remove("lowered");
            }
            if (value != null) {
                value.getStyleClass().add("lowered");
                getChildren().add(value);
            }
        });
    }

    public IconButton addAction(String icon, String tooltip) {
        IconButton button = new IconButton(icon).tooltip(tooltip);
        actions.add(button);
        return button;
    }

    public ObservableList<Node> getActions() {
        return actions;
    }

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
