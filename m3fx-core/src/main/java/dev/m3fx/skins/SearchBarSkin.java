package dev.m3fx.skins;

import dev.m3fx.controls.Icon;
import dev.m3fx.controls.ListItem;
import dev.m3fx.controls.SearchBar;
import dev.m3fx.controls.StateLayer;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.HitInfo;

import java.util.List;
import java.util.function.Function;

/**
 * Skin of {@link SearchBar}, built on {@link TextFieldSkin}: leading icon, the standard text area,
 * trailing nodes, a hover state layer, and the suggestions dropdown.
 */
public class SearchBarSkin extends TextFieldSkin {

    private static final double PADDING = 16;
    private static final double GAP = 16;

    private final Icon searchIcon = new Icon("search");
    private final HBox trailingBox = new HBox(4);
    private final StateLayer stateLayer;
    private final ContextMenu suggestions = new ContextMenu();
    private final ListChangeListener<Node> trailingListener = c -> trailingBox.getChildren().setAll(((SearchBar) getSkinnable()).getTrailing());
    private final EventHandler<KeyEvent> keyHandler = this::onKeyPressed;
    private Node leading;
    private double textX;

    public SearchBarSkin(SearchBar bar) {
        super(bar);
        searchIcon.getStyleClass().add("search-bar-icon");
        searchIcon.setFollowTextFill(false);
        trailingBox.getStyleClass().add("search-bar-trailing");
        trailingBox.setAlignment(Pos.CENTER_RIGHT);
        trailingBox.setManaged(false);
        suggestions.getStyleClass().add("search-suggestions");
        stateLayer = new StateLayer(bar);
        stateLayer.setRippleEnabled(false);
        stateLayer.setManaged(false);
        trailingBox.getChildren().setAll(bar.getTrailing());
        bar.getTrailing().addListener(trailingListener);
        bar.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        registerChangeListener(bar.leadingProperty(), o -> rebuild());
        registerChangeListener(bar.textProperty(), o -> updateSuggestions());
        registerChangeListener(bar.focusedProperty(), o -> {
            if (!bar.isFocused() && !suggestions.isShowing()) {
                suggestions.hide();
            }
        });
        rebuild();
    }

    private void rebuild() {
        SearchBar bar = (SearchBar) getSkinnable();
        if (leading != null) {
            getChildren().remove(leading);
        }
        getChildren().remove(stateLayer);
        getChildren().remove(trailingBox);
        leading = bar.getLeading() != null ? bar.getLeading() : searchIcon;
        leading.setManaged(false);
        getChildren().addFirst(stateLayer);
        getChildren().addAll(leading, trailingBox);
        bar.requestLayout();
    }

    private void onKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.DOWN) {
            updateSuggestions();
            if (suggestions.isShowing() && suggestions.getSkin() != null) {
                Node first = suggestions.getSkin().getNode().lookup(".menu-item");
                if (first != null) {
                    first.requestFocus();
                }
                e.consume();
            }
        } else if (e.getCode() == KeyCode.ESCAPE && suggestions.isShowing()) {
            suggestions.hide();
            e.consume();
        } else if (e.getCode() == KeyCode.ENTER) {
            suggestions.hide();
        }
    }

    private void updateSuggestions() {
        SearchBar bar = (SearchBar) getSkinnable();
        Function<String, List<String>> provider = bar.getSuggestionProvider();
        String text = bar.getText();
        if (provider == null || text == null || text.isBlank() || !bar.isFocused()) {
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
            item.setPrefWidth(bar.getWidth() - 16);
            CustomMenuItem menuItem = new CustomMenuItem(item, true);
            menuItem.getStyleClass().add("search-suggestion");
            menuItem.setOnAction(e -> {
                bar.setText(value);
                bar.positionCaret(value.length());
                suggestions.hide();
                bar.fireEvent(new ActionEvent());
            });
            suggestions.getItems().add(menuItem);
        }
        if (!suggestions.isShowing()) {
            suggestions.show(bar, Side.BOTTOM, 0, 4);
        }
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        double width = getSkinnable().getWidth();
        double height = getSkinnable().getHeight();
        stateLayer.resizeRelocate(0, 0, width, height);
        double lw = leading.prefWidth(-1);
        double lh = leading.prefHeight(lw);
        leading.resizeRelocate(PADDING, (height - lh) / 2, lw, lh);
        double tw = trailingBox.prefWidth(-1);
        double th = trailingBox.prefHeight(tw);
        trailingBox.resizeRelocate(width - tw - 4, (height - th) / 2, tw, th);
        textX = PADDING + lw + GAP;
        double textRight = tw > 0 ? tw + 4 + 8 : PADDING;
        super.layoutChildren(textX, 0, Math.max(0, width - textX - textRight), height);
    }

    @Override
    public HitInfo getIndex(double x, double y) {
        return super.getIndex(x - textX, y);
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return Math.max(360, super.computePrefWidth(height, 0, 0, 0, 0) + PADDING + 24 + GAP + trailingBox.prefWidth(-1) + 12);
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        return Math.max(56, super.computePrefHeight(width, 0, 0, 0, 0));
    }

    @Override
    public void dispose() {
        if (getSkinnable() == null) {
            return;
        }
        SearchBar bar = (SearchBar) getSkinnable();
        bar.getTrailing().removeListener(trailingListener);
        bar.removeEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        suggestions.hide();
        getChildren().removeAll(stateLayer, leading, trailingBox);
        super.dispose();
    }
}
