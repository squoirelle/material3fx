package dev.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import java.util.List;

/**
 * Material segmented button: 2 to 5 connected toggle buttons for single or multi selection.
 * Selected segments show a check mark in place of their icon.
 *
 * <pre>{@code
 * SegmentedButton view = new SegmentedButton("Day", "Week", "Month");
 * view.getSegments().get(1).setSelected(true);
 * }</pre>
 */
public class SegmentedButton extends HBox {

    private static final String ORIGINAL_GRAPHIC = "m3fx.segment.graphic";

    private final ToggleGroup group = new ToggleGroup();
    private final BooleanProperty multiSelect = new SimpleBooleanProperty(this, "multiSelect");
    private final BooleanProperty showCheck = new SimpleBooleanProperty(this, "showCheck", true);

    public SegmentedButton(String... labels) {
        getStyleClass().add("segmented-button");
        setFillHeight(true);
        getChildren().addListener((ListChangeListener<Node>) c -> {
            while (c.next()) {
                for (Node added : c.getAddedSubList()) {
                    if (added instanceof ToggleButton button) {
                        setup(button);
                    }
                }
                for (Node removed : c.getRemoved()) {
                    if (removed instanceof ToggleButton button) {
                        button.setToggleGroup(null);
                    }
                }
            }
            updatePositions();
        });
        // Single select segmented buttons always keep one segment selected.
        group.selectedToggleProperty().addListener((obs, old, toggle) -> {
            if (toggle == null && old != null && !isMultiSelect()) {
                old.setSelected(true);
            }
        });
        multiSelect.addListener((obs, old, multi) -> getSegments().forEach(b -> b.setToggleGroup(multi ? null : group)));
        showCheck.addListener(o -> getSegments().forEach(this::updateGraphic));
        for (String label : labels) {
            getChildren().add(new ToggleButton(label));
        }
    }

    /** Adds a segment with a label and an icon. */
    public ToggleButton addSegment(String text, String icon) {
        ToggleButton button = new ToggleButton(text, icon == null ? null : new Icon(icon, 18));
        getChildren().add(button);
        return button;
    }

    /** The segments; equivalent to the toggle buttons among the children. */
    public List<ToggleButton> getSegments() {
        return getChildren().stream()
                .filter(ToggleButton.class::isInstance)
                .map(ToggleButton.class::cast)
                .toList();
    }

    public ToggleGroup getToggleGroup() {
        return group;
    }

    private void setup(ToggleButton button) {
        button.getStyleClass().add("segment");
        button.setMaxHeight(Double.MAX_VALUE);
        if (!isMultiSelect()) {
            button.setToggleGroup(group);
        }
        if (!button.getProperties().containsKey(ORIGINAL_GRAPHIC)) {
            button.getProperties().put(ORIGINAL_GRAPHIC, button.getGraphic() == null ? "" : button.getGraphic());
            button.selectedProperty().addListener(o -> updateGraphic(button));
        }
        updateGraphic(button);
    }

    private void updateGraphic(ToggleButton button) {
        Object original = button.getProperties().get(ORIGINAL_GRAPHIC);
        if (button.isSelected() && isShowCheck()) {
            button.setGraphic(new Icon("check", 18));
        } else {
            button.setGraphic(original instanceof Node node ? node : null);
        }
    }

    private void updatePositions() {
        List<ToggleButton> segments = getSegments();
        for (int i = 0; i < segments.size(); i++) {
            ObservableList<String> classes = segments.get(i).getStyleClass();
            classes.removeAll("first", "last", "only");
            if (segments.size() == 1) {
                classes.add("only");
            } else if (i == 0) {
                classes.add("first");
            } else if (i == segments.size() - 1) {
                classes.add("last");
            }
        }
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
