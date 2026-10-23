package dev.m3fx.controls;

import dev.m3fx.skins.SplitButtonSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
import javafx.scene.control.SplitMenuButton;

import java.util.Locale;

/**
 * Material split button: a main action and, separated by a small gap, a menu of related actions
 * ("Export" + PDF / CSV / Print). A {@link SplitMenuButton}: {@code onAction}, {@link #getItems()},
 * {@link #show()} / {@link #hide()} work as usual.
 * <p>
 * Keyboard: the two parts are separately focusable; Space / Enter on the menu part, or Alt+Down /
 * F4 anywhere, opens the menu.
 */
public class SplitButton extends SplitMenuButton {

    public enum Variant { FILLED, TONAL, OUTLINED, ELEVATED }

    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant");

    public SplitButton() {
        this(null, null);
    }

    public SplitButton(String text, Node graphic, MenuItem... items) {
        super(items);
        // Not a ".split-menu-button": the standard control's restyle must not apply.
        getStyleClass().setAll("split-button");
        setText(text);
        setGraphic(graphic);
        setMnemonicParsing(false);
        variant.addListener((obs, old, value) -> {
            if (old != null) {
                getStyleClass().remove(old.name().toLowerCase(Locale.ROOT));
            }
            if (value != null) {
                getStyleClass().add(value.name().toLowerCase(Locale.ROOT));
            }
        });
        variant.set(Variant.FILLED);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SplitButtonSkin(this);
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
}
