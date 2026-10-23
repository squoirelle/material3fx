package io.github.squoirelle.material3fx.sample;

import io.github.squoirelle.material3fx.controls.Chip;
import io.github.squoirelle.material3fx.controls.MaterialDialog;
import io.github.squoirelle.material3fx.controls.SearchBar;
import io.github.squoirelle.material3fx.controls.Snackbar;
import io.github.squoirelle.material3fx.controls.StatusLight;
import javafx.beans.binding.Bindings;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.Locale;

/** Inventory: a searchable, filterable table with a context menu. */
public class InventoryController implements PageController {

    private final AppContext context;
    private final FilteredList<Item> filtered;

    @FXML private SearchBar search;
    @FXML private Chip lowFilter;
    @FXML private Chip outFilter;
    @FXML private FlowPane filters;
    @FXML private TableView<Item> table;
    @FXML private TableColumn<Item, Number> quantityColumn;
    @FXML private TableColumn<Item, Item.Stock> stockColumn;
    @FXML private VBox loadingOverlay;
    @FXML private Label countLabel;

    public InventoryController(AppContext context) {
        this.context = context;
        this.filtered = new FilteredList<>(context.items());
    }

    private final ToggleGroup categories = new ToggleGroup();

    @FXML
    private void initialize() {
        // A chip is a ToggleButton: in a ToggleGroup, category chips select one category at a time.
        for (String category : InventoryService.CATEGORIES) {
            Chip chip = new Chip(category, Chip.Variant.FILTER);
            chip.setUserData(category);
            chip.setToggleGroup(categories);
            filters.getChildren().add(chip);
        }

        // The search bar suggests matching item names as you type.
        search.setSuggestionProvider(text -> context.items().stream().map(Item::getName)
                .filter(name -> !text.isBlank() && name.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT)))
                .distinct().limit(6).toList());
        search.textProperty().addListener(o -> updateFilter());
        lowFilter.selectedProperty().addListener(o -> updateFilter());
        outFilter.selectedProperty().addListener(o -> updateFilter());
        categories.selectedToggleProperty().addListener(o -> updateFilter());

        SortedList<Item> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        table.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.SINGLE);

        quantityColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%,d", value.intValue()));
            }
        });
        stockColumn.setCellFactory(column -> new TableCell<>() {
            private final StatusLight light = new StatusLight();

            @Override
            protected void updateItem(Item.Stock stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setGraphic(null);
                    return;
                }
                light.setText(stock.toString());
                light.setStatus(switch (stock) {
                    case IN_STOCK -> StatusLight.Status.SUCCESS;
                    case LOW -> StatusLight.Status.WARNING;
                    case OUT -> StatusLight.Status.ERROR;
                });
                setGraphic(light);
            }
        });
        // Double-click or Enter opens the item in the form.
        table.setRowFactory(t -> {
            TableRow<Item> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 && !row.isEmpty()) {
                    context.edit(row.getItem());
                }
            });
            return row;
        });
        table.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                editSelected();
            } else if (e.getCode() == KeyCode.DELETE) {
                deleteSelected();
            }
        });

        loadingOverlay.visibleProperty().bind(context.loadingProperty());
        countLabel.textProperty().bind(Bindings.createStringBinding(
                () -> filtered.size() == context.items().size()
                        ? context.items().size() + " items"
                        : filtered.size() + " of " + context.items().size() + " items",
                filtered, context.items()));
    }

    @Override
    public void shown() {
        table.requestFocus();
    }

    private void updateFilter() {
        String text = search.getText() == null ? "" : search.getText().trim().toLowerCase(Locale.ROOT);
        String category = categories.getSelectedToggle() == null ? null : (String) categories.getSelectedToggle().getUserData();
        boolean low = lowFilter.isSelected();
        boolean out = outFilter.isSelected();
        filtered.setPredicate(item -> {
            if (!text.isEmpty() && !(item.getName() + " " + item.getSku() + " " + item.getLocation())
                    .toLowerCase(Locale.ROOT).contains(text)) {
                return false;
            }
            if (category != null && !category.equals(item.getCategory())) {
                return false;
            }
            // The two stock chips combine: either level matches when both are selected.
            return (!low && !out) || (low && item.getStock() == Item.Stock.LOW) || (out && item.getStock() == Item.Stock.OUT);
        });
    }

    @FXML
    private void editSelected() {
        Item item = table.getSelectionModel().getSelectedItem();
        if (item != null) {
            context.edit(item);
        }
    }

    @FXML
    private void duplicateSelected() {
        Item item = table.getSelectionModel().getSelectedItem();
        if (item != null) {
            Item copy = item.copy();
            copy.setName(item.getName() + " (copy)");
            context.items().add(context.items().indexOf(item) + 1, copy);
            table.getSelectionModel().select(copy);
            Snackbar.make(table, "Duplicated " + item.getName()).show();
        }
    }

    @FXML
    private void deleteSelected() {
        Item item = table.getSelectionModel().getSelectedItem();
        if (item == null) {
            return;
        }
        MaterialDialog.confirm(table, "Delete " + item.getName() + "?",
                        "It is removed from the inventory. You can undo this right after.", "Delete", "Cancel")
                .thenAccept(confirmed -> {
                    if (!confirmed) {
                        return;
                    }
                    int index = context.items().indexOf(item);
                    context.items().remove(item);
                    Snackbar.make(table, "Deleted " + item.getName())
                            .action("Undo", () -> context.items().add(Math.min(index, context.items().size()), item))
                            .show();
                });
    }
}
