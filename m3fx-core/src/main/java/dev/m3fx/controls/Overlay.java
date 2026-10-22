package dev.m3fx.controls;

import dev.m3fx.Motion;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * A surface shown above the window content: dialogs, sheets, modal drawers, snackbars.
 * <p>
 * Overlays are added to a host: the nearest {@link Scaffold} ancestor of the owner node, or the
 * scene root when it is a {@link StackPane}. Modal overlays get a scrim, trap clicks, and are
 * dismissed by Escape or a click on the scrim when dismissable.
 */
public final class Overlay {

    public enum Position { CENTER, TOP, BOTTOM, START, END, FILL }

    private static final String STACK_KEY = "m3fx.overlay.stack";

    private final StackPane host;
    private final StackPane layer = new StackPane();
    private final Region scrim = new Region();
    private final Node content;
    private final Position position;
    private final boolean modal;
    private final boolean dismissable;
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing");
    private final Timeline animation = new Timeline();
    private Runnable onHidden;
    private Node previousFocus;
    private EventHandler<KeyEvent> escapeFilter;
    private Scene scene;

    private Overlay(StackPane host, Node content, Position position, boolean modal, boolean dismissable) {
        this.host = host;
        this.content = content;
        this.position = position;
        this.modal = modal;
        this.dismissable = dismissable;
        layer.getStyleClass().add("md-overlay");
        layer.setPickOnBounds(modal);
        scrim.getStyleClass().add("md-scrim");
        scrim.setVisible(modal);
        scrim.setMouseTransparent(!modal);
        scrim.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (this.dismissable) {
                hide();
            }
        });
        StackPane.setAlignment(content, switch (position) {
            case CENTER, FILL -> Pos.CENTER;
            case TOP -> Pos.TOP_CENTER;
            case BOTTOM -> Pos.BOTTOM_CENTER;
            case START -> Pos.CENTER_LEFT;
            case END -> Pos.CENTER_RIGHT;
        });
        if (content instanceof Region region) {
            if (position == Position.START || position == Position.END) {
                region.setMaxHeight(Double.MAX_VALUE);
                region.setMaxWidth(Region.USE_PREF_SIZE);
            } else if (position == Position.FILL) {
                region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            } else {
                region.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            }
        }
        layer.getChildren().addAll(scrim, content);
    }

    /**
     * Shows {@code content} as an overlay in the window of {@code owner}.
     *
     * @param modal       adds a scrim and blocks interaction with the content below
     * @param dismissable closes on Escape and on a scrim click
     */
    public static Overlay show(Node owner, Node content, Position position, boolean modal, boolean dismissable) {
        Overlay overlay = new Overlay(findHost(owner), content, position, modal, dismissable);
        overlay.show();
        return overlay;
    }

    /**
     * The host overlays are added to: the nearest {@link Scaffold} ancestor of {@code owner}, or
     * the scene root when it is a {@link StackPane}.
     */
    public static StackPane findHost(Node owner) {
        for (Node n = owner; n != null; n = n.getParent()) {
            if (n instanceof Scaffold scaffold) {
                return scaffold;
            }
        }
        Parent root = owner.getScene() == null ? null : owner.getScene().getRoot();
        if (root instanceof StackPane stack) {
            return stack;
        }
        throw new IllegalStateException("m3fx overlays (dialogs, sheets, snackbars) need the owner to be inside a "
                + "Scaffold, or the scene root to be a StackPane");
    }

    @SuppressWarnings("unchecked")
    private List<Overlay> stack() {
        return (List<Overlay>) host.getProperties().computeIfAbsent(STACK_KEY, k -> new ArrayList<Overlay>());
    }

    private void show() {
        stack().add(this);
        host.getChildren().add(layer);
        showing.set(true);
        scene = host.getScene();
        if (scene != null) {
            previousFocus = scene.getFocusOwner();
            escapeFilter = e -> {
                List<Overlay> s = stack();
                boolean topmost = !s.isEmpty() && s.getLast() == this;
                if (e.getCode() == KeyCode.ESCAPE && dismissable && topmost) {
                    hide();
                    e.consume();
                } else if (e.getCode() == KeyCode.TAB && modal && topmost) {
                    // Focus trap: Tab cycles through the overlay, never the window behind it.
                    moveFocus(e.isShiftDown() ? -1 : 1);
                    e.consume();
                }
            };
            scene.addEventFilter(KeyEvent.KEY_PRESSED, escapeFilter);
        }
        if (modal) {
            focusFirst(content);
        }
        animate(true);
    }

    /** Hides the overlay, animating it out. */
    public void hide() {
        if (!showing.get()) {
            return;
        }
        showing.set(false);
        stack().remove(this);
        if (scene != null && escapeFilter != null) {
            scene.removeEventFilter(KeyEvent.KEY_PRESSED, escapeFilter);
        }
        layer.setMouseTransparent(true);
        animate(false);
    }

    private void finishHide() {
        host.getChildren().remove(layer);
        // Content may be reused outside the overlay (e.g. a drawer going back into the layout):
        // undo the exit animation.
        content.setTranslateX(0);
        content.setTranslateY(0);
        content.setScaleX(1);
        content.setScaleY(1);
        content.setOpacity(1);
        if (previousFocus != null && previousFocus.getScene() != null && modal) {
            previousFocus.requestFocus();
        }
        if (onHidden != null) {
            onHidden.run();
        }
    }

    private void animate(boolean in) {
        animation.stop();
        if (!Motion.shouldAnimate(host)) {
            scrim.setOpacity(in ? 1 : 0);
            content.setOpacity(1);
            content.setTranslateX(0);
            content.setTranslateY(0);
            content.setScaleX(1);
            content.setScaleY(1);
            if (!in) {
                finishHide();
            }
            return;
        }
        Duration duration = in ? Motion.MEDIUM_2 : Motion.SHORT_4;
        Interpolator ease = in ? Motion.EMPHASIZED_DECELERATE : Motion.EMPHASIZED_ACCELERATE;
        content.applyCss();
        double w = content.prefWidth(-1);
        double h = content.prefHeight(w);
        List<KeyValue> start = new ArrayList<>();
        List<KeyValue> end = new ArrayList<>();
        start.add(new KeyValue(scrim.opacityProperty(), in ? 0 : 1));
        end.add(new KeyValue(scrim.opacityProperty(), in ? 1 : 0, ease));
        switch (position) {
            case CENTER, FILL, TOP -> {
                start.add(new KeyValue(content.opacityProperty(), in ? 0 : 1));
                end.add(new KeyValue(content.opacityProperty(), in ? 1 : 0, ease));
                start.add(new KeyValue(content.scaleXProperty(), in ? 0.92 : 1));
                end.add(new KeyValue(content.scaleXProperty(), in ? 1 : 0.96, ease));
                start.add(new KeyValue(content.scaleYProperty(), in ? 0.92 : 1));
                end.add(new KeyValue(content.scaleYProperty(), in ? 1 : 0.96, ease));
            }
            case BOTTOM -> {
                start.add(new KeyValue(content.translateYProperty(), in ? h : 0));
                end.add(new KeyValue(content.translateYProperty(), in ? 0 : h, ease));
            }
            case START -> {
                start.add(new KeyValue(content.translateXProperty(), in ? -w : 0));
                end.add(new KeyValue(content.translateXProperty(), in ? 0 : -w, ease));
            }
            case END -> {
                start.add(new KeyValue(content.translateXProperty(), in ? w : 0));
                end.add(new KeyValue(content.translateXProperty(), in ? 0 : w, ease));
            }
        }
        animation.getKeyFrames().setAll(
                new KeyFrame(Duration.ZERO, start.toArray(KeyValue[]::new)),
                new KeyFrame(duration, end.toArray(KeyValue[]::new)));
        animation.setOnFinished(in ? null : e -> finishHide());
        animation.playFromStart();
    }

    private void moveFocus(int direction) {
        List<Node> focusable = new ArrayList<>();
        collectFocusable(content, focusable);
        if (focusable.isEmpty()) {
            return;
        }
        Node current = scene == null ? null : scene.getFocusOwner();
        int index = focusable.indexOf(current);
        int next = index < 0 ? 0 : Math.floorMod(index + direction, focusable.size());
        focusable.get(next).requestFocus();
    }

    private static void collectFocusable(Node node, List<Node> result) {
        if (!node.isVisible() || node.isDisabled()) {
            return;
        }
        if (node.isFocusTraversable()) {
            result.add(node);
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectFocusable(child, result);
            }
        }
    }

    private static boolean focusFirst(Node node) {
        if (node.isFocusTraversable() && !node.isDisabled() && node.isVisible()) {
            node.requestFocus();
            return true;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (focusFirst(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Node getContent() {
        return content;
    }

    public ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    public boolean isShowing() {
        return showing.get();
    }

    /** Called once the overlay is hidden and removed (after its exit animation). */
    public void setOnHidden(Runnable onHidden) {
        this.onHidden = onHidden;
    }
}
