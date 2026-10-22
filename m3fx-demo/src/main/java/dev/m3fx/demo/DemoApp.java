package dev.m3fx.demo;

import dev.m3fx.MaterialTheme;
import dev.m3fx.ThemeMode;
import dev.m3fx.controls.IconButton;
import dev.m3fx.controls.NavigationDrawer;
import dev.m3fx.controls.NavigationItem;
import dev.m3fx.controls.Scaffold;
import dev.m3fx.controls.SideSheet;
import dev.m3fx.controls.TopAppBar;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The m3fx showcase: every component on its own page, with a live theme editor.
 * Run with {@code ./gradlew :m3fx-demo:run}.
 */
public class DemoApp extends Application {

    private static final double WIDE = 1000;

    private final MaterialTheme theme = new MaterialTheme(MaterialTheme.BASELINE);
    private final Map<NavigationItem, Gallery.Page> pages = new HashMap<>();
    private final Map<Gallery.Page, Node> contents = new HashMap<>();
    private final ScrollPane scroll = new ScrollPane();
    private final TopAppBar topBar = new TopAppBar();
    private final NavigationDrawer drawer = new NavigationDrawer();
    private final Scaffold scaffold = new Scaffold(scroll);
    private SideSheet themeSheet;

    @Override
    public void start(Stage stage) {
        theme.getCustomColors().put("success", Color.web("#2E7D32"));
        theme.getCustomColors().put("warning", Color.web("#F9A825"));
        theme.applyGlobally();

        List<Gallery.Page> all = Gallery.pages();
        drawer.setTitle("m3fx");
        for (Gallery.Page page : all) {
            NavigationItem item = new NavigationItem(page.title(), page.icon());
            pages.put(item, page);
            drawer.getItems().add(item);
            if (page.id().equals("theme")) {
                drawer.getItems().add(drawer.getItems().size() - 1, NavigationItem.headline("Theme"));
            } else if (page.id().equals("standard-buttons")) {
                drawer.getItems().add(drawer.getItems().size() - 1, NavigationItem.headline("Standard JavaFX controls"));
            } else if (page.id().equals("buttons")) {
                drawer.getItems().add(drawer.getItems().size() - 1, NavigationItem.headline("m3fx components"));
            }
        }
        drawer.selectedItemProperty().addListener((obs, old, item) -> show(item));

        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        topBar.bindScroll(scroll);
        IconButton darkMode = topBar.addAction("dark_mode", "Toggle dark mode");
        darkMode.setOnAction(e -> theme.setMode(theme.isDark() ? ThemeMode.LIGHT : ThemeMode.DARK));
        theme.darkProperty().addListener((obs, old, dark) -> darkMode.setIconName(dark ? "light_mode" : "dark_mode"));
        IconButton palette = topBar.addAction("palette", "Theme");
        palette.setOnAction(e -> {
            if (themeSheet == null) {
                themeSheet = ThemeEditor.create(theme);
            }
            themeSheet.show(palette);
        });
        topBar.addAction("info", "About").setOnAction(e -> dev.m3fx.controls.MaterialDialog.alert(scaffold,
                "m3fx", "Material Design 3 for JavaFX. Every page uses the same theme: change it from the palette button."));

        scaffold.setTopBar(topBar);
        Scene scene = new Scene(scaffold, 1360, 900);
        scene.widthProperty().addListener((obs, old, width) -> updateLayout(width.doubleValue()));
        updateLayout(scene.getWidth());
        // The drawer selects its first destination as soon as items are added.
        show(drawer.getSelectedItem());

        stage.setTitle("m3fx - Material Design 3 for JavaFX");
        stage.setScene(scene);
        stage.show();
    }

    /** Standard drawer on wide windows; a menu button opening it as a modal drawer otherwise. */
    private void updateLayout(double width) {
        boolean wide = width >= WIDE;
        if (wide && scaffold.getNavigation() != drawer) {
            drawer.hideModal();
            topBar.setNavigationIcon((Node) null);
            scaffold.setNavigation(drawer);
            drawer.setPrefWidth(300);
        } else if (!wide && scaffold.getNavigation() == drawer) {
            scaffold.setNavigation(null);
            topBar.setNavigationIcon("menu").setOnAction(e -> drawer.showModal(scaffold));
        }
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

    Scaffold scaffold() {
        return scaffold;
    }

    void selectPage(String id) {
        pages.forEach((item, page) -> {
            if (page.id().equals(id)) {
                drawer.setSelectedItem(item);
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
        drawer.showModal(scaffold);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
