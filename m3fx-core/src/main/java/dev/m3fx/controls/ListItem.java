package dev.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
 * Put list items in a {@code VBox} with the {@code md-list} style class, or use
 * {@link #cellFactory(Function)} to render them in a {@link ListView}.
 */
public class ListItem extends HBox {

    private final Label overline = new Label();
    private final Label headline = new Label();
    private final Label supporting = new Label();
    private final Label trailingText = new Label();
    private final VBox texts = new VBox();
    private final ObjectProperty<Node> leading = new SimpleObjectProperty<>(this, "leading");
    private final ObjectProperty<Node> trailing = new SimpleObjectProperty<>(this, "trailing");
    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected");
    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>(this, "onAction");
    private StateLayer stateLayer;

    public ListItem() {
        this(null, null);
    }

    public ListItem(String headline) {
        this(headline, null);
    }

    public ListItem(String headline, String supportingText) {
        getStyleClass().add("list-item");
        overline.getStyleClass().add("list-item-overline");
        this.headline.getStyleClass().add("list-item-headline");
        supporting.getStyleClass().add("list-item-supporting");
        trailingText.getStyleClass().add("list-item-trailing-text");
        texts.getStyleClass().add("list-item-texts");
        supporting.setWrapText(true);
        // Wrapped supporting text needs its full height (three-line items).
        supporting.setMinHeight(USE_PREF_SIZE);
        texts.setAlignment(Pos.CENTER_LEFT);
        setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(texts, Priority.ALWAYS);
        texts.setMaxWidth(Double.MAX_VALUE);
        texts.setMinWidth(0);

        this.headline.setText(headline);
        supporting.setText(supportingText);
        overline.textProperty().addListener(o -> rebuild());
        supporting.textProperty().addListener(o -> rebuild());
        trailingText.textProperty().addListener(o -> rebuild());
        leading.addListener(o -> rebuild());
        trailing.addListener(o -> rebuild());
        selected.addListener((obs, old, value) -> pseudoClassStateChanged(PseudoClasses.SELECTED, value));
        onAction.addListener((obs, old, handler) -> setClickable(handler != null));

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
        rebuild();
    }

    /**
     * A ListView cell factory rendering each item as a {@link ListItem}. Selection is shown with
     * the list's selection colors.
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

    private void setClickable(boolean clickable) {
        pseudoClassStateChanged(PseudoClasses.CLICKABLE, clickable);
        setFocusTraversable(clickable);
        if (clickable && stateLayer == null) {
            stateLayer = new StateLayer(this);
            getChildren().addFirst(stateLayer);
        } else if (!clickable && stateLayer != null) {
            getChildren().remove(stateLayer);
            stateLayer = null;
        }
    }

    private void rebuild() {
        getChildren().clear();
        if (stateLayer != null) {
            getChildren().add(stateLayer);
        }
        texts.getChildren().clear();
        if (hasText(overline)) {
            texts.getChildren().add(overline);
        }
        texts.getChildren().add(headline);
        if (hasText(supporting)) {
            texts.getChildren().add(supporting);
        }
        if (getLeading() != null) {
            keepPrefWidth(getLeading());
            getChildren().add(getLeading());
        }
        getChildren().add(texts);
        if (hasText(trailingText)) {
            getChildren().add(trailingText);
        }
        if (getTrailing() != null) {
            keepPrefWidth(getTrailing());
            getChildren().add(getTrailing());
        }
        int lines = (hasText(overline) ? 1 : 0) + 1 + (hasText(supporting) ? 1 : 0);
        getStyleClass().removeAll("one-line", "two-line", "three-line");
        getStyleClass().add(switch (lines) {
            case 1 -> "one-line";
            case 2 -> "two-line";
            default -> "three-line";
        });
    }

    /** Leading and trailing elements keep their size; only the text column shrinks. */
    private static void keepPrefWidth(Node node) {
        if (node instanceof javafx.scene.layout.Region region) {
            region.setMinWidth(USE_PREF_SIZE);
        }
    }

    private static boolean hasText(Label label) {
        return label.getText() != null && !label.getText().isEmpty();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        if (stateLayer != null) {
            stateLayer.resizeRelocate(0, 0, getWidth(), getHeight());
        }
    }

    public final StringProperty headlineProperty() {
        return headline.textProperty();
    }

    public final String getHeadline() {
        return headline.getText();
    }

    public final void setHeadline(String value) {
        headline.setText(value);
    }

    public final StringProperty supportingTextProperty() {
        return supporting.textProperty();
    }

    public final String getSupportingText() {
        return supporting.getText();
    }

    public final void setSupportingText(String value) {
        supporting.setText(value);
    }

    public final StringProperty overlineProperty() {
        return overline.textProperty();
    }

    public final void setOverline(String value) {
        overline.setText(value);
    }

    /** Short trailing text, e.g. a time or a count ("100+"). */
    public final StringProperty trailingTextProperty() {
        return trailingText.textProperty();
    }

    public final void setTrailingText(String value) {
        trailingText.setText(value);
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
        setLeading(new Icon(icon));
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

    /** Makes the item clickable, with hover / focus / ripple feedback. */
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
