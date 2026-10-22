package dev.m3fx.skins;

import dev.m3fx.controls.Buttons;
import dev.m3fx.controls.IconButton;
import dev.m3fx.controls.Snackbar;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/** Skin of {@link Snackbar}: message, optional action button and close button. */
public class SnackbarSkin extends SkinBase<Snackbar> {

    private final HBox box = new HBox();
    private final Label message = new Label();
    private final Button action = Buttons.text(null);
    private final IconButton close = new IconButton("close");

    public SnackbarSkin(Snackbar snackbar) {
        super(snackbar);
        box.getStyleClass().add("snackbar-content");
        message.getStyleClass().add("snackbar-text");
        action.getStyleClass().add("snackbar-action");
        close.getStyleClass().add("snackbar-close");
        close.setAccessibleText("Dismiss");
        box.setAlignment(Pos.CENTER_LEFT);
        message.setWrapText(true);
        message.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(message, Priority.ALWAYS);
        message.textProperty().bind(snackbar.messageProperty());
        action.textProperty().bind(snackbar.actionTextProperty());
        action.setOnAction(e -> snackbar.fireAction());
        close.setOnAction(e -> snackbar.dismiss());
        getChildren().add(box);
        registerChangeListener(snackbar.actionTextProperty(), o -> rebuild());
        registerChangeListener(snackbar.closeableProperty(), o -> rebuild());
        rebuild();
    }

    private void rebuild() {
        Snackbar snackbar = getSkinnable();
        box.getChildren().setAll(message);
        if (snackbar.getActionText() != null && !snackbar.getActionText().isEmpty()) {
            box.getChildren().add(action);
        }
        if (snackbar.isCloseable()) {
            box.getChildren().add(close);
        }
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        box.resizeRelocate(x, y, w, h);
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return left + box.prefWidth(-1) + right;
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        return top + box.prefHeight(width < 0 ? -1 : width - left - right) + bottom;
    }

    @Override
    protected double computeMaxHeight(double width, double top, double right, double bottom, double left) {
        return computePrefHeight(width, top, right, bottom, left);
    }
}
