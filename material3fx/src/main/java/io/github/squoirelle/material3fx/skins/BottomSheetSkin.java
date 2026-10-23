package io.github.squoirelle.material3fx.skins;

import io.github.squoirelle.material3fx.controls.BottomSheet;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Skin of {@link BottomSheet}: drag handle above the content; dragging the handle down dismisses. */
public class BottomSheetSkin extends SkinBase<BottomSheet> {

    private final VBox box = new VBox();
    private final Region handle = new Region();
    private final StackPane handleArea = new StackPane(handle);
    private double dragStartY;

    public BottomSheetSkin(BottomSheet sheet) {
        super(sheet);
        box.getStyleClass().add("bottom-sheet-content");
        handle.getStyleClass().add("bottom-sheet-handle");
        handleArea.getStyleClass().add("bottom-sheet-handle-area");
        handleArea.setAlignment(Pos.CENTER);
        handleArea.setCursor(Cursor.OPEN_HAND);
        handleArea.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> dragStartY = e.getSceneY());
        handleArea.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> sheet.setTranslateY(Math.max(0, e.getSceneY() - dragStartY)));
        handleArea.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (sheet.getTranslateY() > sheet.getHeight() * 0.3) {
                sheet.hide();
            } else {
                sheet.setTranslateY(0);
            }
        });
        getChildren().add(box);
        registerChangeListener(sheet.contentProperty(), o -> rebuild());
        rebuild();
    }

    private void rebuild() {
        box.getChildren().setAll(handleArea);
        if (getSkinnable().getContent() != null) {
            box.getChildren().add(getSkinnable().getContent());
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
        return top + box.prefHeight(width < 0 ? -1 : width - left - right) + bottom;
    }

    @Override
    protected double computeMaxHeight(double width, double top, double right, double bottom, double left) {
        return computePrefHeight(width, top, right, bottom, left);
    }
}
