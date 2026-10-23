package io.github.squoirelle.material3fx.sample;

import io.github.squoirelle.material3fx.Density;
import io.github.squoirelle.material3fx.MaterialTheme;
import io.github.squoirelle.material3fx.ThemeMode;
import io.github.squoirelle.material3fx.controls.AdaptiveScaffold;
import io.github.squoirelle.material3fx.controls.MaterialDialog;
import io.github.squoirelle.material3fx.controls.NavigationItem;
import io.github.squoirelle.material3fx.controls.TopAppBar;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;

import java.util.EnumMap;
import java.util.Map;

/** The main window: navigation between pages, menus, and theme settings from the View menu. */
public class MainController {

    private final AppContext context;
    private final Map<Page, Node> pages = new EnumMap<>(Page.class);
    private final Map<Page, Object> controllers = new EnumMap<>(Page.class);
    private final Map<NavigationItem, Page> pageOf = new java.util.HashMap<>();

    @FXML private AdaptiveScaffold scaffold;
    @FXML private TopAppBar topBar;
    @FXML private StackPane pageHost;
    @FXML private NavigationItem dashboardNav;
    @FXML private NavigationItem inventoryNav;
    @FXML private NavigationItem itemFormNav;
    @FXML private NavigationItem settingsNav;
    @FXML private ToggleGroup modeGroup;
    @FXML private ToggleGroup densityGroup;

    public MainController(AppContext context) {
        this.context = context;
    }

    @FXML
    private void initialize() {
        // The menu button only shows when the window is narrow enough for a modal drawer.
        topBar.setNavigationIcon(scaffold.getNavigationButton());
        pageOf.put(dashboardNav, Page.DASHBOARD);
        pageOf.put(inventoryNav, Page.INVENTORY);
        pageOf.put(itemFormNav, Page.ITEM_FORM);
        pageOf.put(settingsNav, Page.SETTINGS);
        scaffold.selectedItemProperty().addListener((obs, old, item) -> {
            Page page = pageOf.get(item);
            if (page != null) {
                show(page);
            }
        });
        context.setNavigator(this::navigate);

        // The inventory badge counts the items that need reordering.
        context.items().addListener((ListChangeListener<Item>) c -> updateBadge());
        context.editedItemProperty().addListener((obs, old, item) -> itemFormNav.setText(item == null ? "New item" : "Edit item"));

        MaterialTheme theme = context.theme();
        syncToggles(modeGroup, theme.modeProperty(), ThemeMode::valueOf);
        syncToggles(densityGroup, theme.densityProperty(), Density::valueOf);

        show(Page.DASHBOARD);
    }

    /** Keeps a group of radio menu items (userData = enum constant name) and a theme property in sync. */
    private static <E extends Enum<E>> void syncToggles(ToggleGroup group, ObjectProperty<E> property,
                                                         java.util.function.Function<String, E> parse) {
        Runnable select = () -> group.getToggles().stream()
                .filter(t -> property.get().name().equals(t.getUserData()))
                .findFirst().ifPresent(t -> t.setSelected(true));
        select.run();
        property.addListener(o -> select.run());
        group.selectedToggleProperty().addListener((obs, old, toggle) -> {
            if (toggle != null) {
                property.set(parse.apply((String) toggle.getUserData()));
            } else if (old != null) {
                old.setSelected(true);   // radio items: one stays selected
            }
        });
    }

    private void updateBadge() {
        long count = context.items().stream().filter(i -> i.getStock() != Item.Stock.IN_STOCK).count();
        inventoryNav.setBadgeCount(count == 0 ? -1 : (int) count);
    }

    private void navigate(Page page) {
        NavigationItem item = pageOf.entrySet().stream().filter(e -> e.getValue() == page).findFirst().orElseThrow().getKey();
        if (scaffold.getSelectedItem() == item) {
            show(page);
        } else {
            scaffold.setSelectedItem(item);   // shows the page through the selection listener
        }
    }

    private void show(Page page) {
        Node node = pages.computeIfAbsent(page, p -> context.load(p.fxml(), c -> controllers.put(p, c)));
        pageHost.getChildren().setAll(node);
        topBar.setTitle(page == Page.ITEM_FORM ? itemFormNav.getText() : page.title());
        if (controllers.get(page) instanceof PageController controller) {
            controller.shown();
        }
    }

    @FXML
    private void newItem() {
        context.editedItemProperty().set(null);
        navigate(Page.ITEM_FORM);
    }

    @FXML
    private void refresh() {
        context.refresh();
    }

    @FXML
    private void goDashboard() {
        navigate(Page.DASHBOARD);
    }

    @FXML
    private void goInventory() {
        navigate(Page.INVENTORY);
    }

    @FXML
    private void goSettings() {
        navigate(Page.SETTINGS);
    }

    @FXML
    private void quit() {
        Platform.exit();
    }

    @FXML
    private void about() {
        MaterialDialog.alert(scaffold, "Stockroom",
                "A sample inventory application for Material3FX. Every screen is FXML; the data is generated.");
    }
}
