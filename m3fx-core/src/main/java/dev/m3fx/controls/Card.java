package dev.m3fx.controls;

import dev.m3fx.skins.CardSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.VBox;

/**
 * Material card: a container for content and actions about a single subject.
 * <p>
 * Like {@link javafx.scene.control.TitledPane} it has a single {@link #contentProperty() content}
 * node; the varargs constructor stacks several nodes in a {@code VBox} for convenience. Content is
 * clipped to the rounded corners, so media can bleed to the edges (add the {@code flush} style class
 * to remove the padding). Setting an {@link #onActionProperty() action} makes the whole card
 * clickable, with hover, focus and ripple feedback, Enter and Space.
 */
public class Card extends Control {

    public enum Variant { ELEVATED, FILLED, OUTLINED }

    private static final PseudoClass CLICKABLE = PseudoClass.getPseudoClass("clickable");

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");
    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant");
    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>(this, "onAction") {
        @Override
        protected void invalidated() {
            setEventHandler(ActionEvent.ACTION, get());
            boolean clickable = get() != null;
            pseudoClassStateChanged(CLICKABLE, clickable);
            setFocusTraversable(clickable);
            setAccessibleRole(clickable ? AccessibleRole.BUTTON : AccessibleRole.NODE);
        }
    };

    public Card() {
        this(Variant.OUTLINED, (Node) null);
    }

    public Card(Variant variant, Node content) {
        getStyleClass().add("card");
        setFocusTraversable(false);
        this.variant.addListener((obs, old, value) -> {
            if (old != null) {
                getStyleClass().remove(old.name().toLowerCase());
            }
            if (value != null) {
                getStyleClass().add(value.name().toLowerCase());
            }
        });
        this.variant.set(variant);
        this.content.set(content);
    }

    /** Stacks the nodes in a {@code VBox} (style class {@code card-content}) used as content. */
    public Card(Variant variant, Node... children) {
        this(variant, stack(children));
    }

    private static Node stack(Node... children) {
        VBox box = new VBox(children);
        box.getStyleClass().add("card-content");
        return box;
    }

    /** Fires the card's action, as a click would. */
    public void fire() {
        if (!isDisabled() && getOnAction() != null) {
            fireEvent(new ActionEvent());
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CardSkin(this);
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

    public final ObjectProperty<Variant> variantProperty() {
        return variant;
    }

    public final Variant getVariant() {
        return variant.get();
    }

    public final void setVariant(Variant value) {
        variant.set(value);
    }

    /** Makes the card clickable. */
    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    public final EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    public final void setOnAction(EventHandler<ActionEvent> value) {
        onAction.set(value);
    }
}
