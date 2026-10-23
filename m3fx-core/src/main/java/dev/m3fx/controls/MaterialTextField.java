package dev.m3fx.controls;

import dev.m3fx.skins.MaterialTextFieldSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;

import java.util.function.Function;

/**
 * Material 3 text field, filled or outlined, with a floating label.
 * <p>
 * A real {@link TextField}: text, {@code TextFormatter}, {@code onAction}, prompt text, selection,
 * context menu and accessibility all work as usual. The Material decorations (label, supporting
 * text, icons, prefix / suffix, counter) are drawn by its skin.
 *
 * <pre>{@code
 * MaterialTextField callsign = new MaterialTextField("Callsign");
 * callsign.setSupportingText("Three to eight letters");
 * callsign.setMaxLength(8);
 * callsign.setValidator(text -> text.length() < 3 ? "Too short" : null);
 *
 * MaterialTextField password = MaterialTextField.password("Password");
 * }</pre>
 */
public class MaterialTextField extends TextField implements Validatable {

    public enum Variant { FILLED, OUTLINED }

    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");
    private static final char BULLET = '\u25cf';

    private final StringProperty label = new SimpleStringProperty(this, "label");
    private final StringProperty supportingText = new SimpleStringProperty(this, "supportingText");
    private final StringProperty leadingIcon = new SimpleStringProperty(this, "leadingIcon");
    private final ObjectProperty<Node> trailing = new SimpleObjectProperty<>(this, "trailing");
    private final StringProperty prefix = new SimpleStringProperty(this, "prefix");
    private final StringProperty suffix = new SimpleStringProperty(this, "suffix");
    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant");
    private final IntegerProperty maxLength = new SimpleIntegerProperty(this, "maxLength", -1);
    private final BooleanProperty error = new SimpleBooleanProperty(this, "error") {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(ERROR, get());
        }
    };
    private final ReadOnlyStringWrapper errorMessage = new ReadOnlyStringWrapper(this, "errorMessage");
    private final ObjectProperty<Function<String, String>> validator = new SimpleObjectProperty<>(this, "validator");
    private final BooleanProperty password = new SimpleBooleanProperty(this, "password");
    private final BooleanProperty passwordVisible = new SimpleBooleanProperty(this, "passwordVisible");
    private boolean touched;

    public MaterialTextField() {
        this(null);
    }

    public MaterialTextField(String label) {
        this(label, Variant.OUTLINED);
    }

    public MaterialTextField(String label, Variant variant) {
        // Not a ".text-field": the outlined look of plain text fields must not apply.
        getStyleClass().setAll("text-input", "material-text-field");
        this.variant.addListener((obs, old, value) -> {
            if (old != null) {
                getStyleClass().remove(old.name().toLowerCase());
            }
            if (value != null) {
                getStyleClass().add(value.name().toLowerCase());
            }
        });
        this.variant.set(variant);
        this.label.set(label);
        accessibleTextProperty().bind(this.label);

        focusedProperty().addListener((obs, old, focused) -> {
            if (!focused && getValidator() != null) {
                validate();
            }
        });
        textProperty().addListener(o -> {
            if (touched) {
                validate();
            }
        });
        maxLength.addListener(o -> {
            int max = getMaxLength();
            if (max >= 0 && getLength() > max) {
                setText(getText().substring(0, max));
            }
        });
        password.addListener((obs, old, value) -> {
            setAccessibleRole(value ? AccessibleRole.PASSWORD_FIELD : AccessibleRole.TEXT_FIELD);
            refreshSkin();
        });
        // The skin masks the text when it is built: rebuild it when visibility changes.
        passwordVisible.addListener(o -> refreshSkin());
    }

    /** A password field with a visibility toggle. */
    public static MaterialTextField password(String label) {
        MaterialTextField field = new MaterialTextField(label);
        field.setPassword(true);
        return field;
    }

    private void refreshSkin() {
        if (getSkin() instanceof MaterialTextFieldSkin) {
            setSkin(new MaterialTextFieldSkin(this));
        }
    }

    /** Whether the text is currently hidden (password not revealed). */
    public boolean isMasked() {
        return isPassword() && !isPasswordVisible();
    }

    /** The text as displayed: bullets for a hidden password. */
    public String maskedText(String text) {
        return isMasked() ? String.valueOf(BULLET).repeat(text.length()) : text;
    }

    /** Enforces {@link #maxLengthProperty() maxLength} on typing and pasting. */
    @Override
    public void replaceText(int start, int end, String text) {
        int max = getMaxLength();
        if (max >= 0 && text != null) {
            int room = max - (getLength() - (end - start));
            if (room <= 0) {
                text = "";
            } else if (text.length() > room) {
                text = text.substring(0, room);
            }
            if (text.isEmpty() && start == end) {
                return;
            }
        }
        super.replaceText(start, end, text);
    }

    /** Hidden passwords cannot be cut (like {@link javafx.scene.control.PasswordField}). */
    @Override
    public void cut() {
        if (!isMasked()) {
            super.cut();
        }
    }

    /** Hidden passwords cannot be copied (like {@link javafx.scene.control.PasswordField}). */
    @Override
    public void copy() {
        if (!isMasked()) {
            super.copy();
        }
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        if (attribute == AccessibleAttribute.TEXT && isMasked()) {
            return null;
        }
        return super.queryAccessibleAttribute(attribute, parameters);
    }

    @Override
    public String getValidationLabel() {
        return getLabel();
    }

    /** Runs the validator now, and marks the field as touched so it re-validates on each change. */
    @Override
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

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MaterialTextFieldSkin(this);
    }

    /** The floating label. */
    public final StringProperty labelProperty() {
        return label;
    }

    public final String getLabel() {
        return label.get();
    }

    public final void setLabel(String value) {
        label.set(value);
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

    /** Convenience: a trailing icon button clearing the text, visible when there is text. */
    public final void setClearable(boolean clearable) {
        if (clearable) {
            IconButton clear = new IconButton("cancel");
            clear.setFocusTraversable(false);
            clear.setAccessibleText("Clear");
            clear.setOnAction(e -> {
                clear();
                requestFocus();
            });
            clear.visibleProperty().bind(textProperty().isNotEmpty());
            setTrailing(clear);
        } else {
            setTrailing(null);
        }
    }

    /** Text shown before the input once the label floats, e.g. a currency. */
    public final StringProperty prefixProperty() {
        return prefix;
    }

    public final String getPrefix() {
        return prefix.get();
    }

    public final void setPrefix(String value) {
        prefix.set(value);
    }

    /** Text shown after the input once the label floats, e.g. a unit. */
    public final StringProperty suffixProperty() {
        return suffix;
    }

    public final String getSuffix() {
        return suffix.get();
    }

    public final void setSuffix(String value) {
        suffix.set(value);
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

    @Override
    public final String getErrorMessage() {
        return errorMessage.get();
    }

    /** Sets an error with a message (null clears the error). */
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

    /** Hides the text behind bullets and shows a visibility toggle. */
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
