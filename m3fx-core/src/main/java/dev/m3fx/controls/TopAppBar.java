package dev.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Material top app bar: navigation icon, title and actions.
 * <p>
 * When the content below is scrolled, the bar switches to its "scrolled" color
 * (surface container); link it to a scroll pane with {@link #bindScroll(ScrollPane)}.
 */
public class TopAppBar extends VBox {

    public enum Variant { SMALL, CENTER_ALIGNED, MEDIUM, LARGE }

    private final HBox row = new HBox();
    private final HBox actionsBox = new HBox();
    private final StackPane navigationBox = new StackPane();
    private final Label titleLabel = new Label();
    private final StringProperty title = titleLabel.textProperty();
    private final ObjectProperty<Node> navigationIcon = new SimpleObjectProperty<>(this, "navigationIcon");
    private final ObservableList<Node> actions = FXCollections.observableArrayList();
    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant", Variant.SMALL);
    private final BooleanProperty scrolled = new SimpleBooleanProperty(this, "scrolled");

    public TopAppBar() {
        this(null);
    }

    public TopAppBar(String title) {
        getStyleClass().add("top-app-bar");
        row.getStyleClass().add("top-app-bar-row");
        actionsBox.getStyleClass().add("top-app-bar-actions");
        titleLabel.getStyleClass().add("top-app-bar-title");
        row.setAlignment(Pos.CENTER_LEFT);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        navigationBox.setMaxWidth(USE_PREF_SIZE);
        actionsBox.setMaxWidth(USE_PREF_SIZE);
        this.title.set(title);
        navigationIcon.addListener(o -> rebuild());
        variant.addListener((obs, old, value) -> {
            getStyleClass().remove(old.name().toLowerCase().replace('_', '-'));
            getStyleClass().add(value.name().toLowerCase().replace('_', '-'));
            rebuild();
        });
        getStyleClass().add("small");
        actions.addListener((ListChangeListener<Node>) c -> actionsBox.getChildren().setAll(actions));
        scrolled.addListener((obs, old, value) -> pseudoClassStateChanged(PseudoClasses.SCROLLED, value));
        rebuild();
    }

    /** Convenience: sets a navigation icon button and returns it. */
    public IconButton setNavigationIcon(String icon) {
        IconButton button = new IconButton(icon);
        setNavigationIcon(button);
        return button;
    }

    /** Convenience: adds an action icon button and returns it. */
    public IconButton addAction(String icon, String tooltip) {
        IconButton button = new IconButton(icon).tooltip(tooltip);
        actions.add(button);
        return button;
    }

    /** Marks the bar as scrolled whenever {@code scrollPane} is not at the top. */
    public void bindScroll(ScrollPane scrollPane) {
        scrolled.bind(scrollPane.vvalueProperty().greaterThan(scrollPane.getVmin()));
    }

    private void rebuild() {
        getChildren().clear();
        row.getChildren().clear();
        navigationBox.getChildren().clear();
        if (getNavigationIcon() != null) {
            navigationBox.getChildren().add(getNavigationIcon());
        }
        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        switch (getVariant()) {
            case SMALL -> {
                row.getChildren().addAll(navigationBox, titleLabel, rightSpacer, actionsBox);
                getChildren().add(row);
            }
            case CENTER_ALIGNED -> {
                // Equal-width sides keep the title centered whatever the number of actions.
                StackPane left = new StackPane(navigationBox);
                StackPane right = new StackPane(actionsBox);
                left.setAlignment(Pos.CENTER_LEFT);
                right.setAlignment(Pos.CENTER_RIGHT);
                HBox.setHgrow(left, Priority.ALWAYS);
                HBox.setHgrow(right, Priority.ALWAYS);
                left.setPrefWidth(0);
                right.setPrefWidth(0);
                row.getChildren().addAll(left, titleLabel, right);
                getChildren().add(row);
            }
            case MEDIUM, LARGE -> {
                HBox.setHgrow(leftSpacer, Priority.ALWAYS);
                row.getChildren().addAll(navigationBox, leftSpacer, actionsBox);
                getChildren().addAll(row, titleLabel);
            }
        }
    }

    public final StringProperty titleProperty() {
        return title;
    }

    public final String getTitle() {
        return title.get();
    }

    public final void setTitle(String value) {
        title.set(value);
    }

    public Label getTitleLabel() {
        return titleLabel;
    }

    public final ObjectProperty<Node> navigationIconProperty() {
        return navigationIcon;
    }

    public final Node getNavigationIcon() {
        return navigationIcon.get();
    }

    public final void setNavigationIcon(Node value) {
        navigationIcon.set(value);
    }

    /** Trailing action nodes, typically {@link IconButton}s (at most three, then use an overflow menu). */
    public ObservableList<Node> getActions() {
        return actions;
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

    public final BooleanProperty scrolledProperty() {
        return scrolled;
    }

    public final boolean isScrolled() {
        return scrolled.get();
    }

    public final void setScrolled(boolean value) {
        scrolled.set(value);
    }
}
