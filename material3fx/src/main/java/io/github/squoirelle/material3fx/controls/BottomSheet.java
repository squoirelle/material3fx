package io.github.squoirelle.material3fx.controls;

import io.github.squoirelle.material3fx.skins.BottomSheetSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * Material bottom sheet: slides up from the bottom, over a scrim when modal. Dragging its handle
 * down dismisses it, as do Escape and a click on the scrim.
 */
public class BottomSheet extends Control {

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");
    private final BooleanProperty modal = new SimpleBooleanProperty(this, "modal", true);
    private Overlay overlay;

    public BottomSheet() {
        this(null);
    }

    public BottomSheet(Node content) {
        getStyleClass().add("bottom-sheet");
        setFocusTraversable(false);
        this.content.set(content);
    }

    /** Shows the sheet at the bottom of {@code owner}'s window. */
    public Overlay show(Node owner) {
        setTranslateY(0);
        overlay = Overlay.show(owner, this, Overlay.Position.BOTTOM, isModal(), true);
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
        return new BottomSheetSkin(this);
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

    /** Modal sheets (default) have a scrim and block the content behind them. */
    public final BooleanProperty modalProperty() {
        return modal;
    }

    public final boolean isModal() {
        return modal.get();
    }

    public final void setModal(boolean value) {
        modal.set(value);
    }
}
