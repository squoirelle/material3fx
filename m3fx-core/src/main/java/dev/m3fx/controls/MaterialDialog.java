package dev.m3fx.controls;

import dev.m3fx.skins.MaterialDialogSkin;
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
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.Callback;

import java.util.concurrent.CompletableFuture;

/**
 * Material dialog, shown inside the window over a scrim (see {@link Overlay} for where it is
 * hosted). Modeled on {@link javafx.scene.control.Dialog}: {@link ButtonType}s define the actions
 * ({@link ButtonData} decides the default and cancel buttons), and a
 * {@link #resultConverterProperty() result converter} turns the chosen button into the result.
 * {@link #show(Node)} returns a future instead of blocking.
 *
 * <pre>{@code
 * MaterialDialog<ButtonType> dialog = new MaterialDialog<>("Delete mission?", "This permanently removes the plan.");
 * dialog.setIcon("delete");
 * dialog.getButtonTypes().addAll(ButtonType.CANCEL, new ButtonType("Delete", ButtonData.OK_DONE));
 * dialog.show(ownerNode).thenAccept(button -> ...);
 *
 * MaterialDialog.confirm(owner, "Discard draft?", null, "Discard", "Cancel").thenAccept(confirmed -> ...);
 * }</pre>
 * Escape and a click on the scrim dismiss the dialog when {@link #dismissableProperty() dismissable};
 * the result is then that of the cancel button if there is one, or null.
 *
 * @param <R> result type; {@code ButtonType} unless a result converter is set
 */
public class MaterialDialog<R> extends Control {

    private static final PseudoClass FULL_SCREEN = PseudoClass.getPseudoClass("full-screen");

    private final StringProperty headline = new SimpleStringProperty(this, "headline");
    private final StringProperty supportingText = new SimpleStringProperty(this, "supportingText");
    private final StringProperty icon = new SimpleStringProperty(this, "icon");
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");
    private final ObservableList<ButtonType> buttonTypes = FXCollections.observableArrayList();
    @SuppressWarnings("unchecked")
    private final ObjectProperty<Callback<ButtonType, R>> resultConverter =
            new SimpleObjectProperty<>(this, "resultConverter", button -> (R) button);
    private final BooleanProperty dismissable = new SimpleBooleanProperty(this, "dismissable", true);
    private final BooleanProperty fullScreen = new SimpleBooleanProperty(this, "fullScreen") {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(FULL_SCREEN, get());
        }
    };
    private Overlay overlay;
    private CompletableFuture<R> result;

    public MaterialDialog() {
        this(null, null);
    }

    public MaterialDialog(String headline, String supportingText) {
        getStyleClass().add("material-dialog");
        setFocusTraversable(false);
        accessibleTextProperty().bind(this.headline);
        this.headline.set(headline);
        this.supportingText.set(supportingText);
    }

    /** Shows a confirmation dialog; completes with true when the confirm button is chosen. */
    public static CompletableFuture<Boolean> confirm(Node owner, String headline, String text, String confirm, String cancel) {
        ButtonType yes = new ButtonType(confirm, ButtonData.OK_DONE);
        ButtonType no = new ButtonType(cancel, ButtonData.CANCEL_CLOSE);
        MaterialDialog<Boolean> dialog = new MaterialDialog<>(headline, text);
        dialog.getButtonTypes().addAll(no, yes);
        dialog.setResultConverter(yes::equals);
        return dialog.show(owner).thenApply(Boolean.TRUE::equals);
    }

    /** Shows an informational dialog with a single OK button. */
    public static CompletableFuture<ButtonType> alert(Node owner, String headline, String text) {
        MaterialDialog<ButtonType> dialog = new MaterialDialog<>(headline, text);
        dialog.getButtonTypes().add(ButtonType.OK);
        return dialog.show(owner);
    }

    /**
     * Shows the dialog in the window of {@code owner}. Completes with the converted result of the
     * chosen button, or of the cancel button / null when dismissed.
     */
    public CompletableFuture<R> show(Node owner) {
        if (isShowing()) {
            return result;
        }
        result = new CompletableFuture<>();
        overlay = Overlay.show(owner, this, isFullScreen() ? Overlay.Position.FILL : Overlay.Position.CENTER,
                true, isDismissable());
        overlay.setOnHidden(() -> {
            if (!result.isDone()) {
                result.complete(convert(cancelButton()));
            }
        });
        applyCss();   // creates the skin if needed
        if (getSkin() instanceof MaterialDialogSkin<?> skin) {
            skin.focusInitialNode();
        }
        return result;
    }

    /** Closes the dialog as if {@code button} had been clicked (null: dismissed without result). */
    public void close(ButtonType button) {
        if (result != null && !result.isDone()) {
            result.complete(convert(button));
        }
        if (overlay != null) {
            overlay.hide();
        }
    }

    public boolean isShowing() {
        return overlay != null && overlay.isShowing();
    }

    private R convert(ButtonType button) {
        return button == null || getResultConverter() == null ? null : getResultConverter().call(button);
    }

    private ButtonType cancelButton() {
        return buttonTypes.stream().filter(b -> b.getButtonData().isCancelButton()).findFirst().orElse(null);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MaterialDialogSkin<>(this);
    }

    public final StringProperty headlineProperty() {
        return headline;
    }

    public final String getHeadline() {
        return headline.get();
    }

    public final void setHeadline(String value) {
        headline.set(value);
    }

    public final StringProperty supportingTextProperty() {
        return supportingText;
    }

    public final String getSupportingText() {
        return supportingText.get();
    }

    public final void setSupportingText(String value) {
        supportingText.set(value);
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

    /** The action buttons, as with {@link javafx.scene.control.DialogPane#getButtonTypes()}. */
    public final ObservableList<ButtonType> getButtonTypes() {
        return buttonTypes;
    }

    public final ObjectProperty<Callback<ButtonType, R>> resultConverterProperty() {
        return resultConverter;
    }

    public final Callback<ButtonType, R> getResultConverter() {
        return resultConverter.get();
    }

    public final void setResultConverter(Callback<ButtonType, R> value) {
        resultConverter.set(value);
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

    /** Full-screen dialog: close button, headline and default button in a top bar; content fills the window. */
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
