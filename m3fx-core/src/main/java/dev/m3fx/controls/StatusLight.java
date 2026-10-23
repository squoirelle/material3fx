package dev.m3fx.controls;

import dev.m3fx.skins.StatusLightSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.Locale;

/**
 * A status indicator: a colored dot with an optional label, for system or unit states
 * ("Online", "Degraded", "Offline"). Pulsing draws attention to states that need it.
 *
 * <pre>{@code
 * StatusLight link = new StatusLight(StatusLight.Status.SUCCESS, "Link up");
 * link.setPulsing(true);
 * }</pre>
 * Colors come from the theme's semantic roles (success, warning, error, primary for info,
 * outline for neutral).
 */
public class StatusLight extends Control {

    public enum Status { NEUTRAL, INFO, SUCCESS, WARNING, ERROR }

    private final ObjectProperty<Status> status = new SimpleObjectProperty<>(this, "status");
    private final StringProperty text = new SimpleStringProperty(this, "text");
    private final BooleanProperty pulsing = new SimpleBooleanProperty(this, "pulsing");

    public StatusLight() {
        this(Status.NEUTRAL, null);
    }

    public StatusLight(Status status, String text) {
        getStyleClass().add("status-light");
        setFocusTraversable(false);
        setAccessibleRole(AccessibleRole.TEXT);
        this.status.addListener((obs, old, value) -> {
            if (old != null) {
                getStyleClass().remove(old.name().toLowerCase(Locale.ROOT));
            }
            if (value != null) {
                getStyleClass().add(value.name().toLowerCase(Locale.ROOT));
            }
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        });
        this.text.addListener(o -> notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT));
        this.status.set(status == null ? Status.NEUTRAL : status);
        this.text.set(text);
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        if (attribute == AccessibleAttribute.TEXT) {
            String state = getStatus().name().toLowerCase(Locale.ROOT);
            return getText() == null ? "Status: " + state : getText() + " (" + state + ")";
        }
        return super.queryAccessibleAttribute(attribute, parameters);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new StatusLightSkin(this);
    }

    public final ObjectProperty<Status> statusProperty() {
        return status;
    }

    public final Status getStatus() {
        return status.get();
    }

    public final void setStatus(Status value) {
        status.set(value);
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

    /** Animates a ring around the dot. Stops automatically when not showing or motion is disabled. */
    public final BooleanProperty pulsingProperty() {
        return pulsing;
    }

    public final boolean isPulsing() {
        return pulsing.get();
    }

    public final void setPulsing(boolean value) {
        pulsing.set(value);
    }
}
