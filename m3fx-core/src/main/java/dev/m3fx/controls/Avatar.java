package dev.m3fx.controls;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * A circular avatar showing an image, or initials on a colored background.
 * Used in lists, chips, search bars and top app bars.
 */
public class Avatar extends StackPane {

    private final Label initials = new Label();
    private final ImageView imageView = new ImageView();
    private final Circle clip = new Circle();
    private final DoubleProperty size = new SimpleDoubleProperty(this, "size", 40);
    private final ObjectProperty<Image> image = imageView.imageProperty();

    public Avatar(String name) {
        this(name, null);
    }

    public Avatar(String name, Image image) {
        getStyleClass().add("avatar");
        initials.getStyleClass().add("avatar-initials");
        setAlignment(Pos.CENTER);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        setClip(clip);
        setName(name);
        this.image.set(image);
        this.image.addListener(o -> update());
        size.addListener(o -> update());
        update();
    }

    public final void setName(String name) {
        String text = name == null ? "" : Arrays.stream(name.trim().split("\\s+"))
                .filter(s -> !s.isEmpty())
                .limit(2)
                .map(s -> s.substring(0, 1).toUpperCase(Locale.ROOT))
                .collect(Collectors.joining());
        initials.setText(text);
    }

    private void update() {
        double s = getSize();
        setMinSize(s, s);
        setPrefSize(s, s);
        setMaxSize(s, s);
        clip.setRadius(s / 2);
        clip.setCenterX(s / 2);
        clip.setCenterY(s / 2);
        imageView.setFitWidth(s);
        imageView.setFitHeight(s);
        initials.setStyle("-fx-font-size: " + Math.round(s * 0.4) + "px;");
        getChildren().setAll(getImage() != null ? imageView : initials);
    }

    public final DoubleProperty sizeProperty() {
        return size;
    }

    public final double getSize() {
        return size.get();
    }

    public final void setSize(double value) {
        size.set(value);
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
}
