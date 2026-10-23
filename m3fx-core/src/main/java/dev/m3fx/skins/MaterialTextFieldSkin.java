package dev.m3fx.skins;

import dev.m3fx.Motion;
import dev.m3fx.controls.Icon;
import dev.m3fx.controls.IconButton;
import dev.m3fx.controls.MaterialTextField;
import dev.m3fx.controls.StateLayer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.layout.Region;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.HitInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Skin of {@link MaterialTextField}, built on {@link TextFieldSkin}: the standard text area
 * (text, caret, selection, prompt) is laid out inside the Material geometry, surrounded by the
 * container (filled) or notched outline (outlined), floating label, icons, prefix / suffix,
 * supporting text and counter. Mouse hit-testing is offset to match.
 */
public class MaterialTextFieldSkin extends TextFieldSkin {

    private static final PseudoClass FLOATING = PseudoClass.getPseudoClass("floating");
    private static final PseudoClass LEADING = PseudoClass.getPseudoClass("leading");
    private static final PseudoClass TRAILING = PseudoClass.getPseudoClass("trailing");
    private static final PseudoClass NO_LABEL = PseudoClass.getPseudoClass("no-label");
    private static final double LABEL_SCALE = 0.75;
    private static final double OUTLINE_RADIUS = 4;
    private static final double PADDING = 16;
    private static final double ICON_PADDING = 12;

    private final Region container = new Region();
    private final Region activeIndicator = new Region();
    private final Path outline = new Path();
    private final Label floatingLabel = new Label();
    private final Label supporting = new Label();
    private final Label counter = new Label();
    private final Label prefix = new Label();
    private final Label suffix = new Label();
    private final Icon leadingIcon = new Icon(null, 24);
    private final IconButton visibilityToggle = new IconButton();
    private final StateLayer stateLayer;
    private final DoubleProperty floatProgress = new SimpleDoubleProperty(this, "floatProgress");
    private final Timeline floatAnimation = new Timeline();
    private Node trailing;
    private final javafx.event.EventHandler<javafx.event.ActionEvent> stopAction = javafx.event.Event::consume;

    // Where the text area sits, for hit-testing.
    private double textX;
    private double textY;

    public MaterialTextFieldSkin(MaterialTextField field) {
        super(field);
        container.getStyleClass().add("md-field-container");
        activeIndicator.getStyleClass().add("md-field-indicator");
        outline.getStyleClass().add("md-field-outline");
        floatingLabel.getStyleClass().add("md-field-label");
        supporting.getStyleClass().add("md-field-supporting");
        counter.getStyleClass().add("md-field-counter");
        prefix.getStyleClass().add("md-field-prefix");
        suffix.getStyleClass().add("md-field-suffix");
        leadingIcon.getStyleClass().add("md-field-leading-icon");
        leadingIcon.setFollowTextFill(false);
        for (Node node : List.of(container, activeIndicator, outline, floatingLabel, supporting, counter, prefix, suffix, leadingIcon)) {
            node.setManaged(false);
            node.setMouseTransparent(true);
        }
        floatingLabel.setMinWidth(Region.USE_PREF_SIZE);
        supporting.setWrapText(true);

        stateLayer = new StateLayer(container);
        stateLayer.setRippleEnabled(false);
        stateLayer.setInteractionSource(null);
        stateLayer.setManaged(false);
        registerChangeListener(field.hoverProperty(), o -> stateLayer.setForceHover(field.isHover()));

        visibilityToggle.setFocusTraversable(false);
        visibilityToggle.setManaged(false);
        visibilityToggle.getStyleClass().add("md-field-trailing");
        visibilityToggle.setIconName(field.isPasswordVisible() ? "visibility_off" : "visibility");
        visibilityToggle.setAccessibleText(field.isPasswordVisible() ? "Hide password" : "Show password");
        visibilityToggle.setOnAction(e -> field.setPasswordVisible(!field.isPasswordVisible()));

        floatingLabel.textProperty().bind(field.labelProperty());
        prefix.textProperty().bind(field.prefixProperty());
        suffix.textProperty().bind(field.suffixProperty());
        leadingIcon.nameProperty().bind(field.leadingIconProperty());

        for (ObservableValue<?> p : List.of(field.variantProperty(), field.leadingIconProperty(), field.trailingProperty(),
                field.passwordProperty())) {
            registerChangeListener(p, o -> rebuild());
        }
        registerChangeListener(field.supportingTextProperty(), o -> updateSupporting());
        registerChangeListener(field.errorMessageProperty(), o -> updateSupporting());
        registerChangeListener(field.maxLengthProperty(), o -> updateCounter());
        registerChangeListener(field.textProperty(), o -> {
            updateFloat(true);
            updateCounter();
        });
        registerChangeListener(field.focusedProperty(), o -> updateFloat(true));
        registerChangeListener(field.labelProperty(), o -> {
            updateFloat(false);
            field.requestLayout();
        });
        registerChangeListener(field.prefixProperty(), o -> field.requestLayout());
        registerChangeListener(field.suffixProperty(), o -> field.requestLayout());
        floatProgress.addListener(o -> field.requestLayout());
        rebuild();
        updateSupporting();
        updateCounter();
        updateFloat(false);
    }

    private MaterialTextField field() {
        return (MaterialTextField) getSkinnable();
    }

    @Override
    protected String maskText(String txt) {
        return getSkinnable() instanceof MaterialTextField field ? field.maskedText(txt) : txt;
    }

    private void rebuild() {
        MaterialTextField field = field();
        getChildren().removeAll(container, stateLayer, activeIndicator, outline, floatingLabel, supporting, counter,
                prefix, suffix, leadingIcon, visibilityToggle);
        if (trailing != null) {
            getChildren().remove(trailing);
            trailing.getStyleClass().remove("md-field-trailing");
            trailing.removeEventHandler(javafx.event.ActionEvent.ACTION, stopAction);
        }
        boolean filled = field.getVariant() == MaterialTextField.Variant.FILLED;
        // Background parts go behind the text area, which TextFieldSkin added first.
        if (filled) {
            getChildren().addAll(0, List.of(container, stateLayer, activeIndicator));
        } else {
            getChildren().addFirst(outline);
        }
        getChildren().addAll(prefix, suffix, floatingLabel, supporting, counter);
        if (field.getLeadingIcon() != null) {
            getChildren().add(leadingIcon);
        }
        trailing = field.getTrailing();
        if (trailing == null && field.isPassword()) {
            trailing = visibilityToggle;
        }
        if (trailing != null) {
            trailing.setManaged(false);
            if (!trailing.getStyleClass().contains("md-field-trailing")) {
                trailing.getStyleClass().add("md-field-trailing");
            }
            // A trailing button's action would bubble to the text field and fire its onAction.
            trailing.addEventHandler(javafx.event.ActionEvent.ACTION, stopAction);
            getChildren().add(trailing);
        }
        field.pseudoClassStateChanged(LEADING, field.getLeadingIcon() != null);
        field.pseudoClassStateChanged(TRAILING, trailing != null);
        field.requestLayout();
    }

    private void updateSupporting() {
        MaterialTextField field = field();
        String message = field.getErrorMessage();
        String value = message != null ? message : field.getSupportingText();
        supporting.setText(value);
        supporting.setVisible(value != null && !value.isEmpty());
        field.requestLayout();
    }

    private void updateCounter() {
        MaterialTextField field = field();
        int max = field.getMaxLength();
        counter.setVisible(max >= 0);
        counter.setText(field.getLength() + " / " + max);
    }

    private boolean hasLabel() {
        String label = field().getLabel();
        return label != null && !label.isEmpty();
    }

    private boolean shouldFloat() {
        MaterialTextField field = field();
        return field.isFocused() || field.getLength() > 0 || !hasLabel();
    }

    private void updateFloat(boolean animate) {
        boolean floating = shouldFloat();
        field().pseudoClassStateChanged(FLOATING, floating);
        // Without a label the prompt text is the only hint: always show it.
        field().pseudoClassStateChanged(NO_LABEL, !hasLabel());
        prefix.setVisible(floating);
        suffix.setVisible(floating);
        double target = floating ? 1 : 0;
        floatAnimation.stop();
        if (animate && Motion.shouldAnimate(getSkinnable())) {
            floatAnimation.getKeyFrames().setAll(new KeyFrame(Motion.SHORT_3, new KeyValue(floatProgress, target, Motion.STANDARD)));
            floatAnimation.playFromStart();
        } else {
            floatProgress.set(target);
        }
    }

    private double containerHeight() {
        double h = container.getMinHeight();
        return h > 0 ? h : 56;
    }

    private boolean filled() {
        return field().getVariant() == MaterialTextField.Variant.FILLED;
    }

    private double leadingWidth() {
        return field().getLeadingIcon() != null ? ICON_PADDING + leadingIcon.getLayoutBounds().getWidth() + 16 : PADDING;
    }

    private double trailingWidth() {
        if (trailing == null) {
            return PADDING;
        }
        return trailing.prefWidth(-1) + (trailing instanceof IconButton ? 4 : ICON_PADDING) + 4;
    }

    /** Top of the text area: filled fields with a label push the text below the floated label. */
    private double textTop() {
        return filled() && hasLabel() ? 16 : 0;
    }

    /** Height of one line of text, from TextFieldSkin with no insets. */
    private double lineHeight() {
        return super.computePrefHeight(-1, 0, 0, 0, 0);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        double width = getSkinnable().getWidth();
        double ch = containerHeight();
        boolean filled = filled();
        double p = floatProgress.get();

        double left = leadingWidth();
        double right = trailingWidth();
        if (field().getLeadingIcon() != null) {
            Bounds b = leadingIcon.getLayoutBounds();
            leadingIcon.relocate(ICON_PADDING, (ch - b.getHeight()) / 2);
        }
        if (trailing != null) {
            double tw = trailing.prefWidth(-1);
            double th = trailing.prefHeight(tw);
            double tx = width - tw - (trailing instanceof IconButton ? 4 : ICON_PADDING);
            trailing.resizeRelocate(tx, (ch - th) / 2, tw, th);
        }

        double top = textTop();
        double prefixWidth = prefix.isVisible() && prefix.getText() != null ? prefix.prefWidth(-1) + 2 : 0;
        double suffixWidth = suffix.isVisible() && suffix.getText() != null ? suffix.prefWidth(-1) + 2 : 0;
        textX = left + prefixWidth;
        textY = top;
        double textWidth = Math.max(0, width - left - right - prefixWidth - suffixWidth);
        // The standard text area: text, caret, selection and prompt, vertically centered in it.
        super.layoutChildren(textX, textY, textWidth, ch - top);
        double line = lineHeight();
        double lineY = top + (ch - top - line) / 2;
        prefix.resizeRelocate(left, lineY, prefixWidth, line);
        suffix.resizeRelocate(width - right - suffixWidth, lineY, suffixWidth, line);

        // Floating label: from the resting position (in the text line) to the floated position.
        double labelWidth = floatingLabel.prefWidth(-1);
        double labelHeight = floatingLabel.prefHeight(labelWidth);
        double restX = left;
        double restY = (ch - labelHeight) / 2;
        double floatX = filled ? left : PADDING;
        double floatY = filled ? 8 - labelHeight * (1 - LABEL_SCALE) / 2 : -labelHeight / 2;
        floatingLabel.resize(labelWidth, labelHeight);
        double scale = 1 - (1 - LABEL_SCALE) * p;
        floatingLabel.setScaleX(scale);
        floatingLabel.setScaleY(scale);
        // Scaling is around the center: compensate so the left edge moves as specified.
        floatingLabel.relocate(restX + (floatX - restX) * p - labelWidth * (1 - scale) / 2, restY + (floatY - restY) * p);
        floatingLabel.setVisible(hasLabel());

        if (filled) {
            container.resizeRelocate(0, 0, width, ch);
            stateLayer.resizeRelocate(0, 0, width, ch);
            double indicatorHeight = activeIndicator.prefHeight(-1);
            activeIndicator.resizeRelocate(0, ch - indicatorHeight, width, indicatorHeight);
        } else {
            double gap = hasLabel() ? (labelWidth * LABEL_SCALE + 8) * p : 0;
            outline.getElements().setAll(outlinePath(width, ch, PADDING - 4, gap));
        }

        double supportY = ch + 4;
        double counterWidth = counter.isVisible() ? counter.prefWidth(-1) : 0;
        counter.resizeRelocate(width - PADDING - counterWidth, supportY, counterWidth, counter.prefHeight(-1));
        double supportWidth = Math.max(0, width - 2 * PADDING - counterWidth - (counterWidth > 0 ? 16 : 0));
        supporting.resizeRelocate(PADDING, supportY, supportWidth, supporting.prefHeight(supportWidth));
    }

    /** Mouse positions are relative to the control; the text area is offset inside it. */
    @Override
    public HitInfo getIndex(double x, double y) {
        return super.getIndex(x - textX, y - textY);
    }

    /** Rounded rectangle outline with an optional gap in the top edge for the floating label. */
    private static List<PathElement> outlinePath(double w, double h, double gapStart, double gapWidth) {
        double r = OUTLINE_RADIUS;
        List<PathElement> e = new ArrayList<>();
        double gapEnd = gapStart + gapWidth;
        e.add(new MoveTo(gapWidth > 0.5 ? gapEnd : r, 0));
        e.add(new LineTo(w - r, 0));
        e.add(new ArcTo(r, r, 0, w, r, false, true));
        e.add(new LineTo(w, h - r));
        e.add(new ArcTo(r, r, 0, w - r, h, false, true));
        e.add(new LineTo(r, h));
        e.add(new ArcTo(r, r, 0, 0, h - r, false, true));
        e.add(new LineTo(0, r));
        e.add(new ArcTo(r, r, 0, r, 0, false, true));
        if (gapWidth > 0.5) {
            e.add(new LineTo(gapStart, 0));
        } else {
            e.add(new ClosePath());
        }
        return e;
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        double h = containerHeight();
        if (supporting.isVisible() || counter.isVisible()) {
            h += 4 + Math.max(supporting.isVisible() ? supporting.prefHeight(-1) : 0, counter.isVisible() ? counter.prefHeight(-1) : 0);
        }
        return h;
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
        return Math.max(280, super.computePrefWidth(height, 0, 0, 0, 0) + leadingWidth() + trailingWidth());
    }

    @Override
    protected double computeMinWidth(double height, double top, double right, double bottom, double left) {
        return leadingWidth() + trailingWidth() + 40;
    }

    /**
     * Baseline of a vertically centered text line, the same for both variants, so fields in a
     * row aligned on BASELINE line up box-to-box, level with standard JavaFX text fields.
     */
    @Override
    public double computeBaselineOffset(double top, double right, double bottom, double left) {
        return (containerHeight() - lineHeight()) / 2 + super.computeBaselineOffset(0, 0, 0, 0);
    }

    @Override
    public void dispose() {
        if (getSkinnable() == null) {
            return;
        }
        floatAnimation.stop();
        getChildren().removeAll(container, stateLayer, activeIndicator, outline, floatingLabel, supporting, counter,
                prefix, suffix, leadingIcon, visibilityToggle);
        if (trailing != null) {
            getChildren().remove(trailing);
            trailing.removeEventHandler(javafx.event.ActionEvent.ACTION, stopAction);
        }
        floatingLabel.textProperty().unbind();
        prefix.textProperty().unbind();
        suffix.textProperty().unbind();
        leadingIcon.nameProperty().unbind();
        super.dispose();
    }
}
