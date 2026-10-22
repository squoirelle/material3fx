package dev.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Material search bar: a pill-shaped field with a leading search (or navigation) icon and
 * trailing actions. Optionally shows suggestions in a dropdown as the user types.
 *
 * <pre>{@code
 * SearchBar search = new SearchBar("Search units");
 * search.setSuggestionProvider(query -> units.stream().filter(u -> u.contains(query)).toList());
 * search.setOnSearch(query -> filter(query));
 * }</pre>
 */
public class SearchBar extends HBox {

    private final TextField editor = new TextField();
    private final HBox trailingBox = new HBox(4);
    private final ObjectProperty<Node> leading = new SimpleObjectProperty<>(this, "leading");
    private final ObservableList<Node> trailing = FXCollections.observableArrayList();
    private final ObjectProperty<Function<String, List<String>>> suggestionProvider = new SimpleObjectProperty<>(this, "suggestionProvider");
    private final ContextMenu suggestions = new ContextMenu();
    private Consumer<String> onSearch;
    private final StateLayer stateLayer = new StateLayer(this);

    public SearchBar() {
        this("Search");
    }

    public SearchBar(String placeholder) {
        getStyleClass().add("search-bar");
        editor.getStyleClass().add("search-bar-editor");
        trailingBox.getStyleClass().add("search-bar-trailing");
        suggestions.getStyleClass().add("search-suggestions");
        editor.setPromptText(placeholder);
        setAlignment(Pos.CENTER_LEFT);
        trailingBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(editor, Priority.ALWAYS);
        stateLayer.setRippleEnabled(false);
        Icon search = new Icon("search");
        search.getStyleClass().add("search-bar-icon");
        leading.set(search);
        leading.addListener(o -> rebuild());
        trailing.addListener((ListChangeListener<Node>) c -> trailingBox.getChildren().setAll(trailing));
        rebuild();

        editor.setOnAction(e -> {
            suggestions.hide();
            if (onSearch != null) {
                onSearch.accept(editor.getText());
            }
        });
        editor.textProperty().addListener((obs, old, text) -> updateSuggestions(text));
        editor.focusedProperty().addListener((obs, old, focused) -> pseudoClassStateChanged(PseudoClasses.FOCUSED, focused));
        editor.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DOWN && suggestions.isShowing() && !suggestions.getItems().isEmpty()) {
                suggestions.getSkin().getNode().lookup(".menu-item").requestFocus();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                suggestions.hide();
            }
        });
        setOnMouseClicked(e -> editor.requestFocus());
    }

    private void rebuild() {
        getChildren().clear();
        getChildren().add(stateLayer);
        if (getLeading() != null) {
            getChildren().add(getLeading());
        }
        getChildren().addAll(editor, trailingBox);
    }

    private void updateSuggestions(String text) {
        Function<String, List<String>> provider = getSuggestionProvider();
        if (provider == null || text == null || text.isBlank() || !editor.isFocused()) {
            suggestions.hide();
            return;
        }
        List<String> values = provider.apply(text);
        if (values == null || values.isEmpty()) {
            suggestions.hide();
            return;
        }
        suggestions.getItems().clear();
        for (String value : values.subList(0, Math.min(8, values.size()))) {
            ListItem item = new ListItem(value);
            item.setLeadingIcon("history");
            item.setPrefWidth(getWidth() - 16);
            CustomMenuItem menuItem = new CustomMenuItem(item, true);
            menuItem.getStyleClass().add("search-suggestion");
            menuItem.setOnAction(e -> {
                editor.setText(value);
                editor.positionCaret(value.length());
                suggestions.hide();
                if (onSearch != null) {
                    onSearch.accept(value);
                }
            });
            suggestions.getItems().add(menuItem);
        }
        if (!suggestions.isShowing()) {
            suggestions.show(this, Side.BOTTOM, 0, 4);
        }
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        stateLayer.resizeRelocate(0, 0, getWidth(), getHeight());
    }

    public TextField getEditor() {
        return editor;
    }

    public final StringProperty textProperty() {
        return editor.textProperty();
    }

    public final String getText() {
        return editor.getText();
    }

    public final void setText(String value) {
        editor.setText(value);
    }

    /** Leading node: a search {@link Icon} by default, or a navigation {@link IconButton}. */
    public final ObjectProperty<Node> leadingProperty() {
        return leading;
    }

    public final Node getLeading() {
        return leading.get();
    }

    public final void setLeading(Node value) {
        leading.set(value);
    }

    /** Trailing nodes: icon buttons (voice, filter...) or an {@link Avatar}. */
    public ObservableList<Node> getTrailing() {
        return trailing;
    }

    public final ObjectProperty<Function<String, List<String>>> suggestionProviderProperty() {
        return suggestionProvider;
    }

    public final Function<String, List<String>> getSuggestionProvider() {
        return suggestionProvider.get();
    }

    /** Provides suggestions for the current query; shown in a dropdown below the bar. */
    public final void setSuggestionProvider(Function<String, List<String>> value) {
        suggestionProvider.set(value);
    }

    /** Called when the user submits a query (Enter or a suggestion). */
    public void setOnSearch(Consumer<String> onSearch) {
        this.onSearch = onSearch;
    }

    /** Adds a handler called on Enter, in addition to {@link #setOnSearch(Consumer)}. */
    public void addOnAction(EventHandler<ActionEvent> handler) {
        editor.addEventHandler(ActionEvent.ACTION, handler);
    }
}
