package io.github.squoirelle.material3fx.skins;

import io.github.squoirelle.material3fx.controls.Icon;
import io.github.squoirelle.material3fx.controls.SplitButton;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

/**
 * Skin of {@link SplitButton}: a leading button (text and graphic, fires the control's action) and
 * a trailing button with a chevron that opens the items in a context menu, following the control's
 * {@code showing} state.
 */
public class SplitButtonSkin extends SkinBase<SplitButton> {

    private final HBox box = new HBox(2);
    private final Button leading = new Button();
    private final Button trailing = new Button();
    private final Icon chevron = new Icon("keyboard_arrow_down", 20);
    private final ContextMenu menu = new ContextMenu();
    private final ListChangeListener<MenuItem> itemsListener = c -> menu.getItems().setAll(getSkinnable().getItems());
    private final EventHandler<KeyEvent> keyHandler = this::onKeyPressed;

    public SplitButtonSkin(SplitButton control) {
        super(control);
        box.getStyleClass().add("split-button-content");
        leading.getStyleClass().add("split-button-leading");
        trailing.getStyleClass().add("split-button-trailing");
        leading.textProperty().bind(control.textProperty());
        leading.graphicProperty().bind(control.graphicProperty());
        leading.disableProperty().bind(control.disabledProperty());
        trailing.disableProperty().bind(control.disabledProperty());
        leading.setMnemonicParsing(false);
        trailing.setGraphic(chevron);
        trailing.setAccessibleText("More options");
        // Inner action events would bubble to the control and fire its onAction: consume them.
        leading.setOnAction(e -> {
            e.consume();
            control.fireEvent(new ActionEvent());
        });
        trailing.setOnAction(e -> {
            e.consume();
            if (control.isShowing()) {
                control.hide();
            } else {
                control.show();
            }
        });
        menu.getStyleClass().add("split-button-menu");
        menu.getItems().setAll(control.getItems());
        menu.setOnHidden(e -> control.hide());
        control.getItems().addListener(itemsListener);
        control.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        registerChangeListener(control.showingProperty(), o -> updateMenu());
        box.getChildren().addAll(leading, trailing);
        getChildren().add(box);
        updateMenu();
    }

    private void onKeyPressed(KeyEvent e) {
        if ((e.getCode() == KeyCode.DOWN && e.isAltDown()) || e.getCode() == KeyCode.F4) {
            getSkinnable().show();
            e.consume();
        }
    }

    private void updateMenu() {
        SplitButton control = getSkinnable();
        chevron.setRotate(control.isShowing() ? 180 : 0);
        trailing.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("showing"), control.isShowing());
        if (control.isShowing() && !menu.isShowing() && control.getScene() != null) {
            menu.show(box, Side.BOTTOM, 0, 4);
        } else if (!control.isShowing() && menu.isShowing()) {
            menu.hide();
        }
    }

    /** The leading button, which fires the control's action. */
    public Button getLeadingButton() {
        return leading;
    }

    /** The trailing button, which toggles the menu. */
    public Button getTrailingButton() {
        return trailing;
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
        SplitButton control = getSkinnable();
        control.getItems().removeListener(itemsListener);
        control.removeEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        menu.hide();
        leading.textProperty().unbind();
        leading.graphicProperty().unbind();
        leading.disableProperty().unbind();
        trailing.disableProperty().unbind();
        super.dispose();
    }
}
