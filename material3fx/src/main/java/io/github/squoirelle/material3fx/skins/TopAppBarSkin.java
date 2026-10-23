package io.github.squoirelle.material3fx.skins;

import io.github.squoirelle.material3fx.controls.TopAppBar;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Skin of {@link TopAppBar}. Small: one row with the title after the navigation icon.
 * Center-aligned: title centered between equal-width sides. Medium / large: a row of icons with
 * the title on its own line below.
 */
public class TopAppBarSkin extends SkinBase<TopAppBar> {

    private final VBox box = new VBox();
    private final HBox row = new HBox();
    private final HBox actionsBox = new HBox();
    private final StackPane navigationBox = new StackPane();
    private final Label titleLabel = new Label();
    private final ListChangeListener<Node> actionsListener = c -> actionsBox.getChildren().setAll(getSkinnable().getActions());

    public TopAppBarSkin(TopAppBar bar) {
        super(bar);
        box.getStyleClass().add("top-app-bar-content");
        row.getStyleClass().add("top-app-bar-row");
        actionsBox.getStyleClass().add("top-app-bar-actions");
        titleLabel.getStyleClass().add("top-app-bar-title");
        row.setAlignment(Pos.CENTER_LEFT);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        navigationBox.setMaxWidth(Region.USE_PREF_SIZE);
        actionsBox.setMaxWidth(Region.USE_PREF_SIZE);
        titleLabel.textProperty().bind(bar.titleProperty());
        bar.getActions().addListener(actionsListener);
        actionsBox.getChildren().setAll(bar.getActions());
        getChildren().add(box);
        registerChangeListener(bar.navigationIconProperty(), o -> rebuild());
        registerChangeListener(bar.variantProperty(), o -> rebuild());
        rebuild();
    }

    private void rebuild() {
        TopAppBar bar = getSkinnable();
        box.getChildren().clear();
        row.getChildren().clear();
        navigationBox.getChildren().clear();
        if (bar.getNavigationIcon() != null) {
            navigationBox.getChildren().add(bar.getNavigationIcon());
        }
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        switch (bar.getVariant()) {
            case SMALL -> {
                row.getChildren().addAll(navigationBox, titleLabel, spacer, actionsBox);
                box.getChildren().add(row);
            }
            case CENTER_ALIGNED -> {
                // Equal-width sides keep the title centered whatever the number of actions.
                StackPane left = new StackPane(navigationBox);
                StackPane right = new StackPane(actionsBox);
                left.setAlignment(Pos.CENTER_LEFT);
                right.setAlignment(Pos.CENTER_RIGHT);
                HBox.setHgrow(left, Priority.ALWAYS);
                HBox.setHgrow(right, Priority.ALWAYS);
                left.setPrefWidth(0);
                right.setPrefWidth(0);
                row.getChildren().addAll(left, titleLabel, right);
                box.getChildren().add(row);
            }
            case MEDIUM, LARGE -> {
                row.getChildren().addAll(navigationBox, spacer, actionsBox);
                box.getChildren().addAll(row, titleLabel);
            }
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
        getSkinnable().getActions().removeListener(actionsListener);
        titleLabel.textProperty().unbind();
        super.dispose();
    }
}
