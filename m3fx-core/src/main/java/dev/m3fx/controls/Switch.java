package dev.m3fx.controls;

import dev.m3fx.Motion;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * Material 3 switch. The thumb grows when selected or pressed and can show icons.
 *
 * <pre>{@code
 * Switch wifi = new Switch("Wi-Fi");
 * wifi.selectedProperty().addListener(...);
 * }</pre>
 */
public class Switch extends HBox {

    private static final double TRACK_WIDTH = 52;
    private static final double TRACK_HEIGHT = 32;

    private final Pane track = new Pane();
    private final Region thumb = new Region();
    private final Icon thumbIcon = new Icon(null, 16);
    private final StateLayer stateLayer;
    private final Label label = new Label();

    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected");
    private final BooleanProperty showIcons = new SimpleBooleanProperty(this, "showIcons");
    private final BooleanProperty showOnlySelectedIcon = new SimpleBooleanProperty(this, "showOnlySelectedIcon");
    private final StringProperty text = label.textProperty();

    // 0 = off position, 1 = on position; animated.
    private final DoubleProperty position = new SimpleDoubleProperty(this, "position");
    private final Timeline animation = new Timeline();

    public Switch() {
        this(null);
    }

    public Switch(String text) {
        getStyleClass().add("switch");
        setAlignment(Pos.CENTER_LEFT);
        setFocusTraversable(true);
        label.getStyleClass().add("switch-label");
        track.getStyleClass().add("switch-track");
        thumb.getStyleClass().add("switch-thumb");
        thumbIcon.getStyleClass().add("switch-icon");
        thumbIcon.setManaged(false);
        thumb.setManaged(false);
        track.setMinSize(TRACK_WIDTH, TRACK_HEIGHT);
        track.setPrefSize(TRACK_WIDTH, TRACK_HEIGHT);
        track.setMaxSize(TRACK_WIDTH, TRACK_HEIGHT);

        stateLayer = new StateLayer(this, StateLayer.ClipShape.CIRCLE);
        stateLayer.getStyleClass().add("switch-state-layer");
        track.getChildren().addAll(stateLayer, thumb, thumbIcon);
        getChildren().add(track);
        this.text.set(text);
        this.text.addListener(o -> updateLabel());
        updateLabel();

        selected.addListener((obs, old, value) -> {
            pseudoClassStateChanged(PseudoClasses.SELECTED, value);
            animateTo(value ? 1 : 0);
            updateIcon();
        });
        showIcons.addListener(o -> updateIcon());
        showOnlySelectedIcon.addListener(o -> updateIcon());
        pressedProperty().addListener(o -> requestLayout());
        position.addListener(o -> requestLayout());

        addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.PRIMARY && !isDisabled()) {
                setSelected(!isSelected());
                requestFocus();
            }
        });
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER) {
                setSelected(!isSelected());
                e.consume();
            }
        });
        stateLayer.setInteractionSource(this);
        updateIcon();
    }

    private void updateLabel() {
        String t = text.get();
        boolean hasText = t != null && !t.isEmpty();
        if (hasText && !getChildren().contains(label)) {
            getChildren().addFirst(label);
            javafx.scene.layout.HBox.setHgrow(label, javafx.scene.layout.Priority.ALWAYS);
            label.setMaxWidth(Double.MAX_VALUE);
        } else if (!hasText) {
            getChildren().remove(label);
        }
    }

    private void animateTo(double target) {
        animation.stop();
        if (!Motion.shouldAnimate(this)) {
            position.set(target);
            return;
        }
        animation.getKeyFrames().setAll(new KeyFrame(Motion.of(Motion.MEDIUM_1),
                new KeyValue(position, target, Motion.EMPHASIZED)));
        animation.playFromStart();
    }

    private void updateIcon() {
        boolean visible = isShowIcons() && (isSelected() || !isShowOnlySelectedIcon());
        thumbIcon.setVisible(visible);
        thumbIcon.setName(isSelected() ? "check" : "close");
        pseudoClassStateChanged(WITH_ICON, visible);
        requestLayout();
    }

    private static final javafx.css.PseudoClass WITH_ICON = javafx.css.PseudoClass.getPseudoClass("with-icon");

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        double p = position.get();
        double base = thumbIcon.isVisible() ? 24 : 16;
        double size = isPressed() && !isDisabled() ? 28 : base + (24 - base) * p;
        double cx = TRACK_HEIGHT / 2 + (TRACK_WIDTH - TRACK_HEIGHT) * p;
        double cy = TRACK_HEIGHT / 2;
        thumb.resizeRelocate(cx - size / 2, cy - size / 2, size, size);
        javafx.geometry.Bounds iconBounds = thumbIcon.getLayoutBounds();
        thumbIcon.relocate(cx - iconBounds.getWidth() / 2, cy - iconBounds.getHeight() / 2);
        stateLayer.resizeRelocate(cx - 20, cy - 20, 40, 40);
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

    /** Shows a check (on) / close (off) icon in the thumb. */
    public final BooleanProperty showIconsProperty() {
        return showIcons;
    }

    public final boolean isShowIcons() {
        return showIcons.get();
    }

    public final void setShowIcons(boolean value) {
        showIcons.set(value);
    }

    /** With {@link #showIconsProperty()}, shows the icon only in the on state. */
    public final BooleanProperty showOnlySelectedIconProperty() {
        return showOnlySelectedIcon;
    }

    public final boolean isShowOnlySelectedIcon() {
        return showOnlySelectedIcon.get();
    }

    public final void setShowOnlySelectedIcon(boolean value) {
        showOnlySelectedIcon.set(value);
    }

    /** Optional label shown before the switch. */
    public final StringProperty textProperty() {
        return text;
    }

    public final String getText() {
        return text.get();
    }

    public final void setText(String value) {
        text.set(value);
    }
}
