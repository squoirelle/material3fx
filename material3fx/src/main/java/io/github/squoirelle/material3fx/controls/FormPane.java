package io.github.squoirelle.material3fx.controls;

import javafx.beans.DefaultProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * A form layout: titled sections and rows of label + field, stacked vertically. Like
 * {@link javafx.scene.layout.GridPane} it is a layout pane, adapted to forms:
 * <ul>
 *     <li>labels sit beside their fields on wide forms and above them on narrow ones
 *     ({@link LabelPosition#AUTO}, switching at {@link #autoBreakpointProperty() 600px})</li>
 *     <li>beside fields, labels align with the field's text line</li>
 *     <li>{@link #validate()} validates every {@link Validatable} field and collects the
 *     {@link #getProblems() problems}, for a {@link ValidationSummary}</li>
 * </ul>
 *
 * <pre>{@code
 * FormPane form = new FormPane();
 * form.addSection("Identity");
 * form.addRow("Callsign", callsign).setRequired(true);
 * form.addRow("Period", period);
 * form.addRow(notes);                 // full width, no label (e.g. a field with its own floating label)
 * if (form.validate()) save();
 * }</pre>
 */
@DefaultProperty("children")
public class FormPane extends Region {

    public enum LabelPosition { AUTO, LEFT, TOP }

    /** A validation error: the field, its display label and the message. */
    public record Problem(Node field, String label, String message) {
    }

    private final ObjectProperty<LabelPosition> labelPosition = new SimpleObjectProperty<>(this, "labelPosition", LabelPosition.AUTO);
    private final DoubleProperty autoBreakpoint = new SimpleDoubleProperty(this, "autoBreakpoint", 600);
    private final DoubleProperty maxLabelWidth = new SimpleDoubleProperty(this, "maxLabelWidth", 240);
    private final DoubleProperty hgap = new SimpleDoubleProperty(this, "hgap", 24);
    private final DoubleProperty vgap = new SimpleDoubleProperty(this, "vgap", 16);
    private final DoubleProperty sectionGap = new SimpleDoubleProperty(this, "sectionGap", 32);
    private final ObservableList<Problem> problems = FXCollections.observableArrayList();
    private final ReadOnlyBooleanWrapper labelsOnTop = new ReadOnlyBooleanWrapper(this, "labelsOnTop");

    public FormPane() {
        getStyleClass().add("form-pane");
        for (var p : List.of(labelPosition, autoBreakpoint, maxLabelWidth, hgap, vgap, sectionGap)) {
            p.addListener(o -> requestLayout());
        }
    }

    /** Adds a labelled row. */
    public FormRow addRow(String label, Node field) {
        FormRow row = new FormRow(label, field);
        getChildren().add(row);
        return row;
    }

    /** Adds a full-width row without label. */
    public FormRow addRow(Node field) {
        return addRow(null, field);
    }

    /** Adds a section title. */
    public Label addSection(String title) {
        Label label = new Label(title);
        label.getStyleClass().add("form-section-title");
        getChildren().add(label);
        return label;
    }

    /** Adds any node (a hint, a divider, a {@link ValidationSummary}...) spanning the full width. */
    public void addNode(Node node) {
        getChildren().add(node);
    }

    @Override
    public ObservableList<Node> getChildren() {
        return super.getChildren();
    }

    /**
     * Validates every {@link Validatable} in the form, in order, updates {@link #getProblems()} and
     * returns true when all are valid.
     */
    public boolean validate() {
        List<Problem> found = new ArrayList<>();
        for (Node child : getChildren()) {
            String rowLabel = child instanceof FormRow row ? row.displayLabel() : null;
            collect(child, rowLabel, found);
        }
        problems.setAll(found);
        return found.isEmpty();
    }

    private static void collect(Node node, String rowLabel, List<Problem> found) {
        if (node instanceof Validatable v) {
            if (!v.validate()) {
                String label = rowLabel != null ? rowLabel : v.getValidationLabel();
                found.add(new Problem(node, label, v.getErrorMessage()));
            }
            return;
        }
        if (node instanceof Parent parent && !(node instanceof javafx.scene.control.Control)) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collect(child, rowLabel, found);
            }
        }
    }

    /** Problems found by the last {@link #validate()}. */
    public ObservableList<Problem> getProblems() {
        return problems;
    }

    private boolean computeLabelsOnTop(double width) {
        return switch (getLabelPosition()) {
            case TOP -> true;
            case LEFT -> false;
            case AUTO -> width < getAutoBreakpoint();
        };
    }

    private double labelColumnWidth() {
        double w = 0;
        for (Node child : getChildren()) {
            if (child instanceof FormRow row) {
                w = Math.max(w, row.labelPrefWidth());
            }
        }
        return Math.min(w, getMaxLabelWidth());
    }

    void layoutRow(FormRow row) {
        boolean top = labelsOnTop.get();
        row.layoutRow(top ? 0 : labelColumnWidth(), getHgap(), top);
    }

    @Override
    protected void layoutChildren() {
        double x = snappedLeftInset();
        double w = getWidth() - x - snappedRightInset();
        boolean top = computeLabelsOnTop(w);
        labelsOnTop.set(top);
        pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("labels-on-top"), top);
        double labelWidth = top ? 0 : labelColumnWidth();
        double y = snappedTopInset();
        boolean first = true;
        for (Node child : getChildren()) {
            if (!child.isManaged()) {
                continue;
            }
            boolean section = child.getStyleClass().contains("form-section-title");
            if (!first) {
                y += section ? getSectionGap() : getVgap();
            }
            double h = child instanceof FormRow row ? row.rowPrefHeight(w, labelWidth, getHgap(), top) : child.prefHeight(w);
            child.resizeRelocate(x, y, w, h);
            if (child instanceof FormRow row) {
                row.layoutRow(labelWidth, getHgap(), top);
            }
            y += h;
            first = false;
        }
    }

    /** The height depends on the width: labels go above the fields below the breakpoint. */
    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    @Override
    protected double computePrefHeight(double width) {
        double w = (width < 0 ? computePrefWidth(-1) : width) - snappedLeftInset() - snappedRightInset();
        boolean top = computeLabelsOnTop(w);
        double labelWidth = top ? 0 : labelColumnWidth();
        double h = snappedTopInset() + snappedBottomInset();
        boolean first = true;
        for (Node child : getChildren()) {
            if (!child.isManaged()) {
                continue;
            }
            if (!first) {
                h += child.getStyleClass().contains("form-section-title") ? getSectionGap() : getVgap();
            }
            h += child instanceof FormRow row ? row.rowPrefHeight(w, labelWidth, getHgap(), top) : child.prefHeight(w);
            first = false;
        }
        return h;
    }

    @Override
    protected double computePrefWidth(double height) {
        double fields = 0;
        for (Node child : getChildren()) {
            Node f = child instanceof FormRow row ? row.getField() : child;
            if (f != null) {
                fields = Math.max(fields, f.prefWidth(-1));
            }
        }
        return snappedLeftInset() + labelColumnWidth() + getHgap() + Math.max(280, fields) + snappedRightInset();
    }

    @Override
    protected double computeMinWidth(double height) {
        return snappedLeftInset() + 200 + snappedRightInset();
    }

    /** Whether labels are currently shown above their fields. */
    public final ReadOnlyBooleanProperty labelsOnTopProperty() {
        return labelsOnTop.getReadOnlyProperty();
    }

    public final ObjectProperty<LabelPosition> labelPositionProperty() {
        return labelPosition;
    }

    public final LabelPosition getLabelPosition() {
        return labelPosition.get();
    }

    public final void setLabelPosition(LabelPosition value) {
        labelPosition.set(value);
    }

    /** Width below which {@link LabelPosition#AUTO} puts labels on top (default 600). */
    public final DoubleProperty autoBreakpointProperty() {
        return autoBreakpoint;
    }

    public final double getAutoBreakpoint() {
        return autoBreakpoint.get();
    }

    public final void setAutoBreakpoint(double value) {
        autoBreakpoint.set(value);
    }

    public final DoubleProperty maxLabelWidthProperty() {
        return maxLabelWidth;
    }

    public final double getMaxLabelWidth() {
        return maxLabelWidth.get();
    }

    public final void setMaxLabelWidth(double value) {
        maxLabelWidth.set(value);
    }

    public final DoubleProperty hgapProperty() {
        return hgap;
    }

    public final double getHgap() {
        return hgap.get();
    }

    public final void setHgap(double value) {
        hgap.set(value);
    }

    public final DoubleProperty vgapProperty() {
        return vgap;
    }

    public final double getVgap() {
        return vgap.get();
    }

    public final void setVgap(double value) {
        vgap.set(value);
    }

    public final DoubleProperty sectionGapProperty() {
        return sectionGap;
    }

    public final double getSectionGap() {
        return sectionGap.get();
    }

    public final void setSectionGap(double value) {
        sectionGap.set(value);
    }
}
