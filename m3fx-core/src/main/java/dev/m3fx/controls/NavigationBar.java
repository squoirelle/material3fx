package dev.m3fx.controls;

import dev.m3fx.skins.NavigationBarSkin;
import javafx.scene.control.Skin;

/**
 * Material navigation bar: 3 to 5 destinations at the bottom of compact windows.
 *
 * <pre>{@code
 * NavigationBar bar = new NavigationBar();
 * bar.getItems().addAll(new NavigationItem("Home", "home"), new NavigationItem("Search", "search"));
 * bar.selectedItemProperty().addListener((obs, old, item) -> showPage(item));
 * }</pre>
 */
public class NavigationBar extends NavigationView {

    public NavigationBar() {
        getStyleClass().add("navigation-bar");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new NavigationBarSkin(this);
    }
}
