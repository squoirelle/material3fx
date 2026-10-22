package dev.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Material chip: compact element representing an input, attribute or action.
 * <ul>
 *     <li>{@link Variant#ASSIST}: a smart action, like a button</li>
 *     <li>{@link Variant#FILTER}: toggles on/off, shows a check mark when selected</li>
 *     <li>{@link Variant#INPUT}: a piece of user input (e.g. a recipient), removable with its close icon</li>
 *     <li>{@link Variant#SUGGESTION}: a dynamically generated suggestion</li>
 * </ul>
 */
public class Chip extends HBox {

    public enum Variant { ASSIST, FILTER, INPUT, SUGGESTION }

    private final Label label = new Label();
    private final Icon leadingIcon = new Icon(null, 18);
    private final Icon closeIcon = new Icon("close", 18);
    private final HBox closeTarget = new HBox(closeIcon);
    private final StateLayer stateLayer = new StateLayer(this);

    private final StringProperty text = label.textProperty();
    private final StringProperty icon = new SimpleStringProperty(this, "icon");
    private final ObjectProperty<Node> avatar = new SimpleObjectProperty<>(this, "avatar");
    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant", Variant.ASSIST);
    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected");
    private final BooleanProperty elevated = new SimpleBooleanProperty(this, "elevated");
    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>(this, "onAction");
    private final ObjectProperty<EventHandler<ActionEvent>> onClose = new SimpleObjectProperty<>(this, "onClose");

    public Chip(String text) {
        this(text, Variant.ASSIST);
    }

    public Chip(String text, Variant variant) {
        this(text, null, variant);
    }

    public Chip(String text, String icon, Variant variant) {
        getStyleClass().add("chip");
        setAlignment(Pos.CENTER_LEFT);
        setFocusTraversable(true);
        label.getStyleClass().add("chip-label");
        leadingIcon.getStyleClass().add("chip-icon");
        closeIcon.getStyleClass().add("chip-close-icon");
        closeTarget.getStyleClass().add("chip-close");
        closeTarget.setAlignment(Pos.CENTER);
        getChildren().add(stateLayer);

        label.setMinWidth(Region.USE_PREF_SIZE);
        this.text.set(text);
        this.icon.set(icon);
        this.variant.set(variant);

        this.icon.addListener(o -> rebuild());
        this.avatar.addListener(o -> rebuild());
        this.variant.addListener((obs, old, value) -> {
            if (old != null) {
                getStyleClass().remove(old.name().toLowerCase());
            }
            getStyleClass().add(value.name().toLowerCase());
            rebuild();
        });
        getStyleClass().add(variant.name().toLowerCase());
        this.selected.addListener((obs, old, value) -> {
            pseudoClassStateChanged(PseudoClasses.SELECTED, value);
            rebuild();
        });
        this.elevated.addListener((obs, old, value) -> pseudoClassStateChanged(ELEVATED, value));

        addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.PRIMARY && !isDisabled()) {
                if (closeTarget.isVisible() && closeTarget.localToScene(closeTarget.getBoundsInLocal())
                        .contains(e.getSceneX(), e.getSceneY())) {
                    close();
                } else {
                    activate();
                }
                requestFocus();
            }
        });
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER) {
                activate();
                e.consume();
            } else if ((e.getCode() == KeyCode.BACK_SPACE || e.getCode() == KeyCode.DELETE) && getVariant() == Variant.INPUT) {
                close();
                e.consume();
            }
        });
        rebuild();
    }

    private static final javafx.css.PseudoClass ELEVATED = javafx.css.PseudoClass.getPseudoClass("elevated");

    private void activate() {
        if (getVariant() == Variant.FILTER) {
            setSelected(!isSelected());
        }
        EventHandler<ActionEvent> handler = getOnAction();
        if (handler != null) {
            handler.handle(new ActionEvent(this, this));
        }
    }

    private void close() {
        EventHandler<ActionEvent> handler = getOnClose();
        if (handler != null) {
            handler.handle(new ActionEvent(this, this));
        } else if (getParent() instanceof javafx.scene.layout.Pane pane) {
            pane.getChildren().remove(this);
        }
    }

    private void rebuild() {
        getChildren().retainAll(stateLayer);
        boolean hasLeading = false;
        if (getVariant() == Variant.FILTER && isSelected()) {
            leadingIcon.setName("check");
            getChildren().add(leadingIcon);
            hasLeading = true;
        } else if (getAvatar() != null) {
            getChildren().add(getAvatar());
            hasLeading = true;
        } else if (getIcon() != null) {
            leadingIcon.setName(getIcon());
            getChildren().add(leadingIcon);
            hasLeading = true;
        }
        getChildren().add(label);
        boolean hasClose = getVariant() == Variant.INPUT;
        closeTarget.setVisible(hasClose);
        if (hasClose) {
            getChildren().add(closeTarget);
        }
        pseudoClassStateChanged(LEADING, hasLeading);
        pseudoClassStateChanged(TRAILING, hasClose);
        pseudoClassStateChanged(AVATAR, getAvatar() != null && !(getVariant() == Variant.FILTER && isSelected()));
    }

    private static final javafx.css.PseudoClass LEADING = javafx.css.PseudoClass.getPseudoClass("leading");
    private static final javafx.css.PseudoClass TRAILING = javafx.css.PseudoClass.getPseudoClass("trailing");
    private static final javafx.css.PseudoClass AVATAR = javafx.css.PseudoClass.getPseudoClass("avatar");

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        stateLayer.resizeRelocate(0, 0, getWidth(), getHeight());
        stateLayer.toBack();
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

    /** Leading icon name. */
    public final StringProperty iconProperty() {
        return icon;
    }

    public final String getIcon() {
        return icon.get();
    }

    public final void setIcon(String value) {
        icon.set(value);
    }

    /** Leading avatar, typically an {@link Avatar}, used by input chips. */
    public final ObjectProperty<Node> avatarProperty() {
        return avatar;
    }

    public final Node getAvatar() {
        return avatar.get();
    }

    public final void setAvatar(Node value) {
        avatar.set(value);
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

    public final BooleanProperty selectedProperty() {
        return selected;
    }

    public final boolean isSelected() {
        return selected.get();
    }

    public final void setSelected(boolean value) {
        selected.set(value);
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

    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    public final EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    public final void setOnAction(EventHandler<ActionEvent> value) {
        onAction.set(value);
    }

    /** Called when an input chip's close icon is clicked. By default the chip removes itself from its parent. */
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
