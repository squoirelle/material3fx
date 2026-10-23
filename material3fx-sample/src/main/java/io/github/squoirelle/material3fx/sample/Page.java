package io.github.squoirelle.material3fx.sample;

/** The application's pages, each an FXML file with its controller. */
public enum Page {
    DASHBOARD("Dashboard", "dashboard.fxml"),
    INVENTORY("Inventory", "inventory.fxml"),
    ITEM_FORM("New item", "item-form.fxml"),
    SETTINGS("Settings", "settings.fxml");

    private final String title;
    private final String fxml;

    Page(String title, String fxml) {
        this.title = title;
        this.fxml = fxml;
    }

    public String title() {
        return title;
    }

    public String fxml() {
        return fxml;
    }
}
