package dev.m3fx.controls;

/**
 * A field that can validate its own value, used by {@link FormPane#validate()}.
 * {@link MaterialTextField} (and so {@link NumberField}) and {@link DateRangePicker} implement it.
 */
public interface Validatable {

    /** Validates the current value and shows the error, if any. Returns true when valid. */
    boolean validate();

    /** The current error message, or null when valid. */
    String getErrorMessage();

    /** A short name for the field in error summaries (usually its label). */
    String getValidationLabel();
}
