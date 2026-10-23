package io.github.squoirelle.material3fx.sample;

import io.github.squoirelle.material3fx.MaterialTheme;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * What the controllers share: the theme, the inventory and its loading state, and navigation.
 * Controllers receive it through their constructor (see {@link #load(String)}).
 */
public class AppContext {

    private final MaterialTheme theme;
    private final InventoryService service;
    private final ObservableList<Item> items = FXCollections.observableArrayList();
    private final ReadOnlyBooleanWrapper loading = new ReadOnlyBooleanWrapper(this, "loading");
    private final ObjectProperty<Item> editedItem = new SimpleObjectProperty<>(this, "editedItem");
    private Consumer<Page> navigator = page -> { };
    private Task<List<Item>> pendingLoad;

    public AppContext(MaterialTheme theme, InventoryService service) {
        this.theme = theme;
        this.service = service;
    }

    public MaterialTheme theme() {
        return theme;
    }

    public InventoryService service() {
        return service;
    }

    public ObservableList<Item> items() {
        return items;
    }

    /** True while the inventory is being (re)loaded. */
    public ReadOnlyBooleanProperty loadingProperty() {
        return loading.getReadOnlyProperty();
    }

    /** Reloads the inventory in the background. */
    public void refresh() {
        if (pendingLoad != null) {
            pendingLoad.cancel();
        }
        Task<List<Item>> task = service.loadItems();
        pendingLoad = task;
        loading.set(true);
        task.setOnSucceeded(e -> {
            items.setAll(task.getValue());
            loading.set(false);
        });
        task.setOnFailed(e -> loading.set(false));
    }

    /** The item opened in the item form, or null for a new item. */
    public ObjectProperty<Item> editedItemProperty() {
        return editedItem;
    }

    public void edit(Item item) {
        editedItem.set(item);
        navigate(Page.ITEM_FORM);
    }

    public void navigate(Page page) {
        navigator.accept(page);
    }

    void setNavigator(Consumer<Page> navigator) {
        this.navigator = navigator;
    }

    /**
     * Loads an FXML file of this package. Controllers with an {@code AppContext} constructor get
     * this context.
     */
    public <T> T load(String fxml, Consumer<Object> controller) {
        FXMLLoader loader = new FXMLLoader(AppContext.class.getResource(fxml));
        loader.setControllerFactory(type -> {
            try {
                try {
                    return type.getConstructor(AppContext.class).newInstance(this);
                } catch (NoSuchMethodException e) {
                    return type.getConstructor().newInstance();
                }
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Cannot create controller " + type.getName(), e);
            }
        });
        try {
            T root = loader.load();
            controller.accept(loader.getController());
            return root;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load " + fxml, e);
        }
    }

    public <T> T load(String fxml) {
        return load(fxml, c -> { });
    }
}
