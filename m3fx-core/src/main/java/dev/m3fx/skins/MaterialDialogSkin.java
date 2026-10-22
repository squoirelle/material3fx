package dev.m3fx.skins;

import dev.m3fx.controls.Buttons;
import dev.m3fx.controls.Icon;
import dev.m3fx.controls.IconButton;
import dev.m3fx.controls.MaterialDialog;
import dev.m3fx.controls.MaterialTextField;
import dev.m3fx.controls.TimePicker;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Skin of {@link MaterialDialog}: optional icon, headline, supporting text, content and a row of
 * text buttons; or, full screen, a top bar with close button, headline and the default button.
 */
public class MaterialDialogSkin<R> extends SkinBase<MaterialDialog<R>> {

    private static final PseudoClass WITH_ICON = PseudoClass.getPseudoClass("with-icon");

    private final VBox box = new VBox();
    private final Icon icon = new Icon(null, 24);
    private final Label headline = new Label();
    private final Label text = new Label();
    private final VBox body = new VBox();
    private final HBox actions = new HBox(8);
    private final List<Button> buttons = new ArrayList<>();
    private final ListChangeListener<ButtonType> buttonsListener = c -> rebuild();

    public MaterialDialogSkin(MaterialDialog<R> dialog) {
        super(dialog);
        box.getStyleClass().add("dialog-container");
        icon.getStyleClass().add("dialog-icon");
        icon.setFollowTextFill(false);
        headline.getStyleClass().add("dialog-headline");
        text.getStyleClass().add("dialog-text");
        body.getStyleClass().add("dialog-body");
        actions.getStyleClass().add("dialog-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);
        headline.setWrapText(true);
        text.setWrapText(true);
        icon.nameProperty().bind(dialog.iconProperty());
        headline.textProperty().bind(dialog.headlineProperty());
        text.textProperty().bind(dialog.supportingTextProperty());
        dialog.getButtonTypes().addListener(buttonsListener);
        getChildren().add(box);
        for (ObservableValue<?> p : List.of(dialog.iconProperty(), dialog.contentProperty(), dialog.fullScreenProperty(),
                dialog.headlineProperty(), dialog.supportingTextProperty())) {
            registerChangeListener(p, o -> rebuild());
        }
        rebuild();
    }

    private void rebuild() {
        MaterialDialog<R> dialog = getSkinnable();
        buttons.clear();
        for (ButtonType type : dialog.getButtonTypes()) {
            Button button = Buttons.text(type.getText());
            button.setDefaultButton(type.getButtonData().isDefaultButton());
            button.setCancelButton(type.getButtonData().isCancelButton());
            button.setOnAction(e -> dialog.close(type));
            buttons.add(button);
        }
        box.getChildren().clear();
        body.getChildren().clear();
        boolean hasIcon = dialog.getIcon() != null && !dialog.getIcon().isEmpty();
        dialog.pseudoClassStateChanged(WITH_ICON, hasIcon && !dialog.isFullScreen());
        if (hasText(dialog.getSupportingText())) {
            body.getChildren().add(text);
        }
        if (dialog.getContent() != null) {
            body.getChildren().add(dialog.getContent());
        }
        if (dialog.isFullScreen()) {
            IconButton close = new IconButton("close");
            close.setOnAction(e -> dialog.close(null));
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox bar = new HBox(8, close, headline, spacer);
            bar.getStyleClass().add("dialog-top-bar");
            bar.setAlignment(Pos.CENTER_LEFT);
            buttons.stream().filter(Button::isDefaultButton).findFirst().ifPresent(bar.getChildren()::add);
            if (dialog.getContent() != null) {
                VBox.setVgrow(dialog.getContent(), Priority.ALWAYS);
            }
            VBox.setVgrow(body, Priority.ALWAYS);
            box.getChildren().addAll(bar, body);
            box.setAlignment(Pos.TOP_LEFT);
            return;
        }
        if (hasIcon) {
            box.getChildren().add(icon);
        }
        if (hasText(dialog.getHeadline())) {
            box.getChildren().add(headline);
        }
        if (!body.getChildren().isEmpty()) {
            box.getChildren().add(body);
        }
        actions.getChildren().setAll(buttons);
        if (!buttons.isEmpty()) {
            box.getChildren().add(actions);
        }
        box.setAlignment(hasIcon ? Pos.TOP_CENTER : Pos.TOP_LEFT);
    }

    /**
     * Focuses the default button, unless the dialog contains a form the user should type into
     * (the overlay then focuses its first field).
     */
    public void focusInitialNode() {
        if (!containsTextInput(getSkinnable().getContent())) {
            buttons.stream().filter(Button::isDefaultButton).findFirst()
                    .or(() -> buttons.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(buttons.getLast()))
                    .ifPresent(Button::requestFocus);
        }
    }

    private static boolean containsTextInput(Node node) {
        if (node == null || node instanceof TimePicker) {
            return false;
        }
        if (node instanceof TextInputControl || node instanceof MaterialTextField) {
            return true;
        }
        if (node instanceof Parent parent) {
            return parent.getChildrenUnmodifiable().stream().anyMatch(MaterialDialogSkin::containsTextInput);
        }
        return false;
    }

    private static boolean hasText(String s) {
        return s != null && !s.isEmpty();
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
        return getSkinnable().isFullScreen() ? Double.MAX_VALUE : computePrefHeight(width, top, right, bottom, left);
    }

    @Override
    public void dispose() {
        getSkinnable().getButtonTypes().removeListener(buttonsListener);
        super.dispose();
    }
}
