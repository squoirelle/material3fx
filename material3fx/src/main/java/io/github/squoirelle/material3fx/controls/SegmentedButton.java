package io.github.squoirelle.material3fx.controls;

import io.github.squoirelle.material3fx.skins.SegmentedButtonSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

/**
 * Material segmented button: 2 to 5 connected {@link ToggleButton}s for single or multi
 * selection. Selected segments show a check mark in place of their icon.
 * <p>
 * Single selection (the default) uses the {@link #toggleGroupProperty() toggle group}: arrow keys
 * move the selection like in any JavaFX toggle group, and one segment always stays selected. With
 * {@link #multiSelectProperty() multi select} the buttons are independent toggles.
 *
 * <pre>{@code
 * SegmentedButton view = new SegmentedButton("Day", "Week", "Month");
 * view.getButtons().get(1).setSelected(true);
 * }</pre>
 */
public class SegmentedButton extends Control {

    private final ObservableList<ToggleButton> buttons = FXCollections.observableArrayList();
    private final ObjectProperty<ToggleGroup> toggleGroup = new SimpleObjectProperty<>(this, "toggleGroup", new ToggleGroup());
    private final BooleanProperty multiSelect = new SimpleBooleanProperty(this, "multiSelect");
    private final BooleanProperty showCheck = new SimpleBooleanProperty(this, "showCheck", true);

    /** Single select segmented buttons always keep one segment selected. */
    private final javafx.beans.value.ChangeListener<javafx.scene.control.Toggle> keepSelected = (obs, old, toggle) -> {
        if (toggle == null && old != null && !isMultiSelect()) {
            old.setSelected(true);
        }
    };

    public SegmentedButton(String... labels) {
        getStyleClass().add("segmented-button");
        setFocusTraversable(false);
        buttons.addListener((ListChangeListener<ToggleButton>) c -> {
            while (c.next()) {
                c.getRemoved().forEach(b -> b.setToggleGroup(null));
                c.getAddedSubList().forEach(this::assignGroup);
            }
        });
        toggleGroup.addListener((obs, old, group) -> {
            if (old != null) {
                old.selectedToggleProperty().removeListener(keepSelected);
            }
            if (group != null) {
                group.selectedToggleProperty().addListener(keepSelected);
            }
            buttons.forEach(this::assignGroup);
        });
        toggleGroup.get().selectedToggleProperty().addListener(keepSelected);
        multiSelect.addListener(o -> buttons.forEach(this::assignGroup));
        for (String label : labels) {
            buttons.add(new ToggleButton(label));
        }
    }

    private void assignGroup(ToggleButton button) {
        button.setToggleGroup(isMultiSelect() ? null : getToggleGroup());
    }

    /** Adds a segment with a label and an optional icon. */
    public ToggleButton addSegment(String text, String icon) {
        ToggleButton button = new ToggleButton(text, icon == null ? null : new Icon(icon, 18));
        buttons.add(button);
        return button;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SegmentedButtonSkin(this);
    }

    /** The segments. */
    public ObservableList<ToggleButton> getButtons() {
        return buttons;
    }

    public final ObjectProperty<ToggleGroup> toggleGroupProperty() {
        return toggleGroup;
    }

    public final ToggleGroup getToggleGroup() {
        return toggleGroup.get();
    }

    public final void setToggleGroup(ToggleGroup value) {
        toggleGroup.set(value);
    }

    public final BooleanProperty multiSelectProperty() {
        return multiSelect;
    }

    public final boolean isMultiSelect() {
        return multiSelect.get();
    }

    public final void setMultiSelect(boolean value) {
        multiSelect.set(value);
    }

    /** Whether selected segments show a check mark (default true). */
    public final BooleanProperty showCheckProperty() {
        return showCheck;
    }

    public final boolean isShowCheck() {
        return showCheck.get();
    }

    public final void setShowCheck(boolean value) {
        showCheck.set(value);
    }
}
