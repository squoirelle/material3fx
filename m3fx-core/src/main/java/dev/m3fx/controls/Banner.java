package dev.m3fx.controls;

import dev.m3fx.skins.BannerSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.Locale;

/**
 * Material banner: a prominent, persistent message at the top of the content ("Connection to the
 * relay lost", "Read-only mode") with up to two actions. Unlike a {@link Snackbar} it stays until
 * dismissed or resolved. Hiding and showing animate its height.
 *
 * <pre>{@code
 * Banner offline = new Banner("Connection lost. Changes will sync when it is back.", "cloud_off");
 * offline.setSeverity(Banner.Severity.WARNING);
 * offline.addAction("Retry", this::reconnect);
 * offline.addAction("Dismiss", () -> offline.setShowing(false));
 * }</pre>
 */
public class Banner extends Control {

    public enum Severity { INFO, SUCCESS, WARNING, ERROR }

    private final StringProperty text = new SimpleStringProperty(this, "text");
    private final StringProperty icon = new SimpleStringProperty(this, "icon");
    private final ObservableList<Node> actions = FXCollections.observableArrayList();
    private final ObjectProperty<Severity> severity = new SimpleObjectProperty<>(this, "severity");
    private final BooleanProperty showing = new SimpleBooleanProperty(this, "showing", true);

    public Banner() {
        this(null, null);
    }

    public Banner(String text, String icon) {
        getStyleClass().add("banner");
        setFocusTraversable(false);
        setAccessibleRole(AccessibleRole.TEXT);
        accessibleTextProperty().bind(this.text);
        severity.addListener((obs, old, value) -> {
            if (old != null) {
                getStyleClass().remove(old.name().toLowerCase(Locale.ROOT));
            }
            if (value != null) {
                getStyleClass().add(value.name().toLowerCase(Locale.ROOT));
            }
        });
        severity.set(Severity.INFO);
        this.text.set(text);
        this.icon.set(icon);
    }

    /** Convenience: adds a text button running {@code handler}. */
    public Button addAction(String label, Runnable handler) {
        Button button = Buttons.text(label);
        button.setOnAction(e -> handler.run());
        actions.add(button);
        return button;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new BannerSkin(this);
    }

    public final StringProperty textProperty() {
        return text;
    }

    public final String getText() {
        return text.get();
    }

    public final void setText(String value) {
        text.set(value);
    }

    /** Leading icon name, or null for none. */
    public final StringProperty iconProperty() {
        return icon;
    }

    public final String getIcon() {
        return icon.get();
    }

    public final void setIcon(String value) {
        icon.set(value);
    }

    /** Action buttons, shown at the trailing end (or below a long message). */
    public ObservableList<Node> getActions() {
        return actions;
    }

    /** Colors: INFO uses surface colors; SUCCESS / WARNING / ERROR use the matching container roles. */
    public final ObjectProperty<Severity> severityProperty() {
        return severity;
    }

    public final Severity getSeverity() {
        return severity.get();
    }

    public final void setSeverity(Severity value) {
        severity.set(value);
    }

    /** Whether the banner is shown; changes animate the height. */
    public final BooleanProperty showingProperty() {
        return showing;
    }

    public final boolean isShowing() {
        return showing.get();
    }

    public final void setShowing(boolean value) {
        showing.set(value);
    }
}
