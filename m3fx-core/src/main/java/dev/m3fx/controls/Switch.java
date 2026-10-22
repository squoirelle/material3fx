package dev.m3fx.controls;

import dev.m3fx.skins.SwitchSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Skin;
import javafx.scene.control.ToggleButton;

/**
 * Material 3 switch. A {@link ToggleButton}: {@code selected}, {@code onAction}, toggle groups
 * and keyboard handling work as usual; the text, if any, is shown before the switch.
 *
 * <pre>{@code
 * Switch wifi = new Switch("Wi-Fi");
 * wifi.selectedProperty().addListener(...);
 * }</pre>
 */
public class Switch extends ToggleButton {

    private final BooleanProperty showIcons = new SimpleBooleanProperty(this, "showIcons");
    private final BooleanProperty showOnlySelectedIcon = new SimpleBooleanProperty(this, "showOnlySelectedIcon");

    public Switch() {
        this(null);
    }

    public Switch(String text) {
        super(text);
        getStyleClass().setAll("switch");
        setAccessibleRoleDescription("switch");
        setMnemonicParsing(false);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SwitchSkin(this);
    }

    /** Shows a check (on) / close (off) icon in the thumb. */
    public final BooleanProperty showIconsProperty() {
        return showIcons;
    }

    public final boolean isShowIcons() {
        return showIcons.get();
    }

    public final void setShowIcons(boolean value) {
        showIcons.set(value);
    }

    /** With {@link #showIconsProperty()}, shows the icon only in the on state. */
    public final BooleanProperty showOnlySelectedIconProperty() {
        return showOnlySelectedIcon;
    }

    public final boolean isShowOnlySelectedIcon() {
        return showOnlySelectedIcon.get();
    }

    public final void setShowOnlySelectedIcon(boolean value) {
        showOnlySelectedIcon.set(value);
    }
}
