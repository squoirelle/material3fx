package dev.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Material bottom sheet: slides up from the bottom, over a scrim when modal. It can be dragged
 * down by its handle to dismiss it.
 */
public class BottomSheet extends VBox {

    private final Region handle = new Region();
    private final StackPane handleArea = new StackPane(handle);
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");
    private final BooleanProperty modal = new SimpleBooleanProperty(this, "modal", true);
    private Overlay overlay;
    private double dragStartY;

    public BottomSheet() {
        this(null);
    }

    public BottomSheet(Node content) {
        getStyleClass().add("bottom-sheet");
        handle.getStyleClass().add("bottom-sheet-handle");
        handleArea.getStyleClass().add("bottom-sheet-handle-area");
        handleArea.setAlignment(Pos.CENTER);
        getChildren().add(handleArea);
        this.content.addListener((obs, old, node) -> {
            getChildren().remove(old);
            if (node != null) {
                getChildren().add(node);
            }
        });
        this.content.set(content);

        // Dragged by its handle area, so content (lists, sliders) keeps its own mouse handling.
        handleArea.setCursor(javafx.scene.Cursor.OPEN_HAND);
        handleArea.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> dragStartY = e.getSceneY());
        handleArea.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> setTranslateY(Math.max(0, e.getSceneY() - dragStartY)));
        handleArea.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (getTranslateY() > getHeight() * 0.3) {
                hide();
            } else {
                setTranslateY(0);
            }
        });
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
