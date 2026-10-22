package dev.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;

/**
 * Floating action button. With text it becomes an extended FAB.
 *
 * <pre>{@code
 * Fab compose = new Fab("edit");
 * Fab extended = new Fab("add", "New task");
 * }</pre>
 */
public class Fab extends Button {

    public enum Size { SMALL, REGULAR, LARGE }

    public enum Color { PRIMARY, SECONDARY, TERTIARY, SURFACE }

    private final Icon icon = new Icon();
    private final ObjectProperty<Size> size = new SimpleObjectProperty<>(this, "size", Size.REGULAR);
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color", Color.PRIMARY);

    public Fab() {
        this(null, null);
    }

    public Fab(String iconName) {
        this(iconName, null);
    }

    public Fab(String iconName, String text) {
        super(text);
        getStyleClass().add("fab");
        icon.setName(iconName);
        setGraphic(icon);
        textProperty().addListener(o -> updateClasses());
        size.addListener(o -> updateClasses());
        color.addListener(o -> updateClasses());
        updateClasses();
    }

    private void updateClasses() {
        getStyleClass().removeIf(c -> c.startsWith("fab-"));
        boolean extended = getText() != null && !getText().isEmpty();
        if (extended) {
            getStyleClass().add("fab-extended");
        } else {
            getStyleClass().add("fab-" + getSize().name().toLowerCase());
        }
        getStyleClass().add("fab-" + getColor().name().toLowerCase());
        icon.setSize(getSize() == Size.LARGE && !extended ? 36 : 24);
    }

    public Icon getIcon() {
        return icon;
    }

    public final void setIconName(String name) {
        icon.setName(name);
    }

    public final String getIconName() {
        return icon.getName();
    }

    public final ObjectProperty<Size> sizeProperty() {
        return size;
    }

    public final Size getSize() {
        return size.get();
    }

    public final void setSize(Size value) {
        size.set(value);
    }

    public final ObjectProperty<Color> colorProperty() {
        return color;
    }

    public final Color getColor() {
        return color.get();
    }

    public final void setColor(Color value) {
        color.set(value);
    }
}
