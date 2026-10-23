package io.github.squoirelle.material3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.SingleSelectionModel;

/**
 * Base of the navigation components ({@link NavigationBar}, {@link NavigationRail},
 * {@link NavigationDrawer}): a list of {@link NavigationItem}s and a
 * {@link SingleSelectionModel}, like {@link javafx.scene.control.TabPane}. Only destinations can be
 * selected; headlines and dividers are skipped. The first destination is selected by default.
 * <p>
 * Keyboard: arrow keys move focus between destinations, Home / End jump to the first / last one,
 * Enter or Space selects the focused one.
 */
public abstract class NavigationView extends Control {

    private final ObservableList<NavigationItem> items = FXCollections.observableArrayList();
    private final ObjectProperty<SingleSelectionModel<NavigationItem>> selectionModel =
            new SimpleObjectProperty<>(this, "selectionModel");
    private final ObjectProperty<EventHandler<ActionEvent>> onSelect = new SimpleObjectProperty<>(this, "onSelect");

    protected NavigationView() {
        setFocusTraversable(false);
        setSelectionModel(new NavigationSelectionModel(this));
        items.addListener((ListChangeListener<NavigationItem>) c -> {
            SingleSelectionModel<NavigationItem> model = getSelectionModel();
            NavigationItem selected = model.getSelectedItem();
            if (selected != null && items.contains(selected)) {
                // Keep the same item selected when others are inserted or removed before it.
                model.select(selected);
            } else {
                model.clearSelection();
                items.stream().filter(NavigationItem::isDestination).findFirst().ifPresent(model::select);
            }
        });
    }

    /** The selection model for the items, created with the control. */
    public final ObjectProperty<SingleSelectionModel<NavigationItem>> selectionModelProperty() {
        return selectionModel;
    }

    public final SingleSelectionModel<NavigationItem> getSelectionModel() {
        return selectionModel.get();
    }

    public final void setSelectionModel(SingleSelectionModel<NavigationItem> value) {
        SingleSelectionModel<NavigationItem> old = selectionModel.get();
        if (old != null) {
            old.selectedItemProperty().removeListener(selectionListener);
        }
        selectionModel.set(value);
        if (value != null) {
            value.selectedItemProperty().addListener(selectionListener);
        }
    }

    private final javafx.beans.value.ChangeListener<NavigationItem> selectionListener = (obs, old, item) -> {
        EventHandler<ActionEvent> handler = getOnSelect();
        if (handler != null && item != null) {
            handler.handle(new ActionEvent(this, this));
        }
    };

    public ObservableList<NavigationItem> getItems() {
        return items;
    }

    /** Shortcut for {@code getSelectionModel().selectedItemProperty()}. */
    public final ReadOnlyObjectProperty<NavigationItem> selectedItemProperty() {
        return getSelectionModel().selectedItemProperty();
    }

    public final NavigationItem getSelectedItem() {
        return getSelectionModel().getSelectedItem();
    }

    /** Shortcut for {@code getSelectionModel().select(item)}. */
    public final void setSelectedItem(NavigationItem item) {
        getSelectionModel().select(item);
    }

    /** Called after the selected item changed. */
    public final ObjectProperty<EventHandler<ActionEvent>> onSelectProperty() {
        return onSelect;
    }

    public final EventHandler<ActionEvent> getOnSelect() {
        return onSelect.get();
    }

    public final void setOnSelect(EventHandler<ActionEvent> value) {
        onSelect.set(value);
    }

    /** Single selection over the destinations of a navigation view. */
    static final class NavigationSelectionModel extends SingleSelectionModel<NavigationItem> {
        private final NavigationView view;

        NavigationSelectionModel(NavigationView view) {
            this.view = view;
        }

        @Override
        protected NavigationItem getModelItem(int index) {
            return index >= 0 && index < view.items.size() ? view.items.get(index) : null;
        }

        @Override
        protected int getItemCount() {
            return view.items.size();
        }

        @Override
        public void select(int index) {
            NavigationItem item = getModelItem(index);
            if (item != null && item.isDestination() && !item.isDisabled()) {
                super.select(index);
            }
        }

        @Override
        public void select(NavigationItem item) {
            if (item != null && item.isDestination() && !item.isDisabled()) {
                super.select(item);
            }
        }

        @Override
        public void selectPrevious() {
            for (int i = getSelectedIndex() - 1; i >= 0; i--) {
                if (getModelItem(i).isDestination() && !getModelItem(i).isDisabled()) {
                    select(i);
                    return;
                }
            }
        }

        @Override
        public void selectNext() {
            for (int i = getSelectedIndex() + 1; i < getItemCount(); i++) {
                if (getModelItem(i).isDestination() && !getModelItem(i).isDisabled()) {
                    select(i);
                    return;
                }
            }
        }
    }
}
