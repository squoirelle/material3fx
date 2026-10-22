package dev.m3fx.controls;

import dev.m3fx.skins.NavigationDrawerSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Skin;

/**
 * Material navigation drawer. Use it inline in a layout (standard drawer), or show it over the
 * content with {@link #showModal(Node)} (modal drawer, with a scrim, closed by Escape, a click
 * outside or a selection).
 * <p>
 * Besides destinations, it renders {@link NavigationItem#headline(String) headlines} and
 * {@link NavigationItem#divider() dividers}; sections get a divider above their headline.
 */
public class NavigationDrawer extends NavigationView {

    private final StringProperty title = new SimpleStringProperty(this, "title");
    private final ObjectProperty<Node> header = new SimpleObjectProperty<>(this, "header");
    private Overlay modalOverlay;

    public NavigationDrawer() {
        getStyleClass().add("navigation-drawer");
        selectedItemProperty().addListener(o -> hideModal());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new NavigationDrawerSkin(this);
    }

    /** Shows this drawer as a modal drawer over the content of {@code owner}'s window. */
    public Overlay showModal(Node owner) {
        getStyleClass().add("modal");
        modalOverlay = Overlay.show(owner, this, Overlay.Position.START, true, true);
        modalOverlay.setOnHidden(() -> {
            getStyleClass().remove("modal");
            modalOverlay = null;
        });
        return modalOverlay;
    }

    /** Hides the modal drawer, if shown. */
    public void hideModal() {
        if (modalOverlay != null) {
            modalOverlay.hide();
        }
    }

    /** Whether the drawer is currently shown as a modal drawer. */
    public boolean isShowingModal() {
        return modalOverlay != null && modalOverlay.isShowing();
    }

    /** Optional title shown above the items (e.g. the app name). */
    public final StringProperty titleProperty() {
        return title;
    }

    public final String getTitle() {
        return title.get();
    }

    public final void setTitle(String value) {
        title.set(value);
    }

    /** Optional header node shown above the title. */
    public final ObjectProperty<Node> headerProperty() {
        return header;
    }

    public final Node getHeader() {
        return header.get();
    }

    public final void setHeader(Node value) {
        header.set(value);
    }
}
