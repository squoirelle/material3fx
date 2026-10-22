package dev.m3fx.controls;

import dev.m3fx.Motion;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Material 3 text field, filled or outlined, with a floating label.
 *
 * <pre>{@code
 * MaterialTextField callsign = new MaterialTextField("Callsign");
 * callsign.setSupportingText("Three to eight letters");
 * callsign.setMaxLength(8);
 * callsign.setValidator(text -> text.length() < 3 ? "Too short" : null);
 *
 * MaterialTextField password = MaterialTextField.password("Password");
 * }</pre>
 * The inner JavaFX text field is available through {@link #getEditor()}.
 */
public class MaterialTextField extends Region {

    public enum Variant { FILLED, OUTLINED }

    private static final double LABEL_SCALE = 0.75;
    private static final double OUTLINE_RADIUS = 4;
    private static final double PADDING = 16;
    private static final double ICON_PADDING = 12;

    private final TextField textField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Label floatingLabel = new Label();
    private final Label supportingLabel = new Label();
    private final Label counterLabel = new Label();
    private final Label prefixLabel = new Label();
    private final Label suffixLabel = new Label();
    private final Region container = new Region();
    private final Region activeIndicator = new Region();
    private final Path outline = new Path();
    private final Icon leadingIconNode = new Icon(null, 24);
    private final StateLayer stateLayer;

    private final StringProperty text = new SimpleStringProperty(this, "text", "");
    private final StringProperty supportingText = new SimpleStringProperty(this, "supportingText");
    private final StringProperty leadingIcon = new SimpleStringProperty(this, "leadingIcon");
    private final ObjectProperty<Node> trailing = new SimpleObjectProperty<>(this, "trailing");
    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant", Variant.OUTLINED);
    private final IntegerProperty maxLength = new SimpleIntegerProperty(this, "maxLength", -1);
    private final BooleanProperty error = new SimpleBooleanProperty(this, "error");
    private final ReadOnlyStringWrapper errorMessage = new ReadOnlyStringWrapper(this, "errorMessage");
    private final ObjectProperty<Function<String, String>> validator = new SimpleObjectProperty<>(this, "validator");
    private final BooleanProperty password = new SimpleBooleanProperty(this, "password");
    private final BooleanProperty passwordVisible = new SimpleBooleanProperty(this, "passwordVisible");

    private final DoubleProperty floatProgress = new SimpleDoubleProperty(this, "floatProgress");
    private final Timeline floatAnimation = new Timeline();
    private boolean touched;

    public MaterialTextField() {
        this(null);
    }

    public MaterialTextField(String label) {
        this(label, Variant.OUTLINED);
    }

    public MaterialTextField(String label, Variant variant) {
        getStyleClass().addAll("material-text-field", variant.name().toLowerCase());
        container.getStyleClass().add("md-field-container");
        activeIndicator.getStyleClass().add("md-field-indicator");
        outline.getStyleClass().add("md-field-outline");
        floatingLabel.getStyleClass().add("md-field-label");
        supportingLabel.getStyleClass().add("md-field-supporting");
        counterLabel.getStyleClass().add("md-field-counter");
        prefixLabel.getStyleClass().add("md-field-prefix");
        suffixLabel.getStyleClass().add("md-field-suffix");
        leadingIconNode.getStyleClass().add("md-field-leading-icon");
        for (TextField editor : List.of(textField, passwordField)) {
            editor.getStyleClass().add("md-editor");
            editor.textProperty().bindBidirectional(text);
            editor.focusedProperty().addListener(o -> onFocusChanged());
        }
        floatingLabel.setText(label);
        floatingLabel.setMouseTransparent(true);
        floatingLabel.setMinWidth(USE_PREF_SIZE);
        outline.setManaged(false);
        outline.setMouseTransparent(true);
        container.setMouseTransparent(true);
        activeIndicator.setMouseTransparent(true);
        prefixLabel.setMouseTransparent(true);
        suffixLabel.setMouseTransparent(true);
        for (Label l : List.of(prefixLabel, suffixLabel)) {
            l.setText(null);
            l.textProperty().addListener(o -> requestLayout());
        }

        stateLayer = new StateLayer(container);
        stateLayer.setRippleEnabled(false);
        stateLayer.setInteractionSource(null);
        hoverProperty().addListener(o -> stateLayer.setForceHover(isHover()));

        this.variant.set(variant);
        this.variant.addListener((obs, old, value) -> {
            getStyleClass().remove(old.name().toLowerCase());
            getStyleClass().add(value.name().toLowerCase());
            rebuildChildren();
        });
        InvalidationListener rebuild = o -> rebuildChildren();
        leadingIcon.addListener(rebuild);
        trailing.addListener(rebuild);
        password.addListener(rebuild);
        passwordVisible.addListener(rebuild);
        leadingIconNode.nameProperty().bind(leadingIcon);

        text.addListener(o -> {
            updateFloat(true);
            updateCounter();
            if (touched) {
                validate();
            }
        });
        supportingText.addListener(o -> updateSupporting());
        errorMessage.addListener(o -> updateSupporting());
        maxLength.addListener(o -> {
            updateCounter();
            installFormatter();
        });
        error.addListener((obs, old, value) -> pseudoClassStateChanged(PseudoClasses.ERROR, value));
        floatProgress.addListener(o -> requestLayout());
        floatingLabel.textProperty().addListener(o -> requestLayout());

        addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (!getEditor().isFocused()) {
                getEditor().requestFocus();
            }
        });
        installFormatter();
        rebuildChildren();
        updateSupporting();
        updateCounter();
        updateFloat(false);
    }

    /** A password field with a visibility toggle. */
    public static MaterialTextField password(String label) {
        MaterialTextField field = new MaterialTextField(label);
        field.setPassword(true);
        return field;
    }

    /** The visible inner text field ({@link PasswordField} for hidden passwords). */
    public TextField getEditor() {
        return isPassword() && !isPasswordVisible() ? passwordField : textField;
    }

    /** Runs the validator now, and marks the field as touched so it re-validates on each change. */
    public boolean validate() {
        touched = true;
        Function<String, String> v = getValidator();
        String message = v == null ? null : v.apply(getText() == null ? "" : getText());
        errorMessage.set(message);
        if (v != null) {
            error.set(message != null);
        }
        return message == null;
    }

    private void onFocusChanged() {
        boolean focused = textField.isFocused() || passwordField.isFocused();
        pseudoClassStateChanged(PseudoClasses.FOCUSED, focused);
        if (!focused && getValidator() != null) {
            validate();
        }
        updateFloat(true);
    }

    private void installFormatter() {
        int max = getMaxLength();
        for (TextField editor : List.of(textField, passwordField)) {
            editor.setTextFormatter(max < 0 ? null : new TextFormatter<String>(change ->
                    change.getControlNewText().length() <= max ? change : null));
        }
    }

    private void rebuildChildren() {
        List<Node> children = new ArrayList<>();
        if (getVariant() == Variant.FILLED) {
            children.add(container);
            children.add(activeIndicator);
        } else {
            children.add(outline);
        }
        container.setVisible(getVariant() == Variant.FILLED);
        if (getLeadingIcon() != null) {
            children.add(leadingIconNode);
        }
        children.add(prefixLabel);
        children.add(getEditor());
        children.add(suffixLabel);
        children.add(floatingLabel);
        Node trailingNode = getTrailing();
        if (isPassword()) {
            IconButton toggle = new IconButton(isPasswordVisible() ? "visibility_off" : "visibility");
            toggle.setFocusTraversable(false);
            toggle.setOnAction(e -> {
                int caret = getEditor().getCaretPosition();
                setPasswordVisible(!isPasswordVisible());
                getEditor().requestFocus();
                getEditor().positionCaret(caret);
            });
            trailingNode = toggle;
        }
        if (trailingNode != null) {
            trailingNode.getStyleClass().remove("md-field-trailing");
            trailingNode.getStyleClass().add("md-field-trailing");
            children.add(trailingNode);
        }
        children.add(supportingLabel);
        children.add(counterLabel);
        getChildren().setAll(children);
        if (getVariant() == Variant.FILLED) {
            if (!getChildren().contains(stateLayer)) {
                getChildren().add(1, stateLayer);
            }
        }
        pseudoClassStateChanged(LEADING, getLeadingIcon() != null);
        pseudoClassStateChanged(TRAILING, trailingNode != null);
        requestLayout();
    }

    private Node trailingNode() {
        for (Node child : getChildren()) {
            if (child.getStyleClass().contains("md-field-trailing")) {
                return child;
            }
        }
        return null;
    }

    private static final javafx.css.PseudoClass LEADING = javafx.css.PseudoClass.getPseudoClass("leading");
    private static final javafx.css.PseudoClass TRAILING = javafx.css.PseudoClass.getPseudoClass("trailing");

    private void updateSupporting() {
        String message = errorMessage.get();
        String value = message != null ? message : getSupportingText();
        supportingLabel.setText(value);
        supportingLabel.setVisible(value != null && !value.isEmpty());
        requestLayout();
    }

    private void updateCounter() {
        int max = getMaxLength();
        counterLabel.setVisible(max >= 0);
        int length = getText() == null ? 0 : getText().length();
        counterLabel.setText(length + " / " + max);
    }

    private boolean shouldFloat() {
        boolean focused = textField.isFocused() || passwordField.isFocused();
        boolean empty = getText() == null || getText().isEmpty();
        return focused || !empty || floatingLabel.getText() == null;
    }

    private void updateFloat(boolean animate) {
        boolean floating = shouldFloat();
        pseudoClassStateChanged(PseudoClasses.FLOATING, floating);
        double target = floating ? 1 : 0;
        floatAnimation.stop();
        if (animate && Motion.shouldAnimate(this)) {
            floatAnimation.getKeyFrames().setAll(new KeyFrame(Motion.SHORT_3,
                    new KeyValue(floatProgress, target, Motion.STANDARD)));
            floatAnimation.playFromStart();
        } else {
            floatProgress.set(target);
        }
        // Prompt text only once the label is out of the way.
        prefixLabel.setVisible(floating);
        suffixLabel.setVisible(floating);
    }

    private double containerHeight() {
        double h = container.getMinHeight();
        return h > 0 ? h : 56;
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = containerHeight();
        boolean filled = getVariant() == Variant.FILLED;
        boolean hasLabel = floatingLabel.getText() != null && !floatingLabel.getText().isEmpty();
        double p = floatProgress.get();

        double textStart = PADDING;
        if (getLeadingIcon() != null) {
            double iw = leadingIconNode.getLayoutBounds().getWidth();
            double ih = leadingIconNode.getLayoutBounds().getHeight();
            leadingIconNode.relocate(ICON_PADDING, (h - ih) / 2);
            textStart = ICON_PADDING + iw + 16;
        }
        double textEnd = w - PADDING;
        Node trailingNode = trailingNode();
        if (trailingNode != null) {
            double tw = trailingNode.prefWidth(-1);
            double th = trailingNode.prefHeight(tw);
            double tx = w - tw - (trailingNode instanceof IconButton ? 4 : ICON_PADDING);
            trailingNode.resizeRelocate(tx, (h - th) / 2, tw, th);
            textEnd = tx - 4;
        }

        javafx.scene.control.TextField editor = getEditor();
        double editorHeight = editorHeight();
        double editorY = editorY(h, editorHeight);
        double prefixWidth = prefixLabel.isVisible() && prefixLabel.getText() != null ? prefixLabel.prefWidth(-1) + 2 : 0;
        double suffixWidth = suffixLabel.isVisible() && suffixLabel.getText() != null ? suffixLabel.prefWidth(-1) + 2 : 0;
        prefixLabel.resizeRelocate(textStart, editorY, prefixWidth, editorHeight);
        editor.resizeRelocate(textStart + prefixWidth, editorY, Math.max(0, textEnd - textStart - prefixWidth - suffixWidth), editorHeight);
        suffixLabel.resizeRelocate(textEnd - suffixWidth, editorY, suffixWidth, editorHeight);

        // Floating label: from the resting position (in the text line) to the floated position.
        double labelWidth = floatingLabel.prefWidth(-1);
        double labelHeight = floatingLabel.prefHeight(labelWidth);
        double restX = textStart;
        double restY = (h - labelHeight) / 2;
        double floatX = filled ? textStart : PADDING;
        double floatY = filled ? 8 - labelHeight * (1 - LABEL_SCALE) / 2 : -labelHeight / 2;
        floatingLabel.resize(labelWidth, labelHeight);
        double scale = 1 - (1 - LABEL_SCALE) * p;
        floatingLabel.setScaleX(scale);
        floatingLabel.setScaleY(scale);
        // Scale is around the center: compensate so the label's left edge stays put.
        double x = restX + (floatX - restX) * p - labelWidth * (1 - scale) / 2;
        double y = restY + (floatY - restY) * p;
        floatingLabel.relocate(x, y);

        if (filled) {
            container.resizeRelocate(0, 0, w, h);
            stateLayer.resizeRelocate(0, 0, w, h);
            double indicatorHeight = activeIndicator.prefHeight(-1);
            activeIndicator.resizeRelocate(0, h - indicatorHeight, w, indicatorHeight);
        } else {
            double gap = hasLabel ? (labelWidth * LABEL_SCALE + 8) * p : 0;
            outline.getElements().setAll(outlinePath(w, h, PADDING - 4, gap));
        }

        double supportY = h + 4;
        double counterWidth = counterLabel.isVisible() ? counterLabel.prefWidth(-1) : 0;
        counterLabel.resizeRelocate(w - PADDING - counterWidth, supportY, counterWidth, counterLabel.prefHeight(-1));
        double supportWidth = Math.max(0, w - 2 * PADDING - counterWidth - (counterWidth > 0 ? 16 : 0));
        supportingLabel.resizeRelocate(PADDING, supportY, supportWidth, supportingLabel.prefHeight(supportWidth));
    }

    private double editorHeight() {
        return Math.max(24, getEditor().prefHeight(-1));
    }

    /** Editor line: centered, or pushed down below the floating label in filled fields. */
    private double editorY(double containerHeight, double editorHeight) {
        double y = (containerHeight - editorHeight) / 2;
        boolean hasLabel = floatingLabel.getText() != null && !floatingLabel.getText().isEmpty();
        if (getVariant() == Variant.FILLED && hasLabel) {
            y += 8;
        }
        return y;
    }

    /**
     * Baseline of a vertically centered text line, the same for both variants, so fields in a
     * row aligned on BASELINE line up box-to-box whatever their variant or supporting text, and
     * level with standard JavaFX text fields.
     */
    @Override
    public double getBaselineOffset() {
        return (containerHeight() - editorHeight()) / 2 + getEditor().getBaselineOffset();
    }

    /** Rounded rectangle outline with an optional gap in the top edge for the floating label. */
    private static List<PathElement> outlinePath(double w, double h, double gapStart, double gapWidth) {
        double r = OUTLINE_RADIUS;
        List<PathElement> e = new ArrayList<>();
        double gapEnd = gapStart + gapWidth;
        if (gapWidth > 0.5) {
            e.add(new MoveTo(gapEnd, 0));
        } else {
            e.add(new MoveTo(r, 0));
        }
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
    protected double computePrefHeight(double width) {
        double h = containerHeight();
        if (supportingLabel.isVisible() || counterLabel.isVisible()) {
            h += 4 + Math.max(supportingLabel.prefHeight(-1), counterLabel.prefHeight(-1));
        }
        return h;
    }

    @Override
    protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }

    @Override
    protected double computeMaxHeight(double width) {
        return computePrefHeight(width);
    }

    @Override
    protected double computePrefWidth(double height) {
        return 280;
    }

    @Override
    protected double computeMinWidth(double height) {
        return 120;
    }

    @Override
    public void requestFocus() {
        getEditor().requestFocus();
    }

    // Properties

    public final StringProperty textProperty() {
        return text;
    }

    public final String getText() {
        return text.get();
    }

    public final void setText(String value) {
        text.set(value);
    }

    public final StringProperty labelProperty() {
        return floatingLabel.textProperty();
    }

    public final String getLabel() {
        return floatingLabel.getText();
    }

    public final void setLabel(String value) {
        floatingLabel.setText(value);
    }

    /** Placeholder shown when the field is focused and empty. */
    public final StringProperty placeholderProperty() {
        return textField.promptTextProperty();
    }

    public final void setPlaceholder(String value) {
        textField.setPromptText(value);
        passwordField.setPromptText(value);
    }

    public final StringProperty supportingTextProperty() {
        return supportingText;
    }

    public final String getSupportingText() {
        return supportingText.get();
    }

    public final void setSupportingText(String value) {
        supportingText.set(value);
    }

    public final StringProperty leadingIconProperty() {
        return leadingIcon;
    }

    public final String getLeadingIcon() {
        return leadingIcon.get();
    }

    public final void setLeadingIcon(String value) {
        leadingIcon.set(value);
    }

    /** Trailing node: an {@link Icon} or {@link IconButton} (e.g. clear, dropdown arrow). */
    public final ObjectProperty<Node> trailingProperty() {
        return trailing;
    }

    public final Node getTrailing() {
        return trailing.get();
    }

    public final void setTrailing(Node value) {
        trailing.set(value);
    }

    /** Convenience: a trailing icon button clearing the text. */
    public final void setClearable(boolean clearable) {
        if (clearable) {
            IconButton clear = new IconButton("cancel");
            clear.setFocusTraversable(false);
            clear.setOnAction(e -> {
                setText("");
                getEditor().requestFocus();
            });
            clear.visibleProperty().bind(text.isNotEmpty());
            setTrailing(clear);
        } else {
            setTrailing(null);
        }
    }

    public final StringProperty prefixProperty() {
        return prefixLabel.textProperty();
    }

    /** Text shown before the input once the label floats, e.g. a currency. */
    public final void setPrefix(String value) {
        prefixLabel.setText(value);
    }

    public final StringProperty suffixProperty() {
        return suffixLabel.textProperty();
    }

    /** Text shown after the input once the label floats, e.g. a unit. */
    public final void setSuffix(String value) {
        suffixLabel.setText(value);
    }

    public final ObjectProperty<Variant> variantProperty() {
        return variant;
    }

    public final Variant getVariant() {
        return variant.get();
    }

    public final void setVariant(Variant value) {
        variant.set(value);
    }

    /** Maximum number of characters; shows a counter. -1 (default) for none. */
    public final IntegerProperty maxLengthProperty() {
        return maxLength;
    }

    public final int getMaxLength() {
        return maxLength.get();
    }

    public final void setMaxLength(int value) {
        maxLength.set(value);
    }

    /** Error state. Set by the validator, or manually. */
    public final BooleanProperty errorProperty() {
        return error;
    }

    public final boolean isError() {
        return error.get();
    }

    public final void setError(boolean value) {
        error.set(value);
    }

    /** Error message from the validator, shown in place of the supporting text. */
    public final ReadOnlyStringProperty errorMessageProperty() {
        return errorMessage.getReadOnlyProperty();
    }

    /** Sets an error with a message (null message clears the error). */
    public final void setErrorMessage(String message) {
        errorMessage.set(message);
        error.set(message != null);
    }

    /**
     * Validator returning an error message, or null when the text is valid. Runs when the field
     * loses focus, then on every change.
     */
    public final ObjectProperty<Function<String, String>> validatorProperty() {
        return validator;
    }

    public final Function<String, String> getValidator() {
        return validator.get();
    }

    public final void setValidator(Function<String, String> value) {
        validator.set(value);
    }

    public final BooleanProperty passwordProperty() {
        return password;
    }

    public final boolean isPassword() {
        return password.get();
    }

    public final void setPassword(boolean value) {
        password.set(value);
    }

    public final BooleanProperty passwordVisibleProperty() {
        return passwordVisible;
    }

    public final boolean isPasswordVisible() {
        return passwordVisible.get();
    }

    public final void setPasswordVisible(boolean value) {
        passwordVisible.set(value);
    }
}
