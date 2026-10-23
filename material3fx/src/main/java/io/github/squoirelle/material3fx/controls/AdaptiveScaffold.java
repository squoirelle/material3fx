package io.github.squoirelle.material3fx.controls;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

/**
 * A {@link Scaffold} that adapts to the window width, following Material's window size classes.
 * <p>
 * <b>Navigation.</b> Give it {@link #getNavigationItems() destinations}; it shows them in the
 * component that fits the width, all sharing one selection:
 * <ul>
 *     <li>compact (&lt; 600): a {@link NavigationBar} at the bottom</li>
 *     <li>medium and expanded (600-1199): a {@link NavigationRail}</li>
 *     <li>large and extra-large (&ge; 1200): a standard {@link NavigationDrawer}</li>
 * </ul>
 * With more destinations than a bar (5) or rail (7) can hold, or when the items contain headlines,
 * smaller widths use a modal drawer opened by the {@link #getNavigationButton() navigation button}
 * (put it in the top app bar). {@link #navigationModeProperty() navigationMode} forces a mode.
 * <p>
 * <b>Panes.</b> For list-detail layouts set a {@link #listPaneProperty() list pane} and a
 * {@link #detailPaneProperty() detail pane}: from the expanded size class both are shown side by
 * side; below, one at a time, chosen by {@link #detailShownProperty() detailShown}. Otherwise just
 * use {@link #setContent(Node) content}.
 */
public class AdaptiveScaffold extends Scaffold {

    /** Material window size classes, by width. */
    public enum SizeClass {
        COMPACT(0), MEDIUM(600), EXPANDED(840), LARGE(1200), EXTRA_LARGE(1600);

        private final double minWidth;

        SizeClass(double minWidth) {
            this.minWidth = minWidth;
        }

        public double minWidth() {
            return minWidth;
        }

        public static SizeClass of(double width) {
            SizeClass result = COMPACT;
            for (SizeClass c : values()) {
                if (width >= c.minWidth) {
                    result = c;
                }
            }
            return result;
        }

        public boolean isAtLeast(SizeClass other) {
            return ordinal() >= other.ordinal();
        }
    }

    /** How destinations are presented. */
    public enum NavigationMode { AUTO, BAR, RAIL, DRAWER, MODAL_DRAWER, NONE }

    private static final int MAX_BAR_ITEMS = 5;
    private static final int MAX_RAIL_ITEMS = 7;

    private final ReadOnlyObjectWrapper<SizeClass> sizeClass = new ReadOnlyObjectWrapper<>(this, "sizeClass", SizeClass.COMPACT);
    private final ReadOnlyObjectWrapper<NavigationMode> effectiveNavigationMode =
            new ReadOnlyObjectWrapper<>(this, "effectiveNavigationMode", NavigationMode.NONE);
    private final ObjectProperty<NavigationMode> navigationMode = new SimpleObjectProperty<>(this, "navigationMode", NavigationMode.AUTO);
    private final ObservableList<NavigationItem> navigationItems = FXCollections.observableArrayList();
    private final ObjectProperty<NavigationItem> selectedItem = new SimpleObjectProperty<>(this, "selectedItem");
    private final StringProperty navigationTitle = new SimpleStringProperty(this, "navigationTitle");
    private final ObjectProperty<Node> listPane = new SimpleObjectProperty<>(this, "listPane");
    private final ObjectProperty<Node> detailPane = new SimpleObjectProperty<>(this, "detailPane");
    private final BooleanProperty detailShown = new SimpleBooleanProperty(this, "detailShown");
    private final DoubleProperty listPaneWidth = new SimpleDoubleProperty(this, "listPaneWidth", 360);
    private final ObjectProperty<Node> railHeader = new SimpleObjectProperty<>(this, "railHeader");

    private final NavigationBar bar = new NavigationBar();
    private final NavigationRail rail = new NavigationRail();
    private final NavigationDrawer drawer = new NavigationDrawer();
    private final IconButton navigationButton = new IconButton("menu");
    private final HBox twoPanes = new HBox();
    private final StackPane listSlot = new StackPane();
    private final StackPane detailSlot = new StackPane();
    private boolean syncing;
    private boolean ownContent;

    public AdaptiveScaffold() {
        getStyleClass().add("adaptive-scaffold");
        navigationButton.tooltip("Navigation");
        navigationButton.getStyleClass().add("adaptive-navigation-button");
        navigationButton.setOnAction(e -> drawer.showModal(this));
        twoPanes.getStyleClass().add("adaptive-panes");
        listSlot.getStyleClass().add("adaptive-list-pane");
        detailSlot.getStyleClass().add("adaptive-detail-pane");
        HBox.setHgrow(detailSlot, Priority.ALWAYS);
        drawer.titleProperty().bind(navigationTitle);
        rail.headerProperty().bind(railHeader);

        // One item list and one selection for the three navigation components.
        for (NavigationView view : new NavigationView[] {bar, rail, drawer}) {
            javafx.beans.binding.Bindings.bindContent(view.getItems(), navigationItems);
            view.selectedItemProperty().addListener((obs, old, item) -> {
                if (!syncing && item != null) {
                    selectedItem.set(item);
                }
            });
        }
        selectedItem.addListener((obs, old, item) -> {
            syncing = true;
            try {
                for (NavigationView view : new NavigationView[] {bar, rail, drawer}) {
                    view.getSelectionModel().select(item);
                }
            } finally {
                syncing = false;
            }
        });
        navigationItems.addListener((ListChangeListener<NavigationItem>) c -> {
            if (selectedItem.get() == null || !navigationItems.contains(selectedItem.get())) {
                selectedItem.set(drawer.getSelectedItem());
            }
            update();
        });

        InvalidationListener update = o -> update();
        widthProperty().addListener(o -> sizeClass.set(SizeClass.of(getWidth())));
        sizeClass.addListener(update);
        navigationMode.addListener(update);
        listPane.addListener(update);
        detailPane.addListener(update);
        detailShown.addListener(update);
        listPaneWidth.addListener(update);
        update();
    }

    private NavigationMode resolveMode() {
        if (navigationItems.isEmpty()) {
            return NavigationMode.NONE;
        }
        if (getNavigationMode() != NavigationMode.AUTO) {
            return getNavigationMode();
        }
        long destinations = navigationItems.stream().filter(NavigationItem::isDestination).count();
        boolean sections = navigationItems.stream().anyMatch(i -> !i.isDestination());
        SizeClass size = getSizeClass();
        if (size.isAtLeast(SizeClass.LARGE)) {
            return NavigationMode.DRAWER;
        }
        if (size.isAtLeast(SizeClass.MEDIUM)) {
            return destinations <= MAX_RAIL_ITEMS && !sections ? NavigationMode.RAIL : NavigationMode.MODAL_DRAWER;
        }
        return destinations <= MAX_BAR_ITEMS && !sections ? NavigationMode.BAR : NavigationMode.MODAL_DRAWER;
    }

    private void update() {
        NavigationMode mode = resolveMode();
        effectiveNavigationMode.set(mode);
        if (mode != NavigationMode.MODAL_DRAWER) {
            drawer.hideModal();
        }
        setNavigation(switch (mode) {
            case RAIL -> rail;
            case DRAWER -> drawer;
            default -> null;
        });
        setBottomBar(mode == NavigationMode.BAR ? bar : null);
        navigationButton.setVisible(mode == NavigationMode.MODAL_DRAWER);
        navigationButton.setManaged(mode == NavigationMode.MODAL_DRAWER);
        for (SizeClass c : SizeClass.values()) {
            pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass(c.name().toLowerCase().replace('_', '-')),
                    c == getSizeClass());
        }
        updatePanes();
    }

    private void updatePanes() {
        Node list = getListPane();
        Node detail = getDetailPane();
        if (list == null && detail == null) {
            if (ownContent) {
                setContent(null);
                ownContent = false;
            }
            return;
        }
        listSlot.getChildren().setAll(list == null ? java.util.List.of() : java.util.List.of(list));
        detailSlot.getChildren().setAll(detail == null ? java.util.List.of() : java.util.List.of(detail));
        boolean twoPane = getSizeClass().isAtLeast(SizeClass.EXPANDED) && list != null && detail != null;
        if (twoPane) {
            listSlot.setMinWidth(getListPaneWidth());
            listSlot.setPrefWidth(getListPaneWidth());
            listSlot.setMaxWidth(getListPaneWidth());
            twoPanes.getChildren().setAll(listSlot, detailSlot);
        } else {
            listSlot.setMinWidth(0);
            listSlot.setPrefWidth(-1);
            listSlot.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(listSlot, Priority.ALWAYS);
            boolean showDetail = list == null || (isDetailShown() && detail != null);
            twoPanes.getChildren().setAll(showDetail ? detailSlot : listSlot);
        }
        HBox.setHgrow(listSlot, twoPane ? Priority.NEVER : Priority.ALWAYS);
        pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("two-pane"), twoPane);
        if (getContent() != twoPanes) {
            setContent(twoPanes);
            ownContent = true;
        }
    }

    /** True when the list and detail panes are both visible. */
    public boolean isTwoPane() {
        return twoPanes.getChildren().size() == 2;
    }

    /** The destinations, shared by the bar, rail and drawer. */
    public ObservableList<NavigationItem> getNavigationItems() {
        return navigationItems;
    }

    /** The selected destination, in sync with whichever navigation component is shown. */
    public final ObjectProperty<NavigationItem> selectedItemProperty() {
        return selectedItem;
    }

    public final NavigationItem getSelectedItem() {
        return selectedItem.get();
    }

    public final void setSelectedItem(NavigationItem value) {
        selectedItem.set(value);
    }

    /**
     * Menu button opening the modal drawer; visible only in {@link NavigationMode#MODAL_DRAWER}.
     * Put it in the top app bar: {@code topBar.setNavigationIcon(scaffold.getNavigationButton())}.
     */
    public IconButton getNavigationButton() {
        return navigationButton;
    }

    /** The navigation bar used in compact windows (to customize it). */
    public NavigationBar getNavigationBar() {
        return bar;
    }

    /** The navigation rail used in medium and expanded windows. */
    public NavigationRail getNavigationRail() {
        return rail;
    }

    /** The navigation drawer used in large windows, and as the modal drawer. */
    public NavigationDrawer getNavigationDrawer() {
        return drawer;
    }

    public final ReadOnlyObjectProperty<SizeClass> sizeClassProperty() {
        return sizeClass.getReadOnlyProperty();
    }

    public final SizeClass getSizeClass() {
        return sizeClass.get();
    }

    /** The mode actually used (AUTO resolved). */
    public final ReadOnlyObjectProperty<NavigationMode> effectiveNavigationModeProperty() {
        return effectiveNavigationMode.getReadOnlyProperty();
    }

    public final NavigationMode getEffectiveNavigationMode() {
        return effectiveNavigationMode.get();
    }

    public final ObjectProperty<NavigationMode> navigationModeProperty() {
        return navigationMode;
    }

    public final NavigationMode getNavigationMode() {
        return navigationMode.get();
    }

    public final void setNavigationMode(NavigationMode value) {
        navigationMode.set(value);
    }

    /** Title shown at the top of the drawer. */
    public final StringProperty navigationTitleProperty() {
        return navigationTitle;
    }

    public final String getNavigationTitle() {
        return navigationTitle.get();
    }

    public final void setNavigationTitle(String value) {
        navigationTitle.set(value);
    }

    /** Header of the rail, typically a {@link Fab}. */
    public final ObjectProperty<Node> railHeaderProperty() {
        return railHeader;
    }

    public final Node getRailHeader() {
        return railHeader.get();
    }

    public final void setRailHeader(Node value) {
        railHeader.set(value);
    }

    public final ObjectProperty<Node> listPaneProperty() {
        return listPane;
    }

    public final Node getListPane() {
        return listPane.get();
    }

    public final void setListPane(Node value) {
        listPane.set(value);
    }

    public final ObjectProperty<Node> detailPaneProperty() {
        return detailPane;
    }

    public final Node getDetailPane() {
        return detailPane.get();
    }

    public final void setDetailPane(Node value) {
        detailPane.set(value);
    }

    /** In single-pane layouts, whether the detail pane is shown instead of the list. */
    public final BooleanProperty detailShownProperty() {
        return detailShown;
    }

    public final boolean isDetailShown() {
        return detailShown.get();
    }

    public final void setDetailShown(boolean value) {
        detailShown.set(value);
    }

    public final DoubleProperty listPaneWidthProperty() {
        return listPaneWidth;
    }

    public final double getListPaneWidth() {
        return listPaneWidth.get();
    }

    public final void setListPaneWidth(double value) {
        listPaneWidth.set(value);
    }
}
