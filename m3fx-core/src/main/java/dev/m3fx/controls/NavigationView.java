package dev.m3fx.controls;

import dev.m3fx.Motion;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;

/**
 * Base of the navigation components: an observable list of {@link NavigationItem}s and the
 * selected one. Subclasses decide how items are laid out.
 */
public abstract class NavigationView extends Region {

    private final ObservableList<NavigationItem> items = FXCollections.observableArrayList();
    private final ObjectProperty<NavigationItem> selectedItem = new SimpleObjectProperty<>(this, "selectedItem");
    private final ObjectProperty<EventHandler<ActionEvent>> onSelect = new SimpleObjectProperty<>(this, "onSelect");
    private final List<ItemNode> itemNodes = new ArrayList<>();

    protected NavigationView() {
        items.addListener((ListChangeListener<NavigationItem>) c -> rebuild());
        selectedItem.addListener((obs, old, item) -> {
            for (ItemNode node : itemNodes) {
                node.setSelected(node.item == item, true);
            }
            EventHandler<ActionEvent> handler = getOnSelect();
            if (handler != null) {
                handler.handle(new ActionEvent(this, this));
            }
        });
    }

    /** Creates the node for one item; called whenever the item list changes. */
    protected abstract Node createNode(NavigationItem item);

    /** Replaces the item nodes. */
    protected abstract void setItemNodes(List<Node> nodes);

    private void rebuild() {
        itemNodes.clear();
        List<Node> nodes = new ArrayList<>();
        for (NavigationItem item : items) {
            Node node = createNode(item);
            if (node != null) {
                nodes.add(node);
            }
        }
        setItemNodes(nodes);
        if (getSelectedItem() == null || !items.contains(getSelectedItem())) {
            items.stream().filter(NavigationItem::isDestination).findFirst().ifPresent(this::setSelectedItem);
        }
    }

    /**
     * Item node with an active indicator behind the icon. {@code indicatorWraps} makes the
     * indicator the background of the whole node (drawer) instead of just the icon (bar, rail).
     */
    protected class ItemNode extends StackPane {
        final NavigationItem item;
        final StackPane indicator = new StackPane();
        final Icon icon = new Icon(null, 24);
        final Label label = new Label();
        final Badge badge = new Badge();
        final StateLayer stateLayer;
        private final Timeline animation = new Timeline();

        protected ItemNode(NavigationItem item, Region stateLayerOwner) {
            this.item = item;
            itemNodes.add(this);
            getStyleClass().add("navigation-item");
            indicator.getStyleClass().add("navigation-indicator");
            label.getStyleClass().add("navigation-label");
            setFocusTraversable(true);
            stateLayer = new StateLayer(stateLayerOwner == null ? indicator : stateLayerOwner);
            stateLayer.setInteractionSource(this);
            label.textProperty().bind(item.textProperty());
            disableProperty().bind(item.disabledProperty());

            InvalidationListener iconListener = o -> updateIcon();
            item.iconProperty().addListener(iconListener);
            item.selectedIconProperty().addListener(iconListener);
            InvalidationListener badgeListener = o -> updateBadge();
            item.badgeCountProperty().addListener(badgeListener);
            item.badgeTextProperty().addListener(badgeListener);
            updateBadge();

            hoverProperty().addListener(o -> stateLayer.setForceHover(isHover()));
            addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    setSelectedItem(item);
                    requestFocus();
                }
            });
            addEventHandler(KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                    setSelectedItem(item);
                    e.consume();
                }
            });
            setSelected(getSelectedItem() == item, false);
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            Region owner = stateLayer.getParent() == indicator ? indicator : this;
            stateLayer.resizeRelocate(0, 0, owner.getWidth(), owner.getHeight());
        }

        void setSelected(boolean selected, boolean animate) {
            pseudoClassStateChanged(PseudoClasses.SELECTED, selected);
            updateIcon();
            animation.stop();
            if (selected && animate && Motion.shouldAnimate(this)) {
                indicator.setScaleX(0.4);
                animation.getKeyFrames().setAll(new KeyFrame(Motion.MEDIUM_2,
                        new KeyValue(indicator.scaleXProperty(), 1, Motion.EMPHASIZED_DECELERATE)));
                animation.playFromStart();
            } else {
                indicator.setScaleX(1);
            }
        }

        private void updateIcon() {
            boolean selected = getSelectedItem() == item;
            if (selected && item.getSelectedIcon() != null) {
                icon.setName(item.getSelectedIcon());
                icon.setFilled(false);
            } else {
                icon.setName(item.getIcon());
                icon.setFilled(selected);
            }
        }

        private void updateBadge() {
            String text = item.getBadgeText();
            int count = item.getBadgeCount();
            badge.setVisible(text != null || count >= 0);
            if (text != null) {
                badge.setText(text);
            } else {
                badge.setCount(count > 0 ? count : -1);
            }
        }
    }

    public ObservableList<NavigationItem> getItems() {
        return items;
    }

    public final ObjectProperty<NavigationItem> selectedItemProperty() {
        return selectedItem;
    }

    public final NavigationItem getSelectedItem() {
        return selectedItem.get();
    }

    public final void setSelectedItem(NavigationItem value) {
        selectedItem.set(value);
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
}
