package io.github.squoirelle.material3fx.sample;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;

/** An inventory item. */
public class Item {

    /** Stock level, derived from the quantity and the reorder level. */
    public enum Stock {
        IN_STOCK("In stock"), LOW("Low stock"), OUT("Out of stock");

        private final String label;

        Stock(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final StringProperty sku = new SimpleStringProperty(this, "sku");
    private final StringProperty category = new SimpleStringProperty(this, "category");
    private final StringProperty location = new SimpleStringProperty(this, "location");
    private final StringProperty unit = new SimpleStringProperty(this, "unit", "Pieces");
    private final IntegerProperty quantity = new SimpleIntegerProperty(this, "quantity");
    private final IntegerProperty reorderLevel = new SimpleIntegerProperty(this, "reorderLevel", 10);
    private final ObjectProperty<LocalDate> received = new SimpleObjectProperty<>(this, "received");
    // A read-only property rather than a binding: table columns read it with PropertyValueFactory.
    private final ReadOnlyObjectWrapper<Stock> stock = new ReadOnlyObjectWrapper<>(this, "stock");

    public Item(String name, String sku, String category, String location, int quantity, int reorderLevel) {
        setName(name);
        setSku(sku);
        setCategory(category);
        setLocation(location);
        setQuantity(quantity);
        setReorderLevel(reorderLevel);
        stock.bind(Bindings.createObjectBinding(
                () -> getQuantity() == 0 ? Stock.OUT : getQuantity() < getReorderLevel() ? Stock.LOW : Stock.IN_STOCK,
                this.quantity, this.reorderLevel));
    }

    public Item copy() {
        Item copy = new Item(getName(), getSku(), getCategory(), getLocation(), getQuantity(), getReorderLevel());
        copy.setUnit(getUnit());
        copy.setReceived(getReceived());
        return copy;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String value) {
        name.set(value);
    }

    public StringProperty skuProperty() {
        return sku;
    }

    public String getSku() {
        return sku.get();
    }

    public void setSku(String value) {
        sku.set(value);
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public String getCategory() {
        return category.get();
    }

    public void setCategory(String value) {
        category.set(value);
    }

    public StringProperty locationProperty() {
        return location;
    }

    public String getLocation() {
        return location.get();
    }

    public void setLocation(String value) {
        location.set(value);
    }

    public StringProperty unitProperty() {
        return unit;
    }

    public String getUnit() {
        return unit.get();
    }

    public void setUnit(String value) {
        unit.set(value);
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int value) {
        quantity.set(value);
    }

    public IntegerProperty reorderLevelProperty() {
        return reorderLevel;
    }

    public int getReorderLevel() {
        return reorderLevel.get();
    }

    public void setReorderLevel(int value) {
        reorderLevel.set(value);
    }

    public ObjectProperty<LocalDate> receivedProperty() {
        return received;
    }

    public LocalDate getReceived() {
        return received.get();
    }

    public void setReceived(LocalDate value) {
        received.set(value);
    }

    public ReadOnlyObjectProperty<Stock> stockProperty() {
        return stock.getReadOnlyProperty();
    }

    public Stock getStock() {
        return stock.get();
    }
}
