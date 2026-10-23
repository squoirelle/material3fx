package io.github.squoirelle.material3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

/**
 * One row of a {@link FormPane}: an optional label and a field. Positioned by the form, which
 * decides whether the label sits beside or above the field.
 */
public class FormRow extends Region {

    private final Label label = new Label();
    private final Label requiredMarker = new Label("*");
    private final ObjectProperty<Node> field = new SimpleObjectProperty<>(this, "field");
    private final BooleanProperty required = new SimpleBooleanProperty(this, "required");

    public FormRow(String label, Node field) {
        getStyleClass().add("form-row");
        this.label.getStyleClass().add("form-row-label");
        this.label.setWrapText(true);
        requiredMarker.getStyleClass().add("form-row-required");
        this.label.setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
        this.label.setText(label);
        this.field.addListener((obs, old, value) -> {
            getChildren().remove(old);
            if (value != null) {
                getChildren().add(value);
                this.label.setLabelFor(value);
            }
        });
        getChildren().add(this.label);
        this.field.set(field);
        required.addListener(o -> updateLabel());
        updateLabel();
    }

    private void updateLabel() {
        // The marker is the label's graphic, so the label text stays exactly what was set.
        label.setGraphic(isRequired() ? requiredMarker : null);
        label.setVisible(hasLabel());
        label.setManaged(hasLabel());
        pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("required"), isRequired());
    }

    boolean hasLabel() {
        return label.getText() != null && !label.getText().isEmpty();
    }

    Label labelNode() {
        return label;
    }

    /** Label text shown for errors: the row label, or the field's own validation label. */
    String displayLabel() {
        if (hasLabel()) {
            return label.getText();
        }
        return getField() instanceof Validatable v ? v.getValidationLabel() : null;
    }

    double labelPrefWidth() {
        return hasLabel() ? label.prefWidth(-1) : 0;
    }

    /** Lays the row out with the label column width given by the form (0 = label on top). */
    void layoutRow(double labelWidth, double gap, boolean labelOnTop) {
        Node f = getField();
        double w = getWidth() - snappedLeftInset() - snappedRightInset();
        double x = snappedLeftInset();
        double y = snappedTopInset();
        if (labelOnTop || !hasLabel()) {
            double lh = hasLabel() ? label.prefHeight(w) : 0;
            if (hasLabel()) {
                label.resizeRelocate(x, y, w, lh);
            }
            if (f != null) {
                double fy = y + (hasLabel() ? lh + 4 : 0);
                f.resizeRelocate(x, fy, w, Math.max(0, getHeight() - fy - snappedBottomInset()));
            }
            return;
        }
        double fieldX = x + labelWidth + gap;
        double fieldWidth = Math.max(0, w - labelWidth - gap);
        if (f != null) {
            double fh = f.prefHeight(fieldWidth);
            f.resizeRelocate(fieldX, y, fieldWidth, fh);
            // Label on the field's text line: align baselines.
            double lh = label.prefHeight(labelWidth);
            double baseline = f.getBaselineOffset();
            // Combo boxes, choice boxes and spinners report no useful baseline: center the label on them.
            boolean useBaseline = !(f instanceof javafx.scene.control.ComboBoxBase<?>
                    || f instanceof javafx.scene.control.ChoiceBox<?> || f instanceof javafx.scene.control.Spinner<?>);
            double ly = useBaseline && baseline != BASELINE_OFFSET_SAME_AS_HEIGHT && baseline > 0
                    ? y + baseline - label.getBaselineOffset()
                    : y + (fh - lh) / 2;
            label.resizeRelocate(x, Math.max(y, ly), labelWidth, lh);
        }
    }

    double rowPrefHeight(double width, double labelWidth, double gap, boolean labelOnTop) {
        Node f = getField();
        double fh;
        if (labelOnTop || !hasLabel()) {
            fh = f == null ? 0 : f.prefHeight(width);
            return (hasLabel() ? label.prefHeight(width) + 4 : 0) + fh;
        }
        fh = f == null ? 0 : f.prefHeight(Math.max(0, width - labelWidth - gap));
        return Math.max(fh, label.prefHeight(labelWidth));
    }

    @Override
    protected void layoutChildren() {
        if (getParent() instanceof FormPane form) {
            form.layoutRow(this);
        } else {
            layoutRow(Math.min(200, labelPrefWidth()), 16, false);
        }
    }

    public final StringProperty labelProperty() {
        return label.textProperty();
    }

    public final ObjectProperty<Node> fieldProperty() {
        return field;
    }

    public final Node getField() {
        return field.get();
    }

    public final void setField(Node value) {
        field.set(value);
    }

    /** Shows a required marker after the label (the field's own validation enforces it). */
    public final BooleanProperty requiredProperty() {
        return required;
    }

    public final boolean isRequired() {
        return required.get();
    }

    public final void setRequired(boolean value) {
        required.set(value);
    }
}
