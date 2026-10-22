package dev.m3fx.controls;

import dev.m3fx.Motion;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * Material carousel (multi-browse): a horizontally scrolling row of items with rounded corners.
 * Items at the trailing edge are compressed into smaller "peek" items, hinting there is more
 * content, and scrolling snaps to item boundaries. Scroll with the mouse wheel, by dragging, or
 * with the arrow keys.
 */
public class Carousel extends Region {

    private static final double RADIUS = 28;

    private final ObservableList<Node> items = FXCollections.observableArrayList();
    private final DoubleProperty itemWidth = new SimpleDoubleProperty(this, "itemWidth", 220);
    private final DoubleProperty smallItemWidth = new SimpleDoubleProperty(this, "smallItemWidth", 56);
    private final DoubleProperty spacing = new SimpleDoubleProperty(this, "spacing", 8);
    private final DoubleProperty offset = new SimpleDoubleProperty(this, "offset");
    private final Rectangle clip = new Rectangle();
    private final Timeline snap = new Timeline();
    private double dragStartX;
    private double dragStartOffset;

    public Carousel() {
        getStyleClass().add("carousel");
        setClip(clip);
        setFocusTraversable(true);
        items.addListener((ListChangeListener<Node>) c -> {
            getChildren().clear();
            for (Node item : items) {
                StackPane wrapper = new StackPane(item);
                wrapper.getStyleClass().add("carousel-item");
                Rectangle itemClip = new Rectangle();
                itemClip.setArcWidth(RADIUS * 2);
                itemClip.setArcHeight(RADIUS * 2);
                wrapper.setClip(itemClip);
                getChildren().add(wrapper);
            }
            requestLayout();
        });
        offset.addListener(o -> requestLayout());
        itemWidth.addListener(o -> requestLayout());
        addEventHandler(ScrollEvent.SCROLL, e -> {
            double delta = Math.abs(e.getDeltaX()) > Math.abs(e.getDeltaY()) ? e.getDeltaX() : e.getDeltaY();
            setOffsetClamped(offset.get() - delta);
            e.consume();
            scheduleSnap();
        });
        addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            snap.stop();
            dragStartX = e.getX();
            dragStartOffset = offset.get();
            requestFocus();
        });
        addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> setOffsetClamped(dragStartOffset - (e.getX() - dragStartX)));
        addEventHandler(MouseEvent.MOUSE_RELEASED, e -> snapTo(Math.round(offset.get() / step())));
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.RIGHT) {
                next();
                e.consume();
            } else if (e.getCode() == KeyCode.LEFT) {
                previous();
                e.consume();
            }
        });
    }

    private final javafx.animation.PauseTransition snapDelay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(180));

    private void scheduleSnap() {
        snapDelay.setOnFinished(e -> snapTo(Math.round(offset.get() / step())));
        snapDelay.playFromStart();
    }

    private double step() {
        return effectiveItemWidth() + getSpacing();
    }

    /**
     * The item width actually used: {@link #getItemWidth()} stretched so that whole items plus
     * one small "peek" item exactly fill the carousel when it is scrolled to the start.
     */
    private double effectiveItemWidth() {
        double w = getWidth() - snappedLeftInset() - snappedRightInset();
        if (w <= 0) {
            return getItemWidth();
        }
        double available = w - getSmallItemWidth() - getSpacing();
        int count = Math.max(1, (int) Math.floor((available + getSpacing()) / (getItemWidth() + getSpacing())));
        if (count >= items.size()) {
            return getItemWidth();
        }
        return (available - (count - 1) * getSpacing()) / count;
    }

    private double maxOffset() {
        double content = items.size() * step() - getSpacing();
        return Math.max(0, content - (getWidth() - snappedLeftInset() - snappedRightInset()) + getSmallItemWidth());
    }

    private void setOffsetClamped(double value) {
        offset.set(Math.clamp(value, 0, maxOffset()));
    }

    /** Scrolls to the next item. */
    public void next() {
        snapTo(Math.round(offset.get() / step()) + 1);
    }

    /** Scrolls to the previous item. */
    public void previous() {
        snapTo(Math.round(offset.get() / step()) - 1);
    }

    private void snapTo(long index) {
        double target = Math.clamp(index * step(), 0, maxOffset());
        snap.stop();
        if (!Motion.shouldAnimate(this)) {
            offset.set(target);
            return;
        }
        snap.getKeyFrames().setAll(new KeyFrame(Motion.MEDIUM_2, new KeyValue(offset, target, Motion.EMPHASIZED_DECELERATE)));
        snap.playFromStart();
    }

    @Override
    protected void layoutChildren() {
        double left = snappedLeftInset();
        double top = snappedTopInset();
        double w = getWidth() - left - snappedRightInset();
        double h = getHeight() - top - snappedBottomInset();
        clip.setWidth(getWidth());
        clip.setHeight(getHeight());
        double x = left - offset.get();
        double right = left + w;
        double small = getSmallItemWidth();
        double itemSize = effectiveItemWidth();
        for (Node child : getChildren()) {
            StackPane wrapper = (StackPane) child;
            double width = itemSize;
            double itemLeft = x;
            // Multi-browse: items crossing the trailing edge shrink down to the small width,
            // and items that scrolled past the leading edge shrink on that side.
            double visibleRight = Math.min(x + width, right);
            double visible = visibleRight - Math.max(x, left);
            if (x + width > right) {
                width = Math.max(small, right - x);
            }
            if (x < left) {
                double cut = Math.min(left - x, width - small);
                itemLeft = x + Math.max(0, cut);
                width -= Math.max(0, cut);
            }
            wrapper.resizeRelocate(itemLeft, top, Math.max(small, width), h);
            Rectangle itemClip = (Rectangle) wrapper.getClip();
            itemClip.setWidth(Math.max(small, width));
            itemClip.setHeight(h);
            // The item content keeps its full width and stays centered, like Material's masking.
            Node content = wrapper.getChildren().getFirst();
            if (content instanceof Region region) {
                region.setMinWidth(itemSize);
            }
            wrapper.setOpacity(visible > 0 || x + width > left ? 1 : 0);
            x += itemSize + getSpacing();
        }
    }

    @Override
    protected double computePrefHeight(double width) {
        return 200 + snappedTopInset() + snappedBottomInset();
    }

    @Override
    protected double computePrefWidth(double height) {
        return 3 * step() + snappedLeftInset() + snappedRightInset();
    }

    public ObservableList<Node> getItems() {
        return items;
    }

    public final DoubleProperty itemWidthProperty() {
        return itemWidth;
    }

    public final double getItemWidth() {
        return itemWidth.get();
    }

    public final void setItemWidth(double value) {
        itemWidth.set(value);
    }

    public final DoubleProperty smallItemWidthProperty() {
        return smallItemWidth;
    }

    public final double getSmallItemWidth() {
        return smallItemWidth.get();
    }

    public final void setSmallItemWidth(double value) {
        smallItemWidth.set(value);
    }

    public final DoubleProperty spacingProperty() {
        return spacing;
    }

    public final double getSpacing() {
        return spacing.get();
    }

    public final void setSpacing(double value) {
        spacing.set(value);
    }
}
