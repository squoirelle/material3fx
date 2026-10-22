package dev.m3fx.skins;

import dev.m3fx.controls.Icon;
import dev.m3fx.controls.SegmentedButton;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Skin of {@link SegmentedButton}: the buttons side by side, with position style classes
 * ({@code first}, {@code last}, {@code only}) for the rounded ends, and a check mark on selected
 * segments. In multi-select mode, Left / Right move focus between segments.
 */
public class SegmentedButtonSkin extends SkinBase<SegmentedButton> {

    private final HBox box = new HBox();
    private final Map<ToggleButton, Node> originalGraphics = new HashMap<>();
    private final Map<ToggleButton, InvalidationListener> selectionListeners = new HashMap<>();
    private final ListChangeListener<ToggleButton> buttonsListener = c -> rebuild();
    private final EventHandler<KeyEvent> arrowHandler = this::onKeyPressed;

    public SegmentedButtonSkin(SegmentedButton control) {
        super(control);
        box.getStyleClass().add("segmented-button-box");
        box.setFillHeight(true);
        getChildren().add(box);
        control.getButtons().addListener(buttonsListener);
        control.addEventFilter(KeyEvent.KEY_PRESSED, arrowHandler);
        registerChangeListener(control.showCheckProperty(), o -> control.getButtons().forEach(this::updateGraphic));
        rebuild();
    }

    private void onKeyPressed(KeyEvent e) {
        // In a toggle group JavaFX already moves with the arrows; independent toggles need this.
        if (!getSkinnable().isMultiSelect() || (e.getCode() != KeyCode.LEFT && e.getCode() != KeyCode.RIGHT)) {
            return;
        }
        List<ToggleButton> buttons = getSkinnable().getButtons();
        Node owner = getSkinnable().getScene() == null ? null : getSkinnable().getScene().getFocusOwner();
        int index = owner instanceof ToggleButton button ? buttons.indexOf(button) : -1;
        int target = index + (e.getCode() == KeyCode.RIGHT ? 1 : -1);
        if (index >= 0 && target >= 0 && target < buttons.size()) {
            buttons.get(target).requestFocus();
            e.consume();
        }
    }

    private void rebuild() {
        List<ToggleButton> buttons = getSkinnable().getButtons();
        for (ToggleButton old : List.copyOf(selectionListeners.keySet())) {
            if (!buttons.contains(old)) {
                old.selectedProperty().removeListener(selectionListeners.remove(old));
                old.setGraphic(originalGraphics.remove(old));
                old.getStyleClass().removeAll("segment", "first", "last", "only");
            }
        }
        for (int i = 0; i < buttons.size(); i++) {
            ToggleButton button = buttons.get(i);
            if (!selectionListeners.containsKey(button)) {
                originalGraphics.put(button, button.getGraphic());
                InvalidationListener listener = o -> updateGraphic(button);
                selectionListeners.put(button, listener);
                button.selectedProperty().addListener(listener);
                if (!button.getStyleClass().contains("segment")) {
                    button.getStyleClass().add("segment");
                }
                button.setMaxHeight(Double.MAX_VALUE);
                updateGraphic(button);
            }
            button.getStyleClass().removeAll("first", "last", "only");
            if (buttons.size() == 1) {
                button.getStyleClass().add("only");
            } else if (i == 0) {
                button.getStyleClass().add("first");
            } else if (i == buttons.size() - 1) {
                button.getStyleClass().add("last");
            }
        }
        box.getChildren().setAll(buttons);
    }

    private void updateGraphic(ToggleButton button) {
        if (button.isSelected() && getSkinnable().isShowCheck()) {
            button.setGraphic(new Icon("check", 18));
        } else {
            button.setGraphic(originalGraphics.get(button));
        }
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        box.resizeRelocate(x, y, w, h);
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return left + box.prefWidth(-1) + right;
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        return top + box.prefHeight(-1) + bottom;
    }

    @Override
    protected double computeMaxWidth(double height, double top, double right, double bottom, double left) {
        return computePrefWidth(height, top, right, bottom, left);
    }

    @Override
    protected double computeMaxHeight(double width, double top, double right, double bottom, double left) {
        return computePrefHeight(width, top, right, bottom, left);
    }

    @Override
    public void dispose() {
        getSkinnable().getButtons().removeListener(buttonsListener);
        getSkinnable().removeEventFilter(KeyEvent.KEY_PRESSED, arrowHandler);
        selectionListeners.forEach((button, listener) -> {
            button.selectedProperty().removeListener(listener);
            button.setGraphic(originalGraphics.get(button));
        });
        super.dispose();
    }
}
