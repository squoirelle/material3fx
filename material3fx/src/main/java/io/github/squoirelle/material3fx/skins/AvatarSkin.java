package io.github.squoirelle.material3fx.skins;

import io.github.squoirelle.material3fx.controls.Avatar;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

/** Skin of {@link Avatar}: the image, or initials, clipped to a circle. */
public class AvatarSkin extends SkinBase<Avatar> {

    private final Label initials = new Label();
    private final ImageView imageView = new ImageView();
    private final Circle clip = new Circle();

    public AvatarSkin(Avatar avatar) {
        super(avatar);
        initials.getStyleClass().add("avatar-initials");
        imageView.setSmooth(true);
        avatar.setClip(clip);
        imageView.imageProperty().bind(avatar.imageProperty());
        registerChangeListener(avatar.nameProperty(), o -> update());
        registerChangeListener(avatar.imageProperty(), o -> update());
        registerChangeListener(avatar.sizeProperty(), o -> update());
        update();
    }

    private void update() {
        Avatar avatar = getSkinnable();
        initials.setText(avatar.getInitials());
        initials.setStyle("-fx-font-size: " + Math.round(avatar.getSize() * 0.4) + "px;");
        getChildren().setAll(avatar.getImage() != null ? imageView : initials);
        avatar.requestLayout();
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        double s = Math.min(getSkinnable().getWidth(), getSkinnable().getHeight());
        clip.setRadius(s / 2);
        clip.setCenterX(getSkinnable().getWidth() / 2);
        clip.setCenterY(getSkinnable().getHeight() / 2);
        imageView.setFitWidth(getSkinnable().getWidth());
        imageView.setFitHeight(getSkinnable().getHeight());
        imageView.relocate(0, 0);
        layoutInArea(initials, 0, 0, getSkinnable().getWidth(), getSkinnable().getHeight(), 0,
                javafx.geometry.HPos.CENTER, javafx.geometry.VPos.CENTER);
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return getSkinnable().getSize();
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        return getSkinnable().getSize();
    }

    @Override
    protected double computeMinWidth(double height, double top, double right, double bottom, double left) {
        return getSkinnable().getSize();
    }

    @Override
    protected double computeMinHeight(double width, double top, double right, double bottom, double left) {
        return getSkinnable().getSize();
    }

    @Override
    protected double computeMaxWidth(double height, double top, double right, double bottom, double left) {
        return getSkinnable().getSize();
    }

    @Override
    protected double computeMaxHeight(double width, double top, double right, double bottom, double left) {
        return getSkinnable().getSize();
    }

    @Override
    public void dispose() {
        getSkinnable().setClip(null);
        super.dispose();
    }
}
