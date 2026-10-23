package io.github.squoirelle.material3fx.sample;

import io.github.squoirelle.material3fx.controls.Banner;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;

/** Dashboard: inventory figures, a low stock banner, capacity and services. */
public class DashboardController {

    private final AppContext context;

    @FXML private Label subtitle;
    @FXML private Button refreshButton;
    @FXML private Banner lowStockBanner;
    @FXML private VBox totalBox;
    @FXML private VBox unitsBox;
    @FXML private VBox lowBox;
    @FXML private Label totalLabel;
    @FXML private Label unitsLabel;
    @FXML private Label lowLabel;
    @FXML private ProgressIndicator totalLoading;
    @FXML private ProgressIndicator unitsLoading;
    @FXML private ProgressIndicator lowLoading;
    @FXML private ProgressBar capacityBar;
    @FXML private Label capacityLabel;

    public DashboardController(AppContext context) {
        this.context = context;
    }

    @FXML
    private void initialize() {
        var items = context.items();
        var loading = context.loadingProperty();
        IntegerBinding units = Bindings.createIntegerBinding(() -> items.stream().mapToInt(Item::getQuantity).sum(), items);
        IntegerBinding toReorder = Bindings.createIntegerBinding(
                () -> (int) items.stream().filter(i -> i.getStock() != Item.Stock.IN_STOCK).count(), items);

        totalLabel.textProperty().bind(Bindings.size(items).asString());
        unitsLabel.textProperty().bind(units.asString("%,d"));
        lowLabel.textProperty().bind(toReorder.asString());
        // While loading, each figure gives way to a progress indicator.
        for (var box : new VBox[] {totalBox, unitsBox, lowBox}) {
            box.visibleProperty().bind(loading.not());
        }
        for (var indicator : new ProgressIndicator[] {totalLoading, unitsLoading, lowLoading}) {
            indicator.visibleProperty().bind(loading);
        }
        refreshButton.disableProperty().bind(loading);
        subtitle.textProperty().bind(Bindings.when(loading)
                .then("Loading the inventory...")
                .otherwise(Bindings.format("%d items across %d locations", Bindings.size(items), InventoryService.LOCATIONS.size())));

        lowStockBanner.textProperty().bind(Bindings.format("%d items are low or out of stock.", toReorder));
        lowStockBanner.showingProperty().bind(loading.not().and(toReorder.greaterThan(0)));

        // A fixed warehouse size: the progress bar shows how full it is.
        int capacity = 6000;
        capacityBar.progressProperty().bind(Bindings.createDoubleBinding(() -> Math.min(1, units.get() / (double) capacity), units));
        capacityLabel.textProperty().bind(Bindings.format("%,d of %,d slots used", units, capacity));
    }

    @FXML
    private void refresh() {
        context.refresh();
    }

    @FXML
    private void reviewLowStock() {
        context.navigate(Page.INVENTORY);
    }
}
