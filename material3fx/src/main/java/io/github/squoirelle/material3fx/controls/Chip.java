package io.github.squoirelle.material3fx.controls;

import io.github.squoirelle.material3fx.skins.ChipSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.ToggleButton;

/**
 * Material chip: compact element representing an input, attribute or action. A
 * {@link ToggleButton}, so chips work with {@code onAction}, toggle groups (single-select filter
 * chips) and keyboard navigation like any JavaFX button.
 * <ul>
 *     <li>{@link Variant#ASSIST}: a smart action, like a button</li>
 *     <li>{@link Variant#FILTER}: toggles on/off, shows a check mark when selected</li>
 *     <li>{@link Variant#INPUT}: a piece of user input (e.g. a recipient), removable with its close
 *     icon or the Delete / Backspace keys</li>
 *     <li>{@link Variant#SUGGESTION}: a dynamically generated suggestion</li>
 * </ul>
 * The leading icon is the {@link #graphicProperty() graphic}: an {@link Icon} (see
 * {@link #setIcon(String)}) or an {@link Avatar}.
 */
public class Chip extends ToggleButton {

    public enum Variant { ASSIST, FILTER, INPUT, SUGGESTION }

    private static final PseudoClass ELEVATED = PseudoClass.getPseudoClass("elevated");

    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant");
    private final BooleanProperty elevated = new SimpleBooleanProperty(this, "elevated");
    private final ObjectProperty<EventHandler<ActionEvent>> onClose = new SimpleObjectProperty<>(this, "onClose");

    public Chip() {
        this(null);
    }

    public Chip(String text) {
        this(text, Variant.ASSIST);
    }

    public Chip(String text, Variant variant) {
        this(text, null, variant);
    }

    public Chip(String text, String icon, Variant variant) {
        super(text);
        getStyleClass().setAll("chip");
        setMnemonicParsing(false);
        this.variant.addListener((obs, old, value) -> {
            if (old != null) {
                getStyleClass().remove(old.name().toLowerCase());
            }
            if (value != null) {
                getStyleClass().add(value.name().toLowerCase());
            }
            setAccessibleRole(value == Variant.FILTER ? AccessibleRole.TOGGLE_BUTTON : AccessibleRole.BUTTON);
            if (value != Variant.FILTER) {
                setSelected(false);
            }
        });
        this.variant.set(variant);
        elevated.addListener((obs, old, value) -> pseudoClassStateChanged(ELEVATED, value));
        setIcon(icon);
    }

    /** Filter chips toggle; the other variants only fire their action. */
    @Override
    public void fire() {
        if (isDisabled()) {
            return;
        }
        if (getVariant() == Variant.FILTER) {
            super.fire();
        } else {
            fireEvent(new ActionEvent());
        }
    }

    /**
     * Removes the chip: calls {@link #onCloseProperty() onClose}, or, if there is none, removes
     * the chip from its parent pane. Triggered by the close icon of input chips.
     */
    public void close() {
        EventHandler<ActionEvent> handler = getOnClose();
        if (handler != null) {
            handler.handle(new ActionEvent(this, this));
        } else if (getParent() instanceof javafx.scene.layout.Pane pane) {
            pane.getChildren().remove(this);
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ChipSkin(this);
    }

    /** Convenience: sets the graphic to a Material {@link Icon}, or removes it for null. */
    public final void setIcon(String icon) {
        if (icon == null) {
            setGraphic(null);
            return;
        }
        // Chip icons have their own color role (primary for assist chips), set by the stylesheet.
        Icon node = new Icon(icon, 18);
        node.setFollowTextFill(false);
        node.getStyleClass().add("chip-icon");
        setGraphic(node);
    }

    /** The name of the icon set with {@link #setIcon(String)}, or null. */
    public final String getIcon() {
        return getGraphic() instanceof Icon icon && icon.getStyleClass().contains("chip-icon") ? icon.getName() : null;
    }

    /** Convenience: sets the graphic to an avatar (input chips). */
    public final void setAvatar(Node avatar) {
        setGraphic(avatar);
    }

    /** The graphic, when it is not an icon set with {@link #setIcon(String)}. */
    public final Node getAvatar() {
        return getIcon() == null ? getGraphic() : null;
    }

    public final ObjectProperty<Variant> variantProperty() {
        return variant;
    }

    public final Variant getVariant() {
        return variant.get();
    }

    public final void setVariant(Variant value) {
        variant.set(value);
    }

    /** Elevated chips have a shadow and no outline, for use over images or busy backgrounds. */
    public final BooleanProperty elevatedProperty() {
        return elevated;
    }

    public final boolean isElevated() {
        return elevated.get();
    }

    public final void setElevated(boolean value) {
        elevated.set(value);
    }

    /** Called when an input chip is closed. By default the chip removes itself from its parent. */
    public final ObjectProperty<EventHandler<ActionEvent>> onCloseProperty() {
        return onClose;
    }

    public final EventHandler<ActionEvent> getOnClose() {
        return onClose.get();
    }

    public final void setOnClose(EventHandler<ActionEvent> value) {
        onClose.set(value);
    }
}
