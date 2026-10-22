package dev.m3fx.skins;

import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.function.BooleanSupplier;

/**
 * Button-like input handling for controls that are not buttons but can be activated (clickable
 * cards and list items): primary click, Enter or Space runs the action, when enabled.
 */
final class ActionBehavior {

    private final Control control;
    private final BooleanSupplier enabled;
    private final Runnable action;
    private final EventHandler<MouseEvent> clickHandler;
    private final EventHandler<KeyEvent> keyHandler;

    ActionBehavior(Control control, BooleanSupplier enabled, Runnable action) {
        this.control = control;
        this.enabled = enabled;
        this.action = action;
        clickHandler = e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.isStillSincePress() && isActive()) {
                control.requestFocus();
                action.run();
            }
        };
        keyHandler = e -> {
            if ((e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) && isActive()) {
                action.run();
                e.consume();
            }
        };
        control.addEventHandler(MouseEvent.MOUSE_CLICKED, clickHandler);
        control.addEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
    }

    private boolean isActive() {
        return !control.isDisabled() && enabled.getAsBoolean();
    }

    void dispose() {
        control.removeEventHandler(MouseEvent.MOUSE_CLICKED, clickHandler);
        control.removeEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
    }
}
