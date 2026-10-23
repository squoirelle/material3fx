package io.github.squoirelle.material3fx.skins;

import io.github.squoirelle.material3fx.controls.DateRange;
import io.github.squoirelle.material3fx.controls.DateRangePicker;
import io.github.squoirelle.material3fx.controls.IconButton;
import io.github.squoirelle.material3fx.controls.MaterialTextField;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.Locale;

/**
 * Skin of {@link DateRangePicker}: a read-only {@link MaterialTextField} showing the range, with a
 * calendar button. Click, Enter, Space or Alt+Down open the dialog.
 */
public class DateRangePickerSkin extends SkinBase<DateRangePicker> {

    private final MaterialTextField field = new MaterialTextField();
    private final IconButton calendarButton = new IconButton("date_range");

    public DateRangePickerSkin(DateRangePicker picker) {
        super(picker);
        field.getStyleClass().add("date-range-field");
        field.setEditable(false);
        field.labelProperty().bind(picker.labelProperty());
        field.variantProperty().bind(picker.variantProperty());
        calendarButton.setFocusTraversable(false);
        calendarButton.setAccessibleText("Open calendar");
        calendarButton.setOnAction(e -> picker.show());
        field.setTrailing(calendarButton);
        field.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.PRIMARY && !picker.isDisabled()) {
                picker.show();
            }
        });
        field.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            boolean open = e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE
                    || (e.getCode() == KeyCode.DOWN && e.isAltDown()) || e.getCode() == KeyCode.F4;
            if (open) {
                picker.show();
                e.consume();
            }
        });
        getChildren().add(field);
        registerChangeListener(picker.valueProperty(), o -> updateText());
        registerChangeListener(picker.errorMessageProperty(), o -> updateSupporting());
        registerChangeListener(picker.supportingTextProperty(), o -> updateSupporting());
        updateText();
        updateSupporting();
    }

    private void updateText() {
        DateRange range = getSkinnable().getValue();
        field.setText(range == null ? "" : range.format(Locale.getDefault()));
    }

    private void updateSupporting() {
        DateRangePicker picker = getSkinnable();
        field.setErrorMessage(picker.getErrorMessage());
        field.setSupportingText(picker.getSupportingText());
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        field.resizeRelocate(x, y, w, h);
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return left + field.prefWidth(-1) + right;
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        return top + field.prefHeight(width) + bottom;
    }

    @Override
    protected double computeMaxHeight(double width, double top, double right, double bottom, double left) {
        return computePrefHeight(width, top, right, bottom, left);
    }

    @Override
    public double computeBaselineOffset(double top, double right, double bottom, double left) {
        return top + field.getBaselineOffset();
    }

    @Override
    public void dispose() {
        field.labelProperty().unbind();
        field.variantProperty().unbind();
        super.dispose();
    }
}
