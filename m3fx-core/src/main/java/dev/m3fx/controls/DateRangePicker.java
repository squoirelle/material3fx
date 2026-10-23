package dev.m3fx.controls;

import dev.m3fx.skins.DateRangePickerSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * A date range field: shows the selected {@link DateRange} like a Material text field and opens a
 * {@link DateRangePickerDialog} on click, Enter, Space or Alt+Down. Like
 * {@link javafx.scene.control.DatePicker}, the selection is the {@link #valueProperty() value}.
 *
 * <pre>{@code
 * DateRangePicker period = new DateRangePicker("Mission period");
 * period.setRequired(true);
 * period.valueProperty().addListener((obs, old, range) -> filter(range));
 * }</pre>
 */
public class DateRangePicker extends Control implements Validatable {

    private final ObjectProperty<DateRange> value = new SimpleObjectProperty<>(this, "value");
    private final StringProperty label = new SimpleStringProperty(this, "label");
    private final StringProperty supportingText = new SimpleStringProperty(this, "supportingText");
    private final BooleanProperty required = new SimpleBooleanProperty(this, "required");
    private final ReadOnlyStringWrapper errorMessage = new ReadOnlyStringWrapper(this, "errorMessage");
    private final ObjectProperty<MaterialTextField.Variant> variant =
            new SimpleObjectProperty<>(this, "variant", MaterialTextField.Variant.OUTLINED);

    public DateRangePicker() {
        this(null);
    }

    public DateRangePicker(String label) {
        getStyleClass().add("date-range-picker");
        setFocusTraversable(false);
        this.label.set(label);
        value.addListener(o -> {
            if (errorMessage.get() != null) {
                validate();
            }
        });
    }

    /** Opens the picker dialog; the chosen range becomes the value. */
    public void show() {
        DateRangePickerDialog.show(this, getValue(), getLabel() == null ? "Select range" : getLabel())
                .thenAccept(range -> {
                    if (range != null) {
                        setValue(range);
                    }
                });
    }

    /** Focuses the inner field (the control itself is not focus traversable). */
    @Override
    public void requestFocus() {
        javafx.scene.Node field = lookup(".date-range-field");
        if (field != null) {
            field.requestFocus();
        } else {
            super.requestFocus();
        }
    }

    @Override
    public boolean validate() {
        errorMessage.set(isRequired() && getValue() == null ? "Select a date range" : null);
        return errorMessage.get() == null;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage.get();
    }

    @Override
    public String getValidationLabel() {
        return getLabel();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DateRangePickerSkin(this);
    }

    public final ObjectProperty<DateRange> valueProperty() {
        return value;
    }

    public final DateRange getValue() {
        return value.get();
    }

    public final void setValue(DateRange v) {
        value.set(v);
    }

    public final StringProperty labelProperty() {
        return label;
    }

    public final String getLabel() {
        return label.get();
    }

    public final void setLabel(String v) {
        label.set(v);
    }

    public final StringProperty supportingTextProperty() {
        return supportingText;
    }

    public final void setSupportingText(String v) {
        supportingText.set(v);
    }

    public final String getSupportingText() {
        return supportingText.get();
    }

    /** When true, {@link #validate()} fails while no range is selected. */
    public final BooleanProperty requiredProperty() {
        return required;
    }

    public final boolean isRequired() {
        return required.get();
    }

    public final void setRequired(boolean v) {
        required.set(v);
    }

    public final ReadOnlyStringProperty errorMessageProperty() {
        return errorMessage.getReadOnlyProperty();
    }

    public final ObjectProperty<MaterialTextField.Variant> variantProperty() {
        return variant;
    }

    public final MaterialTextField.Variant getVariant() {
        return variant.get();
    }

    public final void setVariant(MaterialTextField.Variant v) {
        variant.set(v);
    }
}
