package io.github.squoirelle.material3fx.controls;

import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;

/**
 * A section header for menus ({@code ContextMenu}, {@code MenuButton}, {@code Menu}): a short,
 * non-selectable title above a group of items, as in Material menus with sections. Keyboard
 * navigation skips it.
 *
 * <pre>{@code
 * contextMenu.getItems().addAll(
 *         new MenuHeader("Edit"), cut, copy, paste,
 *         new SeparatorMenuItem(),
 *         new MenuHeader("Share"), email, link);
 * }</pre>
 */
public class MenuHeader extends CustomMenuItem {

    private final Label label = new Label();

    public MenuHeader(String text) {
        getStyleClass().add("menu-header");
        label.getStyleClass().add("menu-header-label");
        label.setText(text);
        setContent(label);
        setHideOnClick(false);
        // Disabled items are skipped by menu keyboard navigation; the stylesheet keeps it fully visible.
        setDisable(true);
    }

    public final String getTitle() {
        return label.getText();
    }

    public final void setTitle(String value) {
        label.setText(value);
    }
}
