package io.github.squoirelle.material3fx.controls;

import io.github.squoirelle.material3fx.skins.ValidationSummarySkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * Lists the problems found by a {@link FormPane}'s last {@link FormPane#validate()}: an error
 * container card with one link per problem; activating a link focuses the field. Hidden when there
 * are no problems.
 *
 * <pre>{@code
 * ValidationSummary summary = new ValidationSummary(form);
 * form.addNode(summary);          // or place it anywhere, e.g. above the form
 * save.setOnAction(e -> { if (form.validate()) save(); });
 * }</pre>
 */
public class ValidationSummary extends Control {

    private final ObjectProperty<FormPane> form = new SimpleObjectProperty<>(this, "form");
    private final ListChangeListener<FormPane.Problem> problemsListener = c -> updateVisibility();

    public ValidationSummary() {
        this(null);
    }

    public ValidationSummary(FormPane form) {
        getStyleClass().add("validation-summary");
        setFocusTraversable(false);
        setAccessibleRole(AccessibleRole.TEXT);
        this.form.addListener((obs, old, value) -> {
            if (old != null) {
                old.getProblems().removeListener(problemsListener);
            }
            if (value != null) {
                value.getProblems().addListener(problemsListener);
            }
            updateVisibility();
        });
        this.form.set(form);
        updateVisibility();
    }

    private void updateVisibility() {
        boolean show = getForm() != null && !getForm().getProblems().isEmpty();
        setVisible(show);
        setManaged(show);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ValidationSummarySkin(this);
    }

    public final ObjectProperty<FormPane> formProperty() {
        return form;
    }

    public final FormPane getForm() {
        return form.get();
    }

    public final void setForm(FormPane value) {
        form.set(value);
    }
}
