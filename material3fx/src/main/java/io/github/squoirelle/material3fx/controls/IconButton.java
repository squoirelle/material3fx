package io.github.squoirelle.material3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/**
 * Material icon button: a 40px circular button showing a single icon.
 * <p>
 * Set {@link #setToggle(boolean) toggle} to make it a toggle icon button; the selected state
 * shows the {@link #selectedIconProperty() selected icon} (by default the filled version of the
 * icon) and the variant's selected colors.
 */
public class IconButton extends Button {

    public enum Variant { STANDARD, FILLED, TONAL, OUTLINED }

    private final Icon icon = new Icon();
    private final StringProperty iconName = new SimpleStringProperty(this, "iconName");
    private final StringProperty selectedIcon = new SimpleStringProperty(this, "selectedIcon");
    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant", Variant.STANDARD);
    private final BooleanProperty toggle = new SimpleBooleanProperty(this, "toggle");
    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected");

    public IconButton() {
        this(null);
    }

    public IconButton(String iconName) {
        this(iconName, Variant.STANDARD);
    }

    public IconButton(String iconName, Variant variant) {
        getStyleClass().add("icon-button");
        setGraphic(icon);
        setText(null);
        setMnemonicParsing(false);
        this.iconName.addListener(o -> updateIcon());
        this.selectedIcon.addListener(o -> updateIcon());
        this.selected.addListener((obs, old, value) -> {
            pseudoClassStateChanged(PseudoClasses.SELECTED, value);
            updateIcon();
        });
        this.toggle.addListener((obs, old, value) -> {
            pseudoClassStateChanged(TOGGLE, value);
            setAccessibleRole(value ? javafx.scene.AccessibleRole.TOGGLE_BUTTON : javafx.scene.AccessibleRole.BUTTON);
        });
        this.variant.addListener((obs, old, value) -> updateVariant(old, value));
        setIconName(iconName);
        setVariant(variant);
        updateVariant(null, variant);
    }

    private static final javafx.css.PseudoClass TOGGLE = javafx.css.PseudoClass.getPseudoClass("toggle");

    /** Convenience: sets a tooltip, which icon-only buttons should always have. Also the accessible name. */
    public IconButton tooltip(String text) {
        setTooltip(new Tooltip(text));
        setAccessibleText(text);
        return this;
    }

    @Override
    public Object queryAccessibleAttribute(javafx.scene.AccessibleAttribute attribute, Object... parameters) {
        return switch (attribute) {
            case SELECTED -> isToggle() && isSelected();
            // Icon-only: without an explicit accessible text, fall back to the icon name.
            case TEXT -> getAccessibleText() != null ? getAccessibleText() : getIconName();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    @Override
    public void fire() {
        if (isToggle() && !isDisabled()) {
            setSelected(!isSelected());
        }
        super.fire();
    }

    private void updateIcon() {
        boolean sel = isToggle() && isSelected();
        String name = sel && getSelectedIcon() != null ? getSelectedIcon() : getIconName();
        icon.setName(name);
        icon.setFilled(sel && getSelectedIcon() == null);
    }

    private void updateVariant(Variant old, Variant value) {
        if (old != null) {
            getStyleClass().remove(old.name().toLowerCase());
        }
        if (value != null && value != Variant.STANDARD) {
            getStyleClass().add(value.name().toLowerCase());
        }
    }

    public Icon getIcon() {
        return icon;
    }

    public final StringProperty iconNameProperty() {
        return iconName;
    }

    public final String getIconName() {
        return iconName.get();
    }

    public final void setIconName(String value) {
        iconName.set(value);
    }

    /** Icon shown when selected; when null, the filled version of the icon is used. */
    public final StringProperty selectedIconProperty() {
        return selectedIcon;
    }

    public final String getSelectedIcon() {
        return selectedIcon.get();
    }

    public final void setSelectedIcon(String value) {
        selectedIcon.set(value);
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

    public final BooleanProperty toggleProperty() {
        return toggle;
    }

    public final boolean isToggle() {
        return toggle.get();
    }

    public final void setToggle(boolean value) {
        toggle.set(value);
    }

    public final BooleanProperty selectedProperty() {
        return selected;
    }

    public final boolean isSelected() {
        return selected.get();
    }

    public final void setSelected(boolean value) {
        selected.set(value);
    }
}
