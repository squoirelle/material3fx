package dev.m3fx.controls;

import dev.m3fx.skins.SearchBarSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.function.Function;

/**
 * Material search bar: a pill-shaped {@link TextField} with a leading search (or navigation)
 * icon and trailing actions. Optionally shows suggestions in a dropdown as the user types
 * (Down opens / enters the list, Enter picks).
 * <p>
 * Submitting (Enter, or picking a suggestion) fires the standard {@code onAction}.
 *
 * <pre>{@code
 * SearchBar search = new SearchBar("Search units");
 * search.setSuggestionProvider(query -> units.stream().filter(u -> u.contains(query)).toList());
 * search.setOnAction(e -> filter(search.getText()));
 * }</pre>
 */
public class SearchBar extends TextField {

    private final ObjectProperty<Node> leading = new SimpleObjectProperty<>(this, "leading");
    private final ObservableList<Node> trailing = FXCollections.observableArrayList();
    private final ObjectProperty<Function<String, List<String>>> suggestionProvider =
            new SimpleObjectProperty<>(this, "suggestionProvider");

    public SearchBar() {
        this("Search");
    }

    public SearchBar(String placeholder) {
        // Not a ".text-field": the outlined look of plain text fields must not apply.
        getStyleClass().setAll("text-input", "search-bar");
        setPromptText(placeholder);
        setAccessibleText(placeholder);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SearchBarSkin(this);
    }

    /** Leading node; when null the skin shows a search icon. Typically a navigation {@link IconButton}. */
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

    /** Provides suggestions for the current text; shown in a dropdown below the bar. */
    public final ObjectProperty<Function<String, List<String>>> suggestionProviderProperty() {
        return suggestionProvider;
    }

    public final Function<String, List<String>> getSuggestionProvider() {
        return suggestionProvider.get();
    }

    public final void setSuggestionProvider(Function<String, List<String>> value) {
        suggestionProvider.set(value);
    }
}
