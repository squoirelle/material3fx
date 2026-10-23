package dev.m3fx.demo;

import dev.m3fx.MaterialTheme;
import dev.m3fx.ThemeMode;
import dev.m3fx.controls.AdaptiveScaffold;
import dev.m3fx.controls.IconButton;
import dev.m3fx.controls.MaterialDialog;
import dev.m3fx.controls.NavigationItem;
import dev.m3fx.controls.SideSheet;
import dev.m3fx.controls.TopAppBar;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * The m3fx showcase: every component on its own page, with a live theme editor. The app itself is
 * an {@link AdaptiveScaffold}: a standard drawer on large windows, a modal drawer behind a menu
 * button below. Run with {@code ./gradlew :m3fx-demo:run}.
 */
public class DemoApp extends Application {

    private final MaterialTheme theme = new MaterialTheme(MaterialTheme.BASELINE);
    private final Map<NavigationItem, Gallery.Page> pages = new HashMap<>();
    private final Map<Gallery.Page, Node> contents = new HashMap<>();
    private final ScrollPane scroll = new ScrollPane();
    private final TopAppBar topBar = new TopAppBar();
    private final AdaptiveScaffold scaffold = new AdaptiveScaffold();
    private SideSheet themeSheet;

    @Override
    public void start(Stage stage) {
        theme.getCustomColors().put("success", Color.web("#2E7D32"));
        theme.getCustomColors().put("warning", Color.web("#F9A825"));
        theme.applyGlobally();

        scaffold.setNavigationTitle("m3fx");
        for (Gallery.Page page : Gallery.pages()) {
            switch (page.id()) {
                case "theme" -> scaffold.getNavigationItems().add(NavigationItem.headline("Theme"));
                case "standard-buttons" -> scaffold.getNavigationItems().add(NavigationItem.headline("Standard JavaFX controls"));
                case "buttons" -> scaffold.getNavigationItems().add(NavigationItem.headline("m3fx components"));
                default -> { }
            }
            NavigationItem item = new NavigationItem(page.title(), page.icon());
            pages.put(item, page);
            scaffold.getNavigationItems().add(item);
        }
        scaffold.getNavigationDrawer().setPrefWidth(300);
        scaffold.selectedItemProperty().addListener((obs, old, item) -> show(item));

        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scaffold.setContent(scroll);
        topBar.bindScroll(scroll);
        topBar.setNavigationIcon(scaffold.getNavigationButton());
        IconButton darkMode = topBar.addAction("dark_mode", "Toggle dark mode");
        darkMode.setOnAction(e -> theme.setMode(theme.isDark() ? ThemeMode.LIGHT : ThemeMode.DARK));
        theme.darkProperty().addListener((obs, old, dark) -> darkMode.setIconName(dark ? "light_mode" : "dark_mode"));
        IconButton palette = topBar.addAction("palette", "Theme");
        palette.setOnAction(e -> openThemeSheet());
        topBar.addAction("info", "About").setOnAction(e -> MaterialDialog.alert(scaffold,
                "m3fx", "Material Design 3 for JavaFX. Every page uses the same theme: change it from the palette button."));
        scaffold.setTopBar(topBar);

        Scene scene = new Scene(scaffold, 1360, 900);
        show(scaffold.getSelectedItem());
        stage.setTitle("m3fx - Material Design 3 for JavaFX");
        // Themed before setScene: the window may already be showing (e.g. in the screenshot runner).
        theme.apply(scene);
        stage.setScene(scene);
        stage.show();
    }

    private void show(NavigationItem item) {
        Gallery.Page page = pages.get(item);
        if (page == null) {
            return;
        }
        topBar.setTitle(page.title());
        scroll.setContent(contents.computeIfAbsent(page, p -> p.content().get()));
        scroll.setVvalue(0);
    }

    // Hooks for the screenshot renderer.

    MaterialTheme theme() {
        return theme;
    }

    AdaptiveScaffold scaffold() {
        return scaffold;
    }

    void selectPage(String id) {
        pages.forEach((item, page) -> {
            if (page.id().equals(id)) {
                scaffold.setSelectedItem(item);
            }
        });
    }

    void openThemeSheet() {
        if (themeSheet == null) {
            themeSheet = ThemeEditor.create(theme);
        }
        themeSheet.show(scaffold);
    }

    void openDrawer() {
        scaffold.getNavigationDrawer().showModal(scaffold);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
