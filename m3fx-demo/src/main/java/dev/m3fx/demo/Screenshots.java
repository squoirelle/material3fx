package dev.m3fx.demo;

import dev.m3fx.Density;
import dev.m3fx.Motion;
import dev.m3fx.color.Variant;
import dev.m3fx.controls.BottomSheet;
import dev.m3fx.controls.DatePickerDialog;
import dev.m3fx.controls.ListItem;
import dev.m3fx.controls.MaterialDialog;
import dev.m3fx.controls.Scaffold;
import dev.m3fx.controls.Snackbar;
import dev.m3fx.controls.TimePicker;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import dev.m3fx.MaterialTheme;
import dev.m3fx.ThemeMode;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Renders every demo page, light and dark, to PNG files. Used to review the visuals without
 * clicking through the app: {@code ./gradlew :m3fx-demo:screenshots}.
 */
public class Screenshots extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        List<String> args = getParameters().getRaw();
        File dir = new File(args.isEmpty() ? "screenshots" : args.getFirst());
        String only = args.size() > 1 ? args.get(1) : null;
        dir.mkdirs();
        Motion.setEnabled(false);
        MaterialTheme theme = new MaterialTheme();
        // Same custom colors as the demo app (used by the color scheme page).
        theme.getCustomColors().put("success", Color.web("#2E7D32"));
        theme.getCustomColors().put("warning", Color.web("#F9A825"));
        for (ThemeMode mode : new ThemeMode[] {ThemeMode.LIGHT, ThemeMode.DARK}) {
            theme.setMode(mode);
            for (Gallery.Page page : Gallery.pages()) {
                if (only != null && (only.equals("app") || !page.id().contains(only))) {
                    continue;
                }
                Parent content = Gallery.wrapForScreenshot(page);
                Scene scene = new Scene(content, 1200, page.screenshotHeight());
                theme.apply(scene);
                stage.setScene(scene);
                stage.show();
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                WritableImage image = scene.snapshot(null);
                File file = new File(dir, page.id() + "-" + mode.name().toLowerCase() + ".png");
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                System.out.println("wrote " + file);
            }
        }
        if (only == null || only.equals("app")) {
            appScreenshots(stage, dir);
        }
        Platform.exit();
    }

    /** The full demo app, with real overlays opened programmatically. */
    private void appScreenshots(Stage stage, File dir) throws IOException {
        DemoApp app = new DemoApp();
        app.start(stage);
        Scene scene = stage.getScene();
        dev.m3fx.controls.AdaptiveScaffold scaffold = app.scaffold();
        MaterialTheme theme = app.theme();

        shoot(scene, dir, "app-light");
        theme.setMode(ThemeMode.DARK);
        shoot(scene, dir, "app-dark");
        theme.setMode(ThemeMode.LIGHT);

        app.openThemeSheet();
        shoot(scene, dir, "app-theme-sheet");
        closeOverlays(scaffold);

        MaterialDialog<javafx.scene.control.ButtonType> dialog = new MaterialDialog<>("Reset settings?",
                "This will reset your app preferences back to their default settings.");
        dialog.setIcon("restart_alt");
        dialog.getButtonTypes().addAll(javafx.scene.control.ButtonType.CANCEL,
                new javafx.scene.control.ButtonType("Accept", javafx.scene.control.ButtonBar.ButtonData.OK_DONE));
        dialog.show(scaffold);
        shoot(scene, dir, "app-dialog");
        dialog.close(null);

        TimePicker.showDialog(scaffold, LocalTime.of(9, 30));
        shoot(scene, dir, "app-time-dialog");
        closeOverlays(scaffold);

        DatePickerDialog.show(scaffold, LocalDate.of(2026, 10, 23));
        shoot(scene, dir, "app-date-dialog");
        closeOverlays(scaffold);

        app.selectPage("lists");
        BottomSheet sheet = new BottomSheet(new VBox(new ListItem("Share"), new ListItem("Get link"), new ListItem("Delete")));
        sheet.show(scaffold);
        Snackbar.make(scaffold, "Waypoint deleted").action("Undo", () -> { }).duration(Duration.INDEFINITE).show();
        shoot(scene, dir, "app-sheet-snackbar");
        closeOverlays(scaffold);

        theme.setSeedColor(Color.web("#006A6A"));
        theme.setVariant(Variant.VIBRANT);
        theme.setMode(ThemeMode.DARK);
        app.selectPage("buttons");
        shoot(scene, dir, "app-teal-vibrant-dark");

        theme.setSeedColor(MaterialTheme.BASELINE);
        theme.setVariant(Variant.TONAL_SPOT);
        theme.setMode(ThemeMode.LIGHT);
        theme.setDensity(Density.DENSE);
        app.selectPage("standard-data");
        shoot(scene, dir, "app-dense-data");
        theme.setDensity(Density.DEFAULT);

        theme.setMode(ThemeMode.LIGHT);
        app.selectPage("feedback");
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        javafx.scene.control.MenuButton menu = (javafx.scene.control.MenuButton) scene.getRoot().lookup(".menu-button");
        menu.show();
        shootPopup(dir, "popup-menu-sections");
        menu.hide();
        javafx.scene.control.ColorPicker picker = (javafx.scene.control.ColorPicker) scene.getRoot().lookup(".color-picker");
        picker.show();
        shootPopup(dir, "popup-color-picker");
        picker.hide();
        app.selectPage("data-entry");
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        dev.m3fx.controls.SplitButton split = (dev.m3fx.controls.SplitButton) scene.getRoot().lookup(".split-button");
        split.show();
        shootPopup(dir, "popup-split-button");
        split.hide();

        stage.setWidth(800);
        stage.setHeight(700);
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        app.openDrawer();
        shoot(scene, dir, "app-narrow-modal-drawer");
    }

    /** Snapshot of the most recently opened popup window (menus, color palette). */
    private static void shootPopup(File dir, String name) throws IOException {
        javafx.stage.Window popup = null;
        for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
            if (w instanceof javafx.stage.PopupWindow && w.isShowing()) {
                popup = w;
            }
        }
        if (popup == null) {
            System.out.println("no popup for " + name);
            return;
        }
        popup.getScene().getRoot().applyCss();
        popup.getScene().getRoot().layout();
        WritableImage image = popup.getScene().snapshot(null);
        File file = new File(dir, name + ".png");
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        System.out.println("wrote " + file);
    }

    private static void closeOverlays(Scaffold scaffold) {
        scaffold.getChildren().removeIf(n -> n.getStyleClass().contains("md-overlay"));
        scaffold.getProperties().clear();
    }

    private static void shoot(Scene scene, File dir, String name) throws IOException {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        WritableImage image = scene.snapshot(null);
        File file = new File(dir, name + ".png");
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        System.out.println("wrote " + file);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
