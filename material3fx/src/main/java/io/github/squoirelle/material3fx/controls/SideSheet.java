package io.github.squoirelle.material3fx.controls;

import io.github.squoirelle.material3fx.skins.SideSheetSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * Material modal side sheet: supplementary content (filters, details) sliding in from the end of
 * the window, with a title, close button and optional actions at the bottom.
 */
public class SideSheet extends Control {

    private final StringProperty title = new SimpleStringProperty(this, "title");
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");
    private final ObservableList<Node> actions = FXCollections.observableArrayList();
    private Overlay overlay;

    public SideSheet() {
        this(null, null);
    }

    public SideSheet(String title) {
        this(title, null);
    }

    public SideSheet(String title, Node content) {
        getStyleClass().add("side-sheet");
        setFocusTraversable(false);
        this.title.set(title);
        this.content.set(content);
    }

    /** Convenience: adds an action button; the first one added is filled, the others outlined. */
    public Button addAction(String text, Runnable handler) {
        Button button = actions.isEmpty() ? Buttons.filled(text) : Buttons.outlined(text);
        button.setOnAction(e -> handler.run());
        actions.add(button);
        return button;
    }

    public Overlay show(Node owner) {
        overlay = Overlay.show(owner, this, Overlay.Position.END, true, true);
        return overlay;
    }

    public void hide() {
        if (overlay != null) {
            overlay.hide();
        }
    }

    public boolean isShowing() {
        return overlay != null && overlay.isShowing();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SideSheetSkin(this);
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

    public final ObjectProperty<Node> contentProperty() {
        return content;
    }

    public final Node getContent() {
        return content.get();
    }

    public final void setContent(Node value) {
        content.set(value);
    }

    /** Buttons shown at the bottom of the sheet. */
    public ObservableList<Node> getActions() {
        return actions;
    }
}
