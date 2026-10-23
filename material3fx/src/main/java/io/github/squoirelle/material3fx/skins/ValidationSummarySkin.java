package io.github.squoirelle.material3fx.skins;

import io.github.squoirelle.material3fx.controls.FormPane;
import io.github.squoirelle.material3fx.controls.Icon;
import io.github.squoirelle.material3fx.controls.ValidationSummary;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Skin of {@link ValidationSummary}: icon and title, then one link per problem. */
public class ValidationSummarySkin extends SkinBase<ValidationSummary> {

    private final VBox box = new VBox();
    private final Label title = new Label();
    private final VBox links = new VBox();
    private final ListChangeListener<FormPane.Problem> problemsListener = c -> rebuild();
    private FormPane observed;

    public ValidationSummarySkin(ValidationSummary summary) {
        super(summary);
        box.getStyleClass().add("validation-summary-content");
        title.getStyleClass().add("validation-summary-title");
        links.getStyleClass().add("validation-summary-links");
        Icon icon = new Icon("error");
        icon.setFollowTextFill(false);
        icon.getStyleClass().add("validation-summary-icon");
        HBox header = new HBox(12, icon, title);
        header.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().addAll(header, links);
        getChildren().add(box);
        registerChangeListener(summary.formProperty(), o -> observe());
        observe();
    }

    private void observe() {
        if (observed != null) {
            observed.getProblems().removeListener(problemsListener);
        }
        observed = getSkinnable().getForm();
        if (observed != null) {
            observed.getProblems().addListener(problemsListener);
        }
        rebuild();
    }

    private void rebuild() {
        links.getChildren().clear();
        if (observed == null) {
            return;
        }
        int count = observed.getProblems().size();
        title.setText(count == 1 ? "1 field needs attention" : count + " fields need attention");
        getSkinnable().setAccessibleText(title.getText());
        for (FormPane.Problem problem : observed.getProblems()) {
            String text = problem.label() == null ? problem.message() : problem.label() + ": " + problem.message();
            Hyperlink link = new Hyperlink(text);
            link.getStyleClass().add("validation-summary-link");
            link.setOnAction(e -> problem.field().requestFocus());
            links.getChildren().add(link);
        }
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        box.resizeRelocate(x, y, w, h);
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        return top + box.prefHeight(width < 0 ? -1 : width - left - right) + bottom;
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return left + box.prefWidth(-1) + right;
    }

    @Override
    protected double computeMaxHeight(double width, double top, double right, double bottom, double left) {
        return computePrefHeight(width, top, right, bottom, left);
    }

    @Override
    public void dispose() {
        if (observed != null) {
            observed.getProblems().removeListener(problemsListener);
        }
        super.dispose();
    }
}
