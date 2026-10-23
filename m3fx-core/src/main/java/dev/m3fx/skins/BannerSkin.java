package dev.m3fx.skins;

import dev.m3fx.Motion;
import dev.m3fx.controls.Banner;
import dev.m3fx.controls.Icon;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * Skin of {@link Banner}: icon, message, actions. Showing / hiding animates a reveal factor that
 * scales the preferred height (time-driven, so it cannot loop).
 */
public class BannerSkin extends SkinBase<Banner> {

    private final HBox row = new HBox();
    private final StackPane iconBox = new StackPane();
    private final Icon icon = new Icon(null, 24);
    private final Label message = new Label();
    private final HBox actions = new HBox(8);
    private final Rectangle clip = new Rectangle();
    private final DoubleProperty reveal = new SimpleDoubleProperty(this, "reveal", 1);
    private final Timeline animation = new Timeline();
    private final ListChangeListener<Node> actionsListener = c -> actions.getChildren().setAll(getSkinnable().getActions());

    public BannerSkin(Banner banner) {
        super(banner);
        row.getStyleClass().add("banner-content");
        iconBox.getStyleClass().add("banner-icon-container");
        icon.getStyleClass().add("banner-icon");
        icon.setFollowTextFill(false);
        message.getStyleClass().add("banner-text");
        actions.getStyleClass().add("banner-actions");
        row.setAlignment(Pos.CENTER_LEFT);
        actions.setAlignment(Pos.CENTER_RIGHT);
        message.setWrapText(true);
        message.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(message, Priority.ALWAYS);
        iconBox.getChildren().add(icon);
        icon.nameProperty().bind(banner.iconProperty());
        message.textProperty().bind(banner.textProperty());
        banner.getActions().addListener(actionsListener);
        actions.getChildren().setAll(banner.getActions());
        banner.setClip(clip);
        getChildren().add(row);
        reveal.set(banner.isShowing() ? 1 : 0);
        banner.setVisible(banner.isShowing());
        reveal.addListener(o -> banner.requestLayout());
        registerChangeListener(banner.iconProperty(), o -> rebuild());
        registerChangeListener(banner.showingProperty(), o -> animate(banner.isShowing()));
        rebuild();
    }

    private void rebuild() {
        row.getChildren().clear();
        if (getSkinnable().getIcon() != null) {
            row.getChildren().add(iconBox);
        }
        row.getChildren().addAll(message, actions);
    }

    private void animate(boolean show) {
        Banner banner = getSkinnable();
        animation.stop();
        if (show) {
            banner.setVisible(true);
        }
        if (!Motion.shouldAnimate(banner)) {
            reveal.set(show ? 1 : 0);
            banner.setVisible(show);
            return;
        }
        animation.getKeyFrames().setAll(new KeyFrame(Motion.MEDIUM_2,
                new KeyValue(reveal, show ? 1 : 0, show ? Motion.EMPHASIZED_DECELERATE : Motion.EMPHASIZED_ACCELERATE)));
        animation.setOnFinished(e -> banner.setVisible(banner.isShowing()));
        animation.playFromStart();
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        Banner banner = getSkinnable();
        // Content keeps its full height, bottom-aligned: it slides up (clipped) as the banner collapses.
        double fullHeight = row.prefHeight(w);
        row.resizeRelocate(x, y + h - fullHeight, w, fullHeight);
        clip.setWidth(banner.getWidth());
        clip.setHeight(banner.getHeight());
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        double content = row.prefHeight(width < 0 ? -1 : width - left - right);
        return (top + content + bottom) * reveal.get();
    }

    @Override
    protected double computeMinHeight(double width, double top, double right, double bottom, double left) {
        return computePrefHeight(width, top, right, bottom, left);
    }

    @Override
    protected double computeMaxHeight(double width, double top, double right, double bottom, double left) {
        return computePrefHeight(width, top, right, bottom, left);
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return left + row.prefWidth(-1) + right;
    }

    @Override
    protected double computeMinWidth(double height, double top, double right, double bottom, double left) {
        return left + 120 + right;
    }

    @Override
    public void dispose() {
        animation.stop();
        getSkinnable().getActions().removeListener(actionsListener);
        getSkinnable().setClip(null);
        super.dispose();
    }
}
