package io.github.squoirelle.material3fx.controls;

import io.github.squoirelle.material3fx.Motion;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.PaintConverter;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Material interaction feedback: the translucent hover / focus / pressed overlay and the press
 * ripple. Add it as the bottom-most child of a component and size it to the component's bounds;
 * it reads the component's interaction state and animates itself.
 * <p>
 * The layer color is {@code -md-state-color} from CSS; when unset it follows the owner's text
 * fill (if the owner is a {@link Labeled}), which is what Material specifies: state layers use the
 * content color.
 */
public class StateLayer extends Region {

    /** How the layer is clipped. */
    public enum ClipShape {
        /** Same corner radii as the owner's background. */
        OWNER,
        /** A circle centered in the layer (checkboxes, radios, switch thumbs, icon buttons). */
        CIRCLE,
        /** No rounding. */
        RECTANGLE
    }

    private static final double HOVER_OPACITY = 0.08;
    private static final double FOCUS_OPACITY = 0.10;
    private static final double PRESS_OPACITY = 0.10;
    private static final double DRAG_OPACITY = 0.16;

    private final Region owner;
    private final Region overlay = new Region();
    private final Region clip = new Region();
    private final List<Circle> ripples = new ArrayList<>();
    private final Timeline overlayAnimation = new Timeline();

    private final ObjectProperty<ClipShape> clipShape = new SimpleObjectProperty<>(this, "clipShape", ClipShape.OWNER);
    private final BooleanProperty dragged = new SimpleBooleanProperty(this, "dragged");
    private final BooleanProperty rippleEnabled = new SimpleBooleanProperty(this, "rippleEnabled", true);
    private final BooleanProperty forceHover = new SimpleBooleanProperty(this, "forceHover");
    private Node interactionSource;

    private final EventHandler<MouseEvent> pressHandler = this::onPressed;
    private final EventHandler<MouseEvent> releaseHandler = e -> releaseRipples();
    private final EventHandler<KeyEvent> keyPressHandler = e -> {
        if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER) {
            startRipple(getWidth() / 2, getHeight() / 2);
        }
    };
    private final EventHandler<KeyEvent> keyReleaseHandler = e -> {
        if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER) {
            releaseRipples();
        }
    };

    public StateLayer(Region owner) {
        this(owner, ClipShape.OWNER);
    }

    public StateLayer(Region owner, ClipShape shape) {
        this.owner = owner;
        getStyleClass().add("md-state-layer");
        overlay.getStyleClass().add("md-state-overlay");
        overlay.setOpacity(0);
        overlay.setManaged(false);
        getChildren().add(overlay);
        setManaged(false);
        setMouseTransparent(true);
        setPickOnBounds(false);
        setClip(clip);
        setClipShape(shape);

        InvalidationListener stateListener = o -> updateOverlay();
        owner.hoverProperty().addListener(stateListener);
        owner.focusVisibleProperty().addListener(stateListener);
        owner.pressedProperty().addListener(stateListener);
        owner.disabledProperty().addListener(stateListener);
        dragged.addListener(stateListener);
        forceHover.addListener(stateListener);
        setInteractionSource(owner);

        InvalidationListener colorListener = o -> updateColor();
        stateColor.addListener(colorListener);
        if (owner instanceof Labeled labeled) {
            labeled.textFillProperty().addListener(colorListener);
        }
        owner.backgroundProperty().addListener(o -> requestLayout());
        this.clipShape.addListener(o -> requestLayout());
        updateColor();
    }

    /**
     * The node whose mouse/keyboard presses start ripples. Defaults to the owner; components whose
     * state layer sits on a sub-part (e.g. a checkbox box) can keep the owner as source.
     */
    public final void setInteractionSource(Node source) {
        if (interactionSource != null) {
            interactionSource.removeEventFilter(MouseEvent.MOUSE_PRESSED, pressHandler);
            interactionSource.removeEventFilter(MouseEvent.MOUSE_RELEASED, releaseHandler);
            interactionSource.removeEventFilter(MouseEvent.MOUSE_EXITED, releaseHandler);
            interactionSource.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressHandler);
            interactionSource.removeEventFilter(KeyEvent.KEY_RELEASED, keyReleaseHandler);
        }
        interactionSource = source;
        if (source != null) {
            source.addEventFilter(MouseEvent.MOUSE_PRESSED, pressHandler);
            source.addEventFilter(MouseEvent.MOUSE_RELEASED, releaseHandler);
            source.addEventFilter(MouseEvent.MOUSE_EXITED, releaseHandler);
            source.addEventFilter(KeyEvent.KEY_PRESSED, keyPressHandler);
            source.addEventFilter(KeyEvent.KEY_RELEASED, keyReleaseHandler);
        }
    }

    private void onPressed(MouseEvent event) {
        if (owner.isDisabled() || !event.isPrimaryButtonDown()) {
            return;
        }
        Point2D local = sceneToLocal(event.getSceneX(), event.getSceneY());
        if (getClipShape() == ClipShape.CIRCLE) {
            startRipple(getWidth() / 2, getHeight() / 2);
        } else {
            startRipple(local.getX(), local.getY());
        }
    }

    private void updateOverlay() {
        double target = 0;
        if (!owner.isDisabled()) {
            if (isDragged()) {
                target = DRAG_OPACITY;
            } else if (owner.isPressed()) {
                target = PRESS_OPACITY;
            } else if (owner.isFocusVisible()) {
                target = FOCUS_OPACITY;
            } else if (owner.isHover() || isForceHover()) {
                target = HOVER_OPACITY;
            }
        }
        overlayAnimation.stop();
        overlayAnimation.getKeyFrames().setAll(new KeyFrame(Motion.of(Motion.SHORT_3),
                new KeyValue(overlay.opacityProperty(), target, Motion.STANDARD)));
        overlayAnimation.playFromStart();
    }

    /** Starts a ripple from a point in this layer's coordinates. */
    public void startRipple(double x, double y) {
        if (!isRippleEnabled() || owner.isDisabled() || getWidth() <= 0) {
            return;
        }
        double maxRadius = 0;
        for (double cx : new double[] {0, getWidth()}) {
            for (double cy : new double[] {0, getHeight()}) {
                maxRadius = Math.max(maxRadius, Math.hypot(cx - x, cy - y));
            }
        }
        Circle ripple = new Circle(x, y, Math.max(1, maxRadius * 0.2));
        ripple.getStyleClass().add("md-ripple");
        ripple.setFill(currentColor());
        ripple.setOpacity(PRESS_OPACITY);
        ripple.setManaged(false);
        ripple.getProperties().put("released", false);
        getChildren().add(ripple);
        ripples.add(ripple);

        Duration duration = Motion.of(Duration.millis(Math.min(550, 250 + maxRadius * 1.2)));
        Timeline grow = new Timeline(new KeyFrame(duration,
                new KeyValue(ripple.radiusProperty(), maxRadius, Motion.STANDARD_DECELERATE)));
        ripple.getProperties().put("grow", grow);
        grow.setOnFinished(e -> {
            if (Boolean.TRUE.equals(ripple.getProperties().get("released"))) {
                fadeOut(ripple);
            }
        });
        grow.play();
    }

    private void releaseRipples() {
        for (Circle ripple : List.copyOf(ripples)) {
            if (Boolean.TRUE.equals(ripple.getProperties().get("released"))) {
                continue;
            }
            ripple.getProperties().put("released", true);
            Timeline grow = (Timeline) ripple.getProperties().get("grow");
            if (grow == null || grow.getStatus() != javafx.animation.Animation.Status.RUNNING
                    || grow.getCurrentTime().greaterThan(Duration.millis(150))) {
                // Let the ripple keep expanding while it fades out, so short clicks still show it.
                if (grow != null) {
                    grow.setOnFinished(null);
                }
                fadeOut(ripple);
            }
        }
    }

    private void fadeOut(Circle ripple) {
        FadeTransition fade = new FadeTransition(Motion.of(Motion.MEDIUM_1), ripple);
        fade.setToValue(0);
        fade.setInterpolator(Motion.STANDARD);
        fade.setOnFinished(e -> {
            getChildren().remove(ripple);
            ripples.remove(ripple);
        });
        new ParallelTransition(fade).play();
    }

    private Paint currentColor() {
        Paint color = getStateColor();
        if (color == null && owner instanceof Labeled labeled) {
            color = labeled.getTextFill();
        }
        return color == null ? Color.BLACK : color;
    }

    private void updateColor() {
        Paint color = currentColor();
        overlay.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, null)));
        for (Circle ripple : ripples) {
            ripple.setFill(color);
        }
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        overlay.resizeRelocate(0, 0, w, h);
        // The clip is a region with the owner's exact corner radii, so asymmetric shapes
        // (e.g. the end segments of a segmented button) are matched precisely.
        CornerRadii radii = switch (getClipShape()) {
            case CIRCLE -> new CornerRadii(Math.min(w, h) / 2);
            case RECTANGLE -> CornerRadii.EMPTY;
            case OWNER -> ownerRadii();
        };
        clip.setBackground(new Background(new BackgroundFill(Color.BLACK, radii, null)));
        clip.resize(w, h);
    }

    private CornerRadii ownerRadii() {
        Background bg = owner.getBackground();
        if (bg == null || bg.getFills().isEmpty()) {
            return CornerRadii.EMPTY;
        }
        return bg.getFills().getLast().getRadii();
    }

    public final ObjectProperty<ClipShape> clipShapeProperty() {
        return clipShape;
    }

    public final ClipShape getClipShape() {
        return clipShape.get();
    }

    public final void setClipShape(ClipShape value) {
        clipShape.set(value);
    }

    /** Set while the owner is being dragged, for the stronger drag overlay. */
    public final BooleanProperty draggedProperty() {
        return dragged;
    }

    public final boolean isDragged() {
        return dragged.get();
    }

    public final void setDragged(boolean value) {
        dragged.set(value);
    }

    public final BooleanProperty rippleEnabledProperty() {
        return rippleEnabled;
    }

    public final boolean isRippleEnabled() {
        return rippleEnabled.get();
    }

    public final void setRippleEnabled(boolean value) {
        rippleEnabled.set(value);
    }

    /** Shows the hover overlay regardless of the mouse, e.g. when the owner is hovered through another node. */
    public final BooleanProperty forceHoverProperty() {
        return forceHover;
    }

    public final boolean isForceHover() {
        return forceHover.get();
    }

    public final void setForceHover(boolean value) {
        forceHover.set(value);
    }

    // -md-state-color

    private final StyleableObjectProperty<Paint> stateColor = new StyleableObjectProperty<>() {
        @Override
        public Object getBean() {
            return StateLayer.this;
        }

        @Override
        public String getName() {
            return "stateColor";
        }

        @Override
        public CssMetaData<StateLayer, Paint> getCssMetaData() {
            return STATE_COLOR;
        }
    };

    public final ObjectProperty<Paint> stateColorProperty() {
        return stateColor;
    }

    public final Paint getStateColor() {
        return stateColor.get();
    }

    public final void setStateColor(Paint value) {
        stateColor.set(value);
    }

    private static final CssMetaData<StateLayer, Paint> STATE_COLOR =
            new CssMetaData<>("-md-state-color", PaintConverter.getInstance()) {
                @Override
                public boolean isSettable(StateLayer layer) {
                    return !layer.stateColor.isBound();
                }

                @Override
                @SuppressWarnings("unchecked")
                public StyleableProperty<Paint> getStyleableProperty(StateLayer layer) {
                    return (StyleableProperty<Paint>) layer.stateColorProperty();
                }
            };

    private static final List<CssMetaData<? extends Styleable, ?>> CSS_META_DATA;

    static {
        List<CssMetaData<? extends Styleable, ?>> list = new ArrayList<>(Region.getClassCssMetaData());
        list.add(STATE_COLOR);
        CSS_META_DATA = Collections.unmodifiableList(list);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return CSS_META_DATA;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return CSS_META_DATA;
    }
}
