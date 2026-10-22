package dev.m3fx.controls;

import dev.m3fx.Density;
import dev.m3fx.FxTestSupport;
import dev.m3fx.MaterialTheme;
import dev.m3fx.Motion;
import dev.m3fx.ThemeMode;
import dev.m3fx.color.ColorRole;
import dev.m3fx.color.Variant;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Builds every component in a themed, shown scene: CSS, skins and layout must all work. */
class ComponentsSmokeTest {

    @BeforeAll
    static void setUp() throws InterruptedException {
        FxTestSupport.start();
        Motion.setEnabled(false);
    }

    private static List<Node> allComponents() {
        NavigationBar bar = new NavigationBar();
        bar.getItems().addAll(new NavigationItem("A", "home"), new NavigationItem("B", "search"));
        NavigationRail rail = new NavigationRail();
        rail.getItems().addAll(new NavigationItem("A", "home"), new NavigationItem("B", "search"));
        NavigationDrawer drawer = new NavigationDrawer();
        drawer.getItems().addAll(new NavigationItem("A", "home"), NavigationItem.divider(), NavigationItem.headline("H"));
        TopAppBar topBar = new TopAppBar("Title");
        topBar.setVariant(TopAppBar.Variant.LARGE);
        BottomAppBar bottomBar = new BottomAppBar();
        bottomBar.setFab(new Fab("add"));
        ListView<String> list = new ListView<>(FXCollections.observableArrayList("a", "b"));
        list.setCellFactory(ListItem.cellFactory(ListItem::new));
        MaterialDialog dialog = new MaterialDialog("Title", "Text");
        dialog.addAction("OK");
        Carousel carousel = new Carousel();
        carousel.getItems().addAll(new StackPane(), new StackPane());
        return List.of(
                new Button("Button"), Buttons.tonal("Tonal", "add"), new IconButton("menu", IconButton.Variant.FILLED),
                new Fab("add", "Extended"), new SegmentedButton("A", "B"), new Chip("Chip", Chip.Variant.FILTER),
                new Card(Card.Variant.ELEVATED), new Switch("Switch"), Badge.attach(new Icon("mail"), Badge.count(3)),
                new Avatar("Ada Lovelace"), new Divider(), bar, rail, drawer, topBar, bottomBar,
                new ListItem("Headline", "Supporting"), list, new MaterialTextField("Label"),
                MaterialTextField.password("Password"), new SearchBar(), new TimePicker(LocalTime.NOON), carousel,
                dialog, new SideSheet("Sheet"), new BottomSheet(new VBox()), new CheckBox("Check"),
                new Slider(), new ProgressBar(-1), new ProgressIndicator(0.5), new TableView<String>());
    }

    @Test
    void everyComponentRendersInEveryThemeConfiguration() throws Exception {
        FxTestSupport.onFx(() -> {
            MaterialTheme theme = new MaterialTheme(Color.web("#006A6A"));
            for (ThemeMode mode : List.of(ThemeMode.LIGHT, ThemeMode.DARK)) {
                for (Density density : Density.values()) {
                    theme.setMode(mode);
                    theme.setDensity(density);
                    theme.setVariant(Variant.values()[density.ordinal() % Variant.values().length]);
                    VBox root = new VBox(8);
                    root.getChildren().setAll(allComponents());
                    Scene scene = new Scene(new StackPane(root), 1200, 8000);
                    theme.apply(scene);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.show();
                    scene.getRoot().applyCss();
                    scene.getRoot().layout();
                    for (Node node : root.getChildren()) {
                        assertTrue(node.getLayoutBounds().getHeight() > 0 || node instanceof Divider,
                                () -> node.getClass().getSimpleName() + " has no height");
                    }
                    stage.close();
                }
            }
            return null;
        });
    }

    @Test
    void stylesheetSkinsAreInstalled() throws Exception {
        FxTestSupport.onFx(() -> {
            Button button = new Button("x");
            CheckBox check = new CheckBox("x");
            ProgressIndicator progress = new ProgressIndicator(-1);
            Scene scene = new Scene(new VBox(button, check, progress));
            new MaterialTheme().apply(scene);
            scene.getRoot().applyCss();
            assertInstanceOf(dev.m3fx.skins.MaterialButtonSkin.class, button.getSkin());
            assertInstanceOf(dev.m3fx.skins.MaterialCheckBoxSkin.class, check.getSkin());
            assertInstanceOf(dev.m3fx.skins.CircularProgressSkin.class, progress.getSkin());
            return null;
        });
    }

    @Test
    void themeChangesUpdateTheScene() throws Exception {
        FxTestSupport.onFx(() -> {
            Region box = new Region();
            box.setStyle("-fx-background-color: -md-sys-color-primary;");
            Scene scene = new Scene(new StackPane(box), 100, 100);
            MaterialTheme theme = new MaterialTheme();
            theme.apply(scene);
            scene.getRoot().applyCss();
            Color light = (Color) box.getBackground().getFills().getFirst().getFill();
            assertEquals(theme.color(ColorRole.PRIMARY), light);
            assertTrue(scene.getRoot().getStyleClass().contains("md-light"));

            theme.setMode(ThemeMode.DARK);
            scene.getRoot().applyCss();
            Color dark = (Color) box.getBackground().getFills().getFirst().getFill();
            assertNotEquals(light, dark);
            assertEquals(theme.color(ColorRole.PRIMARY), dark);
            assertTrue(scene.getRoot().getStyleClass().contains("md-dark"));
            assertEquals(theme, MaterialTheme.of(scene));

            theme.remove(scene);
            assertFalse(scene.getRoot().getStyleClass().contains("md-dark"));
            assertNull(MaterialTheme.of(scene));
            return null;
        });
    }

    @Test
    void tooltipResolvesThemeColors() throws Exception {
        FxTestSupport.onFx(() -> {
            Button owner = new Button("x");
            Scene scene = new Scene(new StackPane(owner), 200, 200);
            MaterialTheme theme = new MaterialTheme();
            theme.applyGlobally();
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
            javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip("tip");
            tooltip.show(owner, 10, 10);
            tooltip.getScene().getRoot().applyCss();
            javafx.scene.control.Label label = tooltip.getScene().getRoot().lookupAll("*").stream()
                    .filter(javafx.scene.control.Label.class::isInstance)
                    .map(javafx.scene.control.Label.class::cast)
                    .findFirst().orElseThrow();
            assertEquals(theme.color(ColorRole.INVERSE_ON_SURFACE), label.getTextFill());
            tooltip.hide();
            stage.close();
            return null;
        });
    }

    @Test
    void dialogCompletesWithChosenAction() throws Exception {
        String result = FxTestSupport.onFx(() -> {
            StackPane root = new StackPane();
            Scene scene = new Scene(root, 400, 300);
            new MaterialTheme().apply(scene);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
            MaterialDialog dialog = new MaterialDialog("Delete?", null);
            dialog.addAction("Cancel");
            Button delete = dialog.addPrimaryAction("Delete");
            var future = dialog.show(root);
            assertTrue(root.getChildren().size() > 0 && root.lookup(".material-dialog") != null);
            delete.fire();
            stage.close();
            return future.getNow("not completed");
        });
        assertEquals("Delete", result);
    }

    @Test
    void textFieldValidation() throws Exception {
        FxTestSupport.onFx(() -> {
            MaterialTextField field = new MaterialTextField("Code");
            field.setMaxLength(4);
            field.setValidator(t -> t.length() < 2 ? "Too short" : null);
            field.setText("a");
            assertFalse(field.validate());
            assertTrue(field.isError());
            assertEquals("Too short", field.errorMessageProperty().get());
            field.setText("abc");
            assertTrue(field.validate());
            assertFalse(field.isError());
            field.getEditor().setText("abcdef");
            assertEquals("abc", field.getText(), "max length must reject longer input");
            return null;
        });
    }

    @Test
    void navigationSelectionIsShared() throws Exception {
        FxTestSupport.onFx(() -> {
            NavigationItem a = new NavigationItem("A", "home");
            NavigationItem b = new NavigationItem("B", "search");
            NavigationBar bar = new NavigationBar();
            bar.getItems().addAll(a, b);
            assertEquals(a, bar.getSelectedItem(), "first destination selected by default");
            bar.setSelectedItem(b);
            assertEquals(b, bar.getSelectedItem());
            return null;
        });
    }
}
