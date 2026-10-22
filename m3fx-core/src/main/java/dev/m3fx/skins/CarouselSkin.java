package dev.m3fx.skins;

import dev.m3fx.Motion;
import dev.m3fx.controls.Carousel;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Skin of {@link Carousel}: items in rounded clips, laid out with a scroll offset. Items crossing
 * the trailing edge shrink down to the small width, items scrolled past the leading edge shrink on
 * that side; item content keeps its full width, as with Material's masking.
 */
public class CarouselSkin extends SkinBase<Carousel> {

    private static final double RADIUS = 28;

    private final DoubleProperty offset = new SimpleDoubleProperty(this, "offset");
    private final Rectangle clip = new Rectangle();
    private final Timeline snap = new Timeline();
    private final PauseTransition snapDelay = new PauseTransition(Duration.millis(180));
    private final ListChangeListener<Node> itemsListener = c -> rebuild();
    private final EventHandler<ScrollEvent> scrollHandler = this::onScroll;
    private final EventHandler<MouseEvent> pressHandler = this::onPressed;
    private final EventHandler<MouseEvent> dragHandler = this::onDragged;
    private final EventHandler<MouseEvent> releaseHandler = e -> snapTo(Math.round(offset.get() / step()));
    private final EventHandler<KeyEvent> keyHandler = this::onKeyPressed;
    private double dragStartX;
    private double dragStartOffset;

    public CarouselSkin(Carousel carousel) {
        super(carousel);
        carousel.setClip(clip);
        carousel.getItems().addListener(itemsListener);
        carousel.addEventHandler(ScrollEvent.SCROLL, scrollHandler);
        carousel.addEventHandler(MouseEvent.MOUSE_PRESSED, pressHandler);
        carousel.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragHandler);
        carousel.addEventHandler(MouseEvent.MOUSE_RELEASED, releaseHandler);
        carousel.addEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
        offset.addListener(o -> carousel.requestLayout());
        registerChangeListener(carousel.itemWidthProperty(), o -> carousel.requestLayout());
        registerChangeListener(carousel.smallItemWidthProperty(), o -> carousel.requestLayout());
        registerChangeListener(carousel.spacingProperty(), o -> carousel.requestLayout());
        snapDelay.setOnFinished(e -> snapTo(Math.round(offset.get() / step())));
        rebuild();
    }

    private void rebuild() {
        getChildren().clear();
        for (Node item : getSkinnable().getItems()) {
            StackPane wrapper = new StackPane(item);
            wrapper.getStyleClass().add("carousel-item");
            // Positioned by layoutChildren: unmanaged, so item positions never feed back into
            // the carousel's own size (which would make it re-layout endlessly).
            wrapper.setManaged(false);
            Rectangle itemClip = new Rectangle();
            itemClip.setArcWidth(RADIUS * 2);
            itemClip.setArcHeight(RADIUS * 2);
            wrapper.setClip(itemClip);
            getChildren().add(wrapper);
        }
        getSkinnable().requestLayout();
    }

    private void onScroll(ScrollEvent e) {
        double delta = Math.abs(e.getDeltaX()) > Math.abs(e.getDeltaY()) ? e.getDeltaX() : e.getDeltaY();
        setOffsetClamped(offset.get() - delta);
        snapDelay.playFromStart();
        e.consume();
    }

    private void onPressed(MouseEvent e) {
        snap.stop();
        dragStartX = e.getX();
        dragStartOffset = offset.get();
        getSkinnable().requestFocus();
    }

    private void onDragged(MouseEvent e) {
        setOffsetClamped(dragStartOffset - (e.getX() - dragStartX));
    }

    private void onKeyPressed(KeyEvent e) {
        long current = Math.round(offset.get() / step());
        switch (e.getCode()) {
            case RIGHT, KP_RIGHT -> snapTo(current + 1);
            case LEFT, KP_LEFT -> snapTo(current - 1);
            case HOME -> snapTo(0);
            case END -> snapTo(getSkinnable().getItems().size());
            default -> {
                return;
            }
        }
        e.consume();
    }

    private double step() {
        return effectiveItemWidth() + getSkinnable().getSpacing();
    }

    /** {@link Carousel#getItemWidth()} stretched so whole items plus one peek item exactly fill the width. */
    private double effectiveItemWidth() {
        Carousel c = getSkinnable();
        double w = c.getWidth() - c.snappedLeftInset() - c.snappedRightInset();
        if (w <= 0) {
            return c.getItemWidth();
        }
        double available = w - c.getSmallItemWidth() - c.getSpacing();
        int count = Math.max(1, (int) Math.floor((available + c.getSpacing()) / (c.getItemWidth() + c.getSpacing())));
        if (count >= c.getItems().size()) {
            return c.getItemWidth();
        }
        return (available - (count - 1) * c.getSpacing()) / count;
    }

    private double maxOffset() {
        Carousel c = getSkinnable();
        double content = c.getItems().size() * step() - c.getSpacing();
        return Math.max(0, content - (c.getWidth() - c.snappedLeftInset() - c.snappedRightInset()) + c.getSmallItemWidth());
    }

    private void setOffsetClamped(double value) {
        offset.set(Math.clamp(value, 0, maxOffset()));
    }

    private void snapTo(long index) {
        double target = Math.clamp(index * step(), 0, maxOffset());
        snap.stop();
        if (!Motion.shouldAnimate(getSkinnable())) {
            offset.set(target);
            return;
        }
        snap.getKeyFrames().setAll(new KeyFrame(Motion.MEDIUM_2, new KeyValue(offset, target, Motion.EMPHASIZED_DECELERATE)));
        snap.playFromStart();
    }

    @Override
    protected void layoutChildren(double left, double top, double w, double h) {
        Carousel c = getSkinnable();
        clip.setWidth(c.getWidth());
        clip.setHeight(c.getHeight());
        double x = left - offset.get();
        double right = left + w;
        double small = c.getSmallItemWidth();
        double itemSize = effectiveItemWidth();
        for (Node child : getChildren()) {
            StackPane wrapper = (StackPane) child;
            double width = itemSize;
            double itemLeft = x;
            if (x + width > right) {
                width = Math.max(small, right - x);
            }
            if (x < left) {
                double cut = Math.min(left - x, width - small);
                itemLeft = x + Math.max(0, cut);
                width -= Math.max(0, cut);
            }
            double finalWidth = Math.max(small, width);
            wrapper.resizeRelocate(itemLeft, top, finalWidth, h);
            Rectangle itemClip = (Rectangle) wrapper.getClip();
            itemClip.setWidth(finalWidth);
            itemClip.setHeight(h);
            Node content = wrapper.getChildren().getFirst();
            if (content instanceof Region region) {
                region.setMinWidth(itemSize);
            }
            wrapper.setVisible(itemLeft < right && itemLeft + finalWidth > left);
            x += itemSize + c.getSpacing();
        }
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        return 200 + top + bottom;
    }

    @Override
    protected double computeMinWidth(double height, double top, double right, double bottom, double left) {
        return left + getSkinnable().getSmallItemWidth() * 2 + getSkinnable().getSpacing() + right;
    }

    @Override
    protected double computeMinHeight(double width, double top, double right, double bottom, double left) {
        return top + 40 + bottom;
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        Carousel c = getSkinnable();
        return 3 * (c.getItemWidth() + c.getSpacing()) + left + right;
    }

    @Override
    public void dispose() {
        Carousel c = getSkinnable();
        c.getItems().removeListener(itemsListener);
        c.removeEventHandler(ScrollEvent.SCROLL, scrollHandler);
        c.removeEventHandler(MouseEvent.MOUSE_PRESSED, pressHandler);
        c.removeEventHandler(MouseEvent.MOUSE_DRAGGED, dragHandler);
        c.removeEventHandler(MouseEvent.MOUSE_RELEASED, releaseHandler);
        c.removeEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
        c.setClip(null);
        snap.stop();
        super.dispose();
    }
}
