package dev.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

/**
 * Material card: a container for content and actions about a single subject.
 * <p>
 * Content is clipped to the card's rounded corners, so media (images) can bleed to the edges.
 * Setting an {@link #onActionProperty() action} makes the whole card clickable, with hover,
 * focus and ripple feedback.
 */
public class Card extends VBox {

    public enum Variant { ELEVATED, FILLED, OUTLINED }

    private static final double RADIUS = 12;

    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant");
    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>(this, "onAction");
    private final Rectangle clip = new Rectangle();
    private StateLayer stateLayer;

    public Card() {
        this(Variant.OUTLINED);
    }

    public Card(Variant variant, javafx.scene.Node... children) {
        super(children);
        getStyleClass().add("card");
        clip.setArcWidth(RADIUS * 2);
        clip.setArcHeight(RADIUS * 2);
        setClip(clip);
        this.variant.addListener((obs, old, value) -> {
            if (old != null) {
                getStyleClass().remove(old.name().toLowerCase());
            }
            if (value != null) {
                getStyleClass().add(value.name().toLowerCase());
            }
        });
        this.variant.set(variant);
        onAction.addListener((obs, old, handler) -> updateClickable(handler != null));
        addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.PRIMARY && getOnAction() != null && e.isStillSincePress()) {
                getOnAction().handle(new ActionEvent(this, this));
            }
        });
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if ((e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) && getOnAction() != null) {
                getOnAction().handle(new ActionEvent(this, this));
                e.consume();
            }
        });
    }

    private void updateClickable(boolean clickable) {
        pseudoClassStateChanged(PseudoClasses.CLICKABLE, clickable);
        setFocusTraversable(clickable);
        if (clickable && stateLayer == null) {
            stateLayer = new StateLayer(this);
            stateLayer.getStyleClass().add("card-state-layer");
            getChildren().add(stateLayer);
        } else if (!clickable && stateLayer != null) {
            getChildren().remove(stateLayer);
            stateLayer = null;
        }
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        clip.setWidth(getWidth());
        clip.setHeight(getHeight());
        if (stateLayer != null) {
            // On top of the content: Material cards overlay their state layer on the whole card.
            stateLayer.resizeRelocate(0, 0, getWidth(), getHeight());
            if (getChildren().getLast() != stateLayer) {
                stateLayer.toFront();
            }
        }
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
