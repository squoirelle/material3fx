package io.github.squoirelle.material3fx.demo;

import io.github.squoirelle.material3fx.controls.Buttons;
import io.github.squoirelle.material3fx.controls.DateRangePicker;
import io.github.squoirelle.material3fx.controls.FormPane;
import io.github.squoirelle.material3fx.controls.Icon;
import io.github.squoirelle.material3fx.controls.MaterialTextField;
import io.github.squoirelle.material3fx.controls.NumberField;
import io.github.squoirelle.material3fx.controls.RangeSlider;
import io.github.squoirelle.material3fx.controls.Snackbar;
import io.github.squoirelle.material3fx.controls.SplitButton;
import io.github.squoirelle.material3fx.controls.ValidationSummary;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Forms: form pane, number fields, date range, range slider, validation summary, split button. */
final class DataEntryPage {

    private DataEntryPage() {
    }

    static Node page() {
        // Fields in labelled form rows have no floating label of their own: the row label names them.
        MaterialTextField callsign = new MaterialTextField();
        callsign.setPromptText("e.g. EAGLE7");
        callsign.setMaxLength(8);
        callsign.setValidator(t -> t.length() < 3 ? "At least 3 characters" : null);
        ComboBox<String> unit = new ComboBox<>(FXCollections.observableArrayList("Alpha squadron", "Bravo squadron", "Charlie squadron"));
        unit.setPromptText("Select");
        unit.setMaxWidth(Double.MAX_VALUE);
        DateRangePicker period = new DateRangePicker();
        period.setRequired(true);
        NumberField crew = new NumberField();
        crew.setMin(1);
        crew.setMax(12);
        crew.setDecimals(0);
        crew.setValue(4.0);
        NumberField altitude = new NumberField();
        altitude.setSuffix("ft");
        altitude.setMin(0);
        altitude.setMax(45000);
        altitude.setStep(500);
        altitude.setDecimals(0);
        NumberField fuel = new NumberField();
        fuel.setSuffix("%");
        fuel.setMin(0);
        fuel.setMax(100);
        fuel.setValue(12.5);

        RangeSlider band = new RangeSlider(118, 137, 121.5, 128.0);
        band.setBlockIncrement(0.5);
        band.setMajorTickUnit(1);
        band.setShowTickMarks(true);
        Label bandLabel = new Label();
        bandLabel.getStyleClass().add("on-surface-variant");
        bandLabel.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(
                () -> String.format("%.1f - %.1f MHz", band.getLowValue(), band.getHighValue()),
                band.lowValueProperty(), band.highValueProperty()));
        HBox bandBox = new HBox(16, band, bandLabel);
        bandBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(band, javafx.scene.layout.Priority.ALWAYS);

        FormPane form = new FormPane();
        ValidationSummary summary = new ValidationSummary(form);
        form.addNode(summary);
        form.addSection("Mission");
        form.addRow("Callsign", callsign).setRequired(true);
        form.addRow("Unit", unit);
        form.addRow("Period", period).setRequired(true);
        form.addSection("Flight plan");
        form.addRow("Crew", crew);
        form.addRow("Altitude", altitude);
        form.addRow("Fuel reserve", fuel);
        form.addRow("Radio band", bandBox);
        form.setMaxWidth(720);

        SplitButton save = new SplitButton("Save", new Icon("save", 18),
                new MenuItem("Save as draft"), new MenuItem("Save and submit"), new MenuItem("Export as PDF"));
        save.setOnAction(e -> {
            if (form.validate()) {
                Snackbar.show(save, "Mission saved");
            }
        });
        Button reset = Buttons.text("Reset");
        reset.setOnAction(e -> {
            callsign.clear();
            period.setValue(null);
            form.getProblems().clear();
        });
        SplitButton tonal = new SplitButton("Export", null, new MenuItem("PDF"), new MenuItem("CSV"));
        tonal.setVariant(SplitButton.Variant.TONAL);
        SplitButton outlined = new SplitButton("Share", null, new MenuItem("Email"), new MenuItem("Link"));
        outlined.setVariant(SplitButton.Variant.OUTLINED);
        HBox actions = new HBox(12, save, reset);
        actions.setAlignment(Pos.CENTER_LEFT);

        return Gallery.page("Data entry", "FormPane puts labels beside fields on wide windows and above them on narrow ones. "
                        + "Save validates every field and lists the problems; click one to jump to the field.",
                Gallery.section("Form", form, actions),
                Gallery.section("Split buttons: filled, tonal, outlined", Gallery.row(
                        new SplitButton("Save", null, new MenuItem("Save as...")), tonal, outlined)));
    }
}
