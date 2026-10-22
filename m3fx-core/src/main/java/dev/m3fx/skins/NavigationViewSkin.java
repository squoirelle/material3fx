package dev.m3fx.skins;

import dev.m3fx.Motion;
import dev.m3fx.controls.Badge;
import dev.m3fx.controls.Icon;
import dev.m3fx.controls.NavigationItem;
import dev.m3fx.controls.NavigationView;
import dev.m3fx.controls.StateLayer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;

/**
 * Base skin of the navigation components: builds one {@link ItemNode} per destination (subclasses
 * arrange them) and handles the keyboard: arrows move focus, Home / End jump, Enter / Space select.
 */
public abstract class NavigationViewSkin<C extends NavigationView> extends SkinBase<C> {

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    private final List<ItemNode> itemNodes = new ArrayList<>();
    private final ListChangeListener<NavigationItem> itemsListener = c -> rebuild();
    private final EventHandler<KeyEvent> keyHandler = this::onKeyPressed;
    private final InvalidationListener selectionListener = o -> updateSelection(true);

    protected NavigationViewSkin(C control) {
        super(control);
        control.getItems().addListener(itemsListener);
        control.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        control.getSelectionModel().selectedItemProperty().addListener(selectionListener);
        registerChangeListener(control.selectionModelProperty(), o -> {
            control.getSelectionModel().selectedItemProperty().addListener(selectionListener);
            updateSelection(false);
        });
    }

    /** Creates the node for one item; destinations should be {@link ItemNode}s. Null to skip. */
    protected abstract Node createNode(NavigationItem item);

    /** Replaces the item nodes in the skin's layout. */
    protected abstract void setItemNodes(List<Node> nodes);

    /** Rebuilds all item nodes from the control's items. Subclasses call it once constructed. */
    protected final void rebuild() {
        itemNodes.forEach(ItemNode::dispose);
        itemNodes.clear();
        List<Node> nodes = new ArrayList<>();
        for (NavigationItem item : getSkinnable().getItems()) {
            Node node = createNode(item);
            if (node instanceof ItemNode itemNode) {
                itemNodes.add(itemNode);
            }
            if (node != null) {
                nodes.add(node);
            }
        }
        setItemNodes(nodes);
        updateSelection(false);
    }

    private void updateSelection(boolean animate) {
        NavigationItem selected = getSkinnable().getSelectedItem();
        for (ItemNode node : itemNodes) {
            node.setSelected(node.item == selected, animate);
        }
    }

    private void onKeyPressed(KeyEvent e) {
        List<ItemNode> enabled = itemNodes.stream().filter(n -> !n.isDisabled()).toList();
        // The scene's focus owner, not isFocused(): that one also needs the window to have OS focus.
        Node owner = getSkinnable().getScene() == null ? null : getSkinnable().getScene().getFocusOwner();
        int focused = enabled.indexOf(owner);
        if (focused < 0) {
            return;
        }
        int target = switch (e.getCode()) {
            case UP, LEFT, KP_UP, KP_LEFT -> focused - 1;
            case DOWN, RIGHT, KP_DOWN, KP_RIGHT -> focused + 1;
            case HOME -> 0;
            case END -> enabled.size() - 1;
            default -> -2;
        };
        if (target == -2) {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                getSkinnable().getSelectionModel().select(enabled.get(focused).item);
                e.consume();
            }
            return;
        }
        if (target >= 0 && target < enabled.size()) {
            enabled.get(target).requestFocus();
        }
        e.consume();
    }

    @Override
    public void dispose() {
        getSkinnable().getItems().removeListener(itemsListener);
        getSkinnable().removeEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        getSkinnable().getSelectionModel().selectedItemProperty().removeListener(selectionListener);
        itemNodes.forEach(ItemNode::dispose);
        super.dispose();
    }

    /**
     * One destination: an active indicator (with the icon and badge) and a label. Subclasses
     * arrange the parts; the indicator gets the state layer unless the whole node is the indicator.
     */
    protected static class ItemNode extends StackPane {
        protected final NavigationView view;
        protected final NavigationItem item;
        protected final StackPane indicator = new StackPane();
        protected final Icon icon = new Icon(null, 24);
        protected final Label label = new Label();
        protected final Badge badge = new Badge();
        protected final StateLayer stateLayer;
        private final Timeline animation = new Timeline();
        private final InvalidationListener iconListener = o -> updateIcon();
        private final InvalidationListener badgeListener = o -> updateBadge();

        protected ItemNode(NavigationView view, NavigationItem item) {
            this.view = view;
            this.item = item;
            getStyleClass().add("navigation-item");
            indicator.getStyleClass().add("navigation-indicator");
            label.getStyleClass().add("navigation-label");
            icon.setFollowTextFill(false);
            setFocusTraversable(true);
            setAccessibleRole(AccessibleRole.TAB_ITEM);
            accessibleTextProperty().bind(item.textProperty());
            stateLayer = new StateLayer(indicator);
            stateLayer.setInteractionSource(this);
            label.textProperty().bind(item.textProperty());
            disableProperty().bind(item.disabledProperty());
            item.iconProperty().addListener(iconListener);
            item.selectedIconProperty().addListener(iconListener);
            item.badgeCountProperty().addListener(badgeListener);
            item.badgeTextProperty().addListener(badgeListener);
            updateBadge();
            updateIcon();
            hoverProperty().addListener(o -> stateLayer.setForceHover(isHover()));
            focusVisibleProperty().addListener(o -> stateLayer.setForceHover(isFocusVisible() || isHover()));
            addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    view.getSelectionModel().select(item);
                    requestFocus();
                }
            });
        }

        /** The state layer sized to its owner, which is the indicator or, for drawers, this node. */
        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            Region owner = stateLayer.getParent() == indicator ? indicator : this;
            stateLayer.resizeRelocate(0, 0, owner.getWidth(), owner.getHeight());
        }

        void setSelected(boolean selected, boolean animate) {
            pseudoClassStateChanged(SELECTED, selected);
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
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

        private boolean isSelected() {
            return view.getSelectedItem() == item;
        }

        private void updateIcon() {
            boolean selected = isSelected();
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

        @Override
        public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
            if (attribute == AccessibleAttribute.SELECTED) {
                return isSelected();
            }
            return super.queryAccessibleAttribute(attribute, parameters);
        }

        void dispose() {
            item.iconProperty().removeListener(iconListener);
            item.selectedIconProperty().removeListener(iconListener);
            item.badgeCountProperty().removeListener(badgeListener);
            item.badgeTextProperty().removeListener(badgeListener);
            accessibleTextProperty().unbind();
            label.textProperty().unbind();
            disableProperty().unbind();
        }
    }
}
