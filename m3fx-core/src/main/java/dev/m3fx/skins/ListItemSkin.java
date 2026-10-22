package dev.m3fx.skins;

import dev.m3fx.controls.ListItem;
import dev.m3fx.controls.StateLayer;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Skin of {@link ListItem}: leading element, text column (overline, headline, supporting text),
 * trailing text and element, in a row. Clickable items get a state layer, and Up / Down move focus
 * to the previous / next clickable sibling item.
 */
public class ListItemSkin extends SkinBase<ListItem> {

    private final HBox row = new HBox();
    private final VBox texts = new VBox();
    private final Label overline = new Label();
    private final Label headline = new Label();
    private final Label supporting = new Label();
    private final Label trailingText = new Label();
    private final StateLayer stateLayer;
    private final ActionBehavior behavior;
    private final EventHandler<KeyEvent> arrowHandler = this::onKeyPressed;

    public ListItemSkin(ListItem item) {
        super(item);
        row.getStyleClass().add("list-item-row");
        texts.getStyleClass().add("list-item-texts");
        overline.getStyleClass().add("list-item-overline");
        headline.getStyleClass().add("list-item-headline");
        supporting.getStyleClass().add("list-item-supporting");
        trailingText.getStyleClass().add("list-item-trailing-text");
        row.setAlignment(Pos.CENTER_LEFT);
        texts.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(texts, Priority.ALWAYS);
        texts.setMaxWidth(Double.MAX_VALUE);
        texts.setMinWidth(0);
        supporting.setWrapText(true);
        // Wrapped supporting text needs its full height (three-line items).
        supporting.setMinHeight(Region.USE_PREF_SIZE);
        overline.textProperty().bind(item.overlineProperty());
        headline.textProperty().bind(item.headlineProperty());
        supporting.textProperty().bind(item.supportingTextProperty());
        trailingText.textProperty().bind(item.trailingTextProperty());

        stateLayer = new StateLayer(item);
        behavior = new ActionBehavior(item, () -> item.getOnAction() != null, item::fire);
        item.addEventHandler(KeyEvent.KEY_PRESSED, arrowHandler);
        for (ObservableValue<?> p : List.of(item.overlineProperty(), item.supportingTextProperty(), item.trailingTextProperty(),
                item.leadingProperty(), item.trailingProperty(), item.onActionProperty())) {
            registerChangeListener(p, o -> rebuild());
        }
        rebuild();
    }

    private void onKeyPressed(KeyEvent e) {
        if ((e.getCode() != KeyCode.UP && e.getCode() != KeyCode.DOWN) || getSkinnable().getOnAction() == null) {
            return;
        }
        Parent parent = getSkinnable().getParent();
        if (parent == null) {
            return;
        }
        List<Node> siblings = parent.getChildrenUnmodifiable().stream()
                .filter(n -> n instanceof ListItem li && li.getOnAction() != null && !li.isDisabled() && li.isVisible())
                .toList();
        int index = siblings.indexOf(getSkinnable());
        int target = e.getCode() == KeyCode.DOWN ? index + 1 : index - 1;
        if (index >= 0 && target >= 0 && target < siblings.size()) {
            siblings.get(target).requestFocus();
            e.consume();
        }
    }

    private void rebuild() {
        ListItem item = getSkinnable();
        texts.getChildren().clear();
        if (hasText(item.getOverline())) {
            texts.getChildren().add(overline);
        }
        texts.getChildren().add(headline);
        if (hasText(item.getSupportingText())) {
            texts.getChildren().add(supporting);
        }
        row.getChildren().clear();
        if (item.getLeading() != null) {
            keepPrefWidth(item.getLeading());
            row.getChildren().add(item.getLeading());
        }
        row.getChildren().add(texts);
        if (hasText(item.getTrailingText())) {
            row.getChildren().add(trailingText);
        }
        if (item.getTrailing() != null) {
            keepPrefWidth(item.getTrailing());
            row.getChildren().add(item.getTrailing());
        }
        getChildren().setAll(row);
        if (item.getOnAction() != null) {
            getChildren().addFirst(stateLayer);
        }
        int lines = (hasText(item.getOverline()) ? 1 : 0) + 1 + (hasText(item.getSupportingText()) ? 1 : 0);
        item.getStyleClass().removeAll("one-line", "two-line", "three-line");
        item.getStyleClass().add(switch (lines) {
            case 1 -> "one-line";
            case 2 -> "two-line";
            default -> "three-line";
        });
    }

    /** Leading and trailing elements keep their size; only the text column shrinks. */
    private static void keepPrefWidth(Node node) {
        if (node instanceof Region region) {
            region.setMinWidth(Region.USE_PREF_SIZE);
        }
    }

    private static boolean hasText(String text) {
        return text != null && !text.isEmpty();
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        row.resizeRelocate(x, y, w, h);
        stateLayer.resizeRelocate(0, 0, getSkinnable().getWidth(), getSkinnable().getHeight());
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return left + row.prefWidth(-1) + right;
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        double w = width < 0 ? -1 : Math.max(0, width - left - right);
        return top + row.prefHeight(w) + bottom;
    }

    @Override
    protected double computeMinHeight(double width, double top, double right, double bottom, double left) {
        return computePrefHeight(width, top, right, bottom, left);
    }

    @Override
    public void dispose() {
        behavior.dispose();
        getSkinnable().removeEventHandler(KeyEvent.KEY_PRESSED, arrowHandler);
        super.dispose();
    }
}
