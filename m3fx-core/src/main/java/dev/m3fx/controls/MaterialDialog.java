package dev.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Material dialog, shown in-window over a scrim (see {@link Overlay} for where it is hosted).
 *
 * <pre>{@code
 * MaterialDialog dialog = new MaterialDialog("Delete mission?", "This permanently removes the plan.");
 * dialog.setIcon("delete");
 * dialog.addAction("Cancel");
 * dialog.addPrimaryAction("Delete");
 * dialog.show(ownerNode).thenAccept(result -> { if ("Delete".equals(result)) ... });
 *
 * MaterialDialog.confirm(owner, "Discard draft?", null, "Discard", "Cancel")
 *         .thenAccept(confirmed -> ...);
 * }</pre>
 * Setting {@link #fullScreenProperty() full screen} turns it into a full-screen dialog with a
 * close button and the primary action in its top bar.
 */
public class MaterialDialog extends VBox {

    private final Icon iconNode = new Icon(null, 24);
    private final Label headlineLabel = new Label();
    private final Label textLabel = new Label();
    private final HBox actionsBox = new HBox(8);
    private final VBox body = new VBox();
    private final StringProperty icon = new SimpleStringProperty(this, "icon");
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");
    private final BooleanProperty dismissable = new SimpleBooleanProperty(this, "dismissable", true);
    private final BooleanProperty fullScreen = new SimpleBooleanProperty(this, "fullScreen");
    private final List<Button> actions = new ArrayList<>();
    private Button primaryAction;
    private Overlay overlay;
    private CompletableFuture<String> result;

    public MaterialDialog() {
        this(null, null);
    }

    public MaterialDialog(String headline, String supportingText) {
        getStyleClass().add("material-dialog");
        setMaxHeight(USE_PREF_SIZE);
        iconNode.getStyleClass().add("dialog-icon");
        headlineLabel.getStyleClass().add("dialog-headline");
        textLabel.getStyleClass().add("dialog-text");
        actionsBox.getStyleClass().add("dialog-actions");
        body.getStyleClass().add("dialog-body");
        headlineLabel.setWrapText(true);
        textLabel.setWrapText(true);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        headlineLabel.setText(headline);
        textLabel.setText(supportingText);
        iconNode.nameProperty().bind(icon);
        icon.addListener(o -> rebuild());
        content.addListener(o -> rebuild());
        fullScreen.addListener((obs, old, value) -> {
            pseudoClassStateChanged(FULL_SCREEN, value);
            rebuild();
        });
        headlineLabel.textProperty().addListener(o -> rebuild());
        textLabel.textProperty().addListener(o -> rebuild());
        rebuild();
    }

    private static final javafx.css.PseudoClass FULL_SCREEN = javafx.css.PseudoClass.getPseudoClass("full-screen");
    private static final javafx.css.PseudoClass WITH_ICON = javafx.css.PseudoClass.getPseudoClass("with-icon");

    /** Shows a confirmation dialog; completes with true when the confirm action is chosen. */
    public static CompletableFuture<Boolean> confirm(Node owner, String headline, String text, String confirm, String cancel) {
        MaterialDialog dialog = new MaterialDialog(headline, text);
        dialog.addAction(cancel);
        dialog.addPrimaryAction(confirm);
        return dialog.show(owner).thenApply(confirm::equals);
    }

    /** Shows an informational dialog with a single OK action. */
    public static CompletableFuture<String> alert(Node owner, String headline, String text) {
        MaterialDialog dialog = new MaterialDialog(headline, text);
        dialog.addPrimaryAction("OK");
        return dialog.show(owner);
    }

    /** Adds a text action that closes the dialog with its text as result. */
    public Button addAction(String text) {
        Button button = Buttons.text(text);
        button.setOnAction(e -> close(text));
        actions.add(button);
        rebuild();
        return button;
    }

    /**
     * Adds the primary action. In a basic dialog it is a text button like the others; in a
     * full-screen dialog it moves to the top bar.
     */
    public Button addPrimaryAction(String text) {
        Button button = addAction(text);
        button.setDefaultButton(true);
        primaryAction = button;
        rebuild();
        return button;
    }

    private void rebuild() {
        getChildren().clear();
        body.getChildren().clear();
        boolean hasIcon = getIcon() != null && !getIcon().isEmpty();
        pseudoClassStateChanged(WITH_ICON, hasIcon);
        if (isFullScreen()) {
            IconButton close = new IconButton("close");
            close.setOnAction(e -> close(null));
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox bar = new HBox(8, close, headlineLabel, spacer);
            bar.getStyleClass().add("dialog-top-bar");
            bar.setAlignment(Pos.CENTER_LEFT);
            if (primaryAction != null) {
                bar.getChildren().add(primaryAction);
            }
            getChildren().add(bar);
            if (textLabel.getText() != null) {
                body.getChildren().add(textLabel);
            }
            if (getContent() != null) {
                body.getChildren().add(getContent());
                VBox.setVgrow(getContent(), Priority.ALWAYS);
            }
            VBox.setVgrow(body, Priority.ALWAYS);
            getChildren().add(body);
            return;
        }
        if (hasIcon) {
            getChildren().add(iconNode);
        }
        if (headlineLabel.getText() != null) {
            getChildren().add(headlineLabel);
        }
        if (textLabel.getText() != null) {
            body.getChildren().add(textLabel);
        }
        if (getContent() != null) {
            body.getChildren().add(getContent());
        }
        if (!body.getChildren().isEmpty()) {
            getChildren().add(body);
        }
        actionsBox.getChildren().setAll(actions);
        if (!actions.isEmpty()) {
            getChildren().add(actionsBox);
        }
        setAlignment(hasIcon ? Pos.TOP_CENTER : Pos.TOP_LEFT);
    }

    /** Shows the dialog in the window of {@code owner}. Completes with the chosen action's text, or null when dismissed. */
    public CompletableFuture<String> show(Node owner) {
        if (overlay != null && overlay.isShowing()) {
            return result;
        }
        result = new CompletableFuture<>();
        overlay = Overlay.show(owner, this, isFullScreen() ? Overlay.Position.FILL : Overlay.Position.CENTER,
                true, isDismissable());
        // Focus the primary action, unless the dialog contains a form the user should type into.
        if (primaryAction != null && !containsTextInput(getContent())) {
            primaryAction.requestFocus();
        }
        overlay.setOnHidden(() -> {
            if (!result.isDone()) {
                result.complete(null);
            }
        });
        return result;
    }

    private static boolean containsTextInput(Node node) {
        if (node == null || node instanceof TimePicker) {
            return false;
        }
        if (node instanceof javafx.scene.control.TextInputControl || node instanceof MaterialTextField) {
            return true;
        }
        if (node instanceof javafx.scene.Parent parent) {
            return parent.getChildrenUnmodifiable().stream().anyMatch(MaterialDialog::containsTextInput);
        }
        return false;
    }

    /** Closes the dialog with the given result. */
    public void close(String value) {
        if (result != null && !result.isDone()) {
            result.complete(value);
        }
        if (overlay != null) {
            overlay.hide();
        }
    }

    public final void setHeadline(String value) {
        headlineLabel.setText(value);
    }

    public final String getHeadline() {
        return headlineLabel.getText();
    }

    public final StringProperty headlineProperty() {
        return headlineLabel.textProperty();
    }

    public final void setSupportingText(String value) {
        textLabel.setText(value);
    }

    public final String getSupportingText() {
        return textLabel.getText();
    }

    public final StringProperty supportingTextProperty() {
        return textLabel.textProperty();
    }

    /** Optional hero icon, shown centered above the headline. */
    public final StringProperty iconProperty() {
        return icon;
    }

    public final String getIcon() {
        return icon.get();
    }

    public final void setIcon(String value) {
        icon.set(value);
    }

    /** Custom content shown below the supporting text (forms, lists...). */
    public final ObjectProperty<Node> contentProperty() {
        return content;
    }

    public final Node getContent() {
        return content.get();
    }

    public final void setContent(Node value) {
        content.set(value);
    }

    /** Whether Escape and a click on the scrim close the dialog (default true). */
    public final BooleanProperty dismissableProperty() {
        return dismissable;
    }

    public final boolean isDismissable() {
        return dismissable.get();
    }

    public final void setDismissable(boolean value) {
        dismissable.set(value);
    }

    public final BooleanProperty fullScreenProperty() {
        return fullScreen;
    }

    public final boolean isFullScreen() {
        return fullScreen.get();
    }

    public final void setFullScreen(boolean value) {
        fullScreen.set(value);
    }
}
