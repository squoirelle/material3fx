package io.github.squoirelle.material3fx.sample;

import io.github.squoirelle.material3fx.controls.FormPane;
import io.github.squoirelle.material3fx.controls.MaterialTextField;
import io.github.squoirelle.material3fx.controls.NumberField;
import io.github.squoirelle.material3fx.controls.SegmentedButton;
import io.github.squoirelle.material3fx.controls.Snackbar;
import io.github.squoirelle.material3fx.controls.Switch;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;

import java.time.LocalDate;

/** Creates or edits an item, validates it, and saves it in the background. */
public class ItemFormController implements PageController {

    private final AppContext context;

    @FXML private Label title;
    @FXML private FormPane form;
    @FXML private MaterialTextField name;
    @FXML private MaterialTextField sku;
    @FXML private ComboBox<String> category;
    @FXML private SegmentedButton unit;
    @FXML private NumberField quantity;
    @FXML private NumberField reorderLevel;
    // Not "location": FXMLLoader injects the FXML file URL into a field of that name.
    @FXML private ChoiceBox<String> locationChoice;
    @FXML private DatePicker received;
    @FXML private Switch notify;
    @FXML private TextArea notes;
    @FXML private Button saveButton;
    @FXML private ProgressIndicator saving;

    public ItemFormController(AppContext context) {
        this.context = context;
    }

    @FXML
    private void initialize() {
        category.getItems().setAll(InventoryService.CATEGORIES);
        locationChoice.getItems().setAll(InventoryService.LOCATIONS);
        name.setValidator(text -> text.isBlank() ? "Enter a name" : null);
        sku.setValidator(text -> text.isBlank() ? "Enter a SKU"
                : text.matches("[A-Za-z]{3}-\\d{3,8}") ? null : "Three letters, a dash and digits, e.g. TOO-007");
        reset();
    }

    @Override
    public void shown() {
        Item item = context.editedItemProperty().get();
        title.setText(item == null ? "New item" : "Edit " + item.getName());
        if (item == null) {
            reset();
        } else {
            fill(item);
        }
        name.requestFocus();
    }

    private void fill(Item item) {
        name.setText(item.getName());
        sku.setText(item.getSku());
        category.setValue(item.getCategory());
        locationChoice.setValue(item.getLocation());
        quantity.setValue((double) item.getQuantity());
        reorderLevel.setValue((double) item.getReorderLevel());
        received.setValue(item.getReceived());
        unit.getButtons().forEach(b -> b.setSelected(b.getUserData().equals(item.getUnit())));
        clearErrors();
    }

    @FXML
    private void reset() {
        name.clear();
        sku.clear();
        notes.clear();
        category.setValue(InventoryService.CATEGORIES.get(0));
        locationChoice.setValue(InventoryService.LOCATIONS.get(0));
        quantity.setValue(0.0);
        reorderLevel.setValue(10.0);
        received.setValue(LocalDate.now());
        notify.setSelected(true);
        unit.getButtons().get(0).setSelected(true);
        clearErrors();
    }

    private void clearErrors() {
        name.setError(false);
        sku.setError(false);
        form.getProblems().clear();
    }

    @FXML
    private void save() {
        if (!form.validate()) {
            return;   // the fields show their errors, the summary lists them
        }
        Item edited = context.editedItemProperty().get();
        Item item = edited != null ? edited.copy() : new Item(null, null, null, null, 0, 0);
        item.setName(name.getText().trim());
        item.setSku(sku.getText().trim().toUpperCase());
        item.setCategory(category.getValue());
        item.setLocation(locationChoice.getValue());
        item.setQuantity(quantity.getValue() == null ? 0 : quantity.getValue().intValue());
        item.setReorderLevel(reorderLevel.getValue() == null ? 0 : reorderLevel.getValue().intValue());
        item.setReceived(received.getValue());
        unit.getButtons().stream().filter(ToggleButton::isSelected).findFirst()
                .ifPresent(b -> item.setUnit((String) b.getUserData()));

        // Saving takes a moment: the button shows a progress indicator and is disabled meanwhile.
        Task<Item> task = context.service().save(item);
        setSaving(true);
        task.setOnSucceeded(e -> {
            setSaving(false);
            // A refresh while editing replaces the items: the edited one may be gone.
            int index = edited == null ? -1 : context.items().indexOf(edited);
            if (index >= 0) {
                context.items().set(index, item);
            } else {
                context.items().add(0, item);
            }
            context.editedItemProperty().set(null);
            Snackbar.make(saveButton, "Saved " + item.getName())
                    .action("View inventory", () -> context.navigate(Page.INVENTORY))
                    .show();
            shown();
        });
        task.setOnFailed(e -> {
            setSaving(false);
            Snackbar.make(saveButton, "Could not save: " + task.getException().getMessage()).show();
        });
    }

    private void setSaving(boolean value) {
        saveButton.setDisable(value);
        saving.setVisible(value);
        saving.setManaged(value);
        saveButton.setText(value ? "Saving..." : "Save");
    }
}
