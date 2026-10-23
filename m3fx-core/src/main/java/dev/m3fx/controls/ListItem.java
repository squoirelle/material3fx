package dev.m3fx.controls;

import dev.m3fx.skins.ListItemSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.util.Callback;

import java.util.function.Function;

/**
 * Material list item: one to three lines of text with optional leading and trailing elements.
 *
 * <pre>{@code
 * ListItem item = new ListItem("Grace Hopper", "Rear admiral, COBOL");
 * item.setLeading(new Avatar("Grace Hopper"));
 * item.setTrailing(new Switch());
 * }</pre>
 * Put list items in a {@code VBox} with the {@code md-list} style class (clickable items then
 * support Up / Down arrow navigation), or render them in a {@link ListView} with
 * {@link #cellFactory(Function)}.
 */
public class ListItem extends Control {

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    private static final PseudoClass CLICKABLE = PseudoClass.getPseudoClass("clickable");

    private final StringProperty headline = new SimpleStringProperty(this, "headline");
    private final StringProperty supportingText = new SimpleStringProperty(this, "supportingText");
    private final StringProperty overline = new SimpleStringProperty(this, "overline");
    private final StringProperty trailingText = new SimpleStringProperty(this, "trailingText");
    private final ObjectProperty<Node> leading = new SimpleObjectProperty<>(this, "leading");
    private final ObjectProperty<Node> trailing = new SimpleObjectProperty<>(this, "trailing");
    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected") {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SELECTED, get());
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
        }
    };
    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>(this, "onAction") {
        @Override
        protected void invalidated() {
            EventHandler<ActionEvent> handler = get();
            // Only actions fired at this control: not those bubbling up from buttons or switches inside.
            setEventHandler(ActionEvent.ACTION, handler == null ? null : e -> {
                if (e.getTarget() == e.getSource()) {
                    handler.handle(e);
                }
            });
            boolean clickable = get() != null;
            pseudoClassStateChanged(CLICKABLE, clickable);
            setFocusTraversable(clickable);
            setAccessibleRole(clickable ? AccessibleRole.BUTTON : AccessibleRole.LIST_ITEM);
        }
    };

    public ListItem() {
        this(null, null);
    }

    public ListItem(String headline) {
        this(headline, null);
    }

    public ListItem(String headline, String supportingText) {
        getStyleClass().add("list-item");
        setFocusTraversable(false);
        setAccessibleRole(AccessibleRole.LIST_ITEM);
        this.headline.set(headline);
        this.supportingText.set(supportingText);
    }

    /**
     * A ListView cell factory rendering each item as a {@link ListItem}. Selection is shown with
     * the list's own selection colors.
     */
    public static <T> Callback<ListView<T>, ListCell<T>> cellFactory(Function<T, ListItem> factory) {
        return list -> new ListCell<>() {
            {
                getStyleClass().add("list-item-cell");
            }

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    ListItem listItem = factory.apply(item);
                    listItem.selectedProperty().bind(selectedProperty());
                    listItem.prefWidthProperty().bind(list.widthProperty().subtract(2));
                    setGraphic(listItem);
                }
            }
        };
    }

    /** Fires the item's action, as a click would. */
    public void fire() {
        if (!isDisabled() && getOnAction() != null) {
            fireEvent(new ActionEvent());
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ListItemSkin(this);
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        return switch (attribute) {
            case TEXT -> getSupportingText() == null ? getHeadline() : getHeadline() + ", " + getSupportingText();
            case SELECTED -> isSelected();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
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

    public final StringProperty overlineProperty() {
        return overline;
    }

    public final String getOverline() {
        return overline.get();
    }

    public final void setOverline(String value) {
        overline.set(value);
    }

    /** Short trailing text, e.g. a time or a count ("100+"). */
    public final StringProperty trailingTextProperty() {
        return trailingText;
    }

    public final String getTrailingText() {
        return trailingText.get();
    }

    public final void setTrailingText(String value) {
        trailingText.set(value);
    }

    /** Leading element: an {@link Icon}, {@link Avatar}, image or checkbox. */
    public final ObjectProperty<Node> leadingProperty() {
        return leading;
    }

    public final Node getLeading() {
        return leading.get();
    }

    public final void setLeading(Node value) {
        leading.set(value);
    }

    /** Convenience: leading icon by name. */
    public final void setLeadingIcon(String icon) {
        setLeading(icon == null ? null : new Icon(icon));
    }

    /** Trailing element: an {@link Icon}, checkbox, {@link Switch}... */
    public final ObjectProperty<Node> trailingProperty() {
        return trailing;
    }

    public final Node getTrailing() {
        return trailing.get();
    }

    public final void setTrailing(Node value) {
        trailing.set(value);
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

    /** Makes the item clickable, with hover / focus / ripple feedback, Enter, Space and arrow navigation. */
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
