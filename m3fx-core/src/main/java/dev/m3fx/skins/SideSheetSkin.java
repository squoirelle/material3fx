package dev.m3fx.skins;

import dev.m3fx.controls.IconButton;
import dev.m3fx.controls.SideSheet;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Skin of {@link SideSheet}: header (title, close), content, action bar. */
public class SideSheetSkin extends SkinBase<SideSheet> {

    private final VBox box = new VBox();
    private final HBox header = new HBox();
    private final Label title = new Label();
    private final HBox actions = new HBox(8);
    private final Region filler = new Region();
    private final ListChangeListener<Node> actionsListener = c -> rebuild();

    public SideSheetSkin(SideSheet sheet) {
        super(sheet);
        box.getStyleClass().add("side-sheet-content");
        header.getStyleClass().add("side-sheet-header");
        title.getStyleClass().add("side-sheet-headline");
        actions.getStyleClass().add("side-sheet-actions");
        title.textProperty().bind(sheet.titleProperty());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        IconButton close = new IconButton("close");
        close.setAccessibleText("Close");
        close.setOnAction(e -> sheet.hide());
        header.getChildren().addAll(title, spacer, close);
        header.setAlignment(Pos.CENTER_LEFT);
        actions.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(filler, Priority.ALWAYS);
        sheet.getActions().addListener(actionsListener);
        getChildren().add(box);
        registerChangeListener(sheet.contentProperty(), o -> rebuild());
        rebuild();
    }

    private void rebuild() {
        SideSheet sheet = getSkinnable();
        box.getChildren().setAll(header);
        Node content = sheet.getContent();
        if (content != null) {
            VBox.setVgrow(content, Priority.ALWAYS);
            box.getChildren().add(content);
        } else {
            box.getChildren().add(filler);
        }
        actions.getChildren().setAll(sheet.getActions());
        if (!sheet.getActions().isEmpty()) {
            box.getChildren().add(actions);
        }
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        box.resizeRelocate(x, y, w, h);
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return left + box.prefWidth(-1) + right;
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        return top + box.prefHeight(-1) + bottom;
    }

    @Override
    public void dispose() {
        getSkinnable().getActions().removeListener(actionsListener);
        super.dispose();
    }
}
