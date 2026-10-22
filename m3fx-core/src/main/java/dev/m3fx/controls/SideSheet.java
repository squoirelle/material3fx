package dev.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Material modal side sheet: supplementary content (filters, details) sliding in from the end
 * of the window, with a headline, close button and optional actions.
 */
public class SideSheet extends VBox {

    private final Label headline = new Label();
    private final HBox header = new HBox();
    private final HBox actions = new HBox(8);
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");
    private Overlay overlay;

    public SideSheet(String title) {
        this(title, null);
    }

    public SideSheet(String title, Node content) {
        getStyleClass().add("side-sheet");
        headline.getStyleClass().add("side-sheet-headline");
        header.getStyleClass().add("side-sheet-header");
        actions.getStyleClass().add("side-sheet-actions");
        headline.setText(title);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        IconButton close = new IconButton("close");
        close.setOnAction(e -> hide());
        header.getChildren().addAll(headline, spacer, close);
        header.setAlignment(Pos.CENTER_LEFT);
        actions.setAlignment(Pos.CENTER_LEFT);
        getChildren().add(header);
        this.content.addListener((obs, old, node) -> {
            getChildren().remove(old);
            if (node != null) {
                getChildren().add(1, node);
                VBox.setVgrow(node, Priority.ALWAYS);
            }
        });
        this.content.set(content);
    }

    /** Adds an action button at the bottom of the sheet; the first one added is filled. */
    public Button addAction(String text, Runnable handler) {
        Button button = actions.getChildren().isEmpty() ? Buttons.filled(text) : Buttons.outlined(text);
        button.setOnAction(e -> handler.run());
        actions.getChildren().add(button);
        if (!getChildren().contains(actions)) {
            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);
            if (getContent() == null) {
                getChildren().add(spacer);
            }
            getChildren().add(actions);
        }
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

    public final ObjectProperty<Node> contentProperty() {
        return content;
    }

    public final Node getContent() {
        return content.get();
    }

    public final void setContent(Node value) {
        content.set(value);
    }

    public final void setTitle(String value) {
        headline.setText(value);
    }

    public final String getTitle() {
        return headline.getText();
    }
}
