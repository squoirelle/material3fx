package dev.m3fx.controls;

import dev.m3fx.skins.AvatarSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * A circular avatar showing an image, or the initials of a name on a colored background.
 * Used in lists, chips, search bars and top app bars.
 */
public class Avatar extends Control {

    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final ObjectProperty<Image> image = new SimpleObjectProperty<>(this, "image");
    private final DoubleProperty size = new SimpleDoubleProperty(this, "size", 40);

    public Avatar() {
        this(null, null);
    }

    public Avatar(String name) {
        this(name, null);
    }

    public Avatar(String name, Image image) {
        getStyleClass().add("avatar");
        setAccessibleRole(AccessibleRole.IMAGE_VIEW);
        accessibleTextProperty().bind(this.name);
        this.name.set(name);
        this.image.set(image);
        setFocusTraversable(false);
    }

    /** Up to two initials of the name ("Grace Hopper" gives "GH"). */
    public String getInitials() {
        String n = getName();
        if (n == null) {
            return "";
        }
        return Arrays.stream(n.trim().split("\\s+"))
                .filter(s -> !s.isEmpty())
                .limit(2)
                .map(s -> s.substring(0, 1).toUpperCase(Locale.ROOT))
                .collect(Collectors.joining());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AvatarSkin(this);
    }

    public final StringProperty nameProperty() {
        return name;
    }

    public final String getName() {
        return name.get();
    }

    public final void setName(String value) {
        name.set(value);
    }

    public final ObjectProperty<Image> imageProperty() {
        return image;
    }

    public final Image getImage() {
        return image.get();
    }

    public final void setImage(Image value) {
        image.set(value);
    }

    /** Diameter in pixels (default 40). */
    public final DoubleProperty sizeProperty() {
        return size;
    }

    public final double getSize() {
        return size.get();
    }

    public final void setSize(double value) {
        size.set(value);
    }
}
