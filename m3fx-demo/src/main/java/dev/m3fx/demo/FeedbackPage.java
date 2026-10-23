package dev.m3fx.demo;

import dev.m3fx.controls.Banner;
import dev.m3fx.controls.Buttons;
import dev.m3fx.controls.Icon;
import dev.m3fx.controls.MenuHeader;
import dev.m3fx.controls.StatusLight;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/** Banners, status lights, menus with section headers, color picker. */
final class FeedbackPage {

    private FeedbackPage() {
    }

    static Node page() {
        Banner info = new Banner("A new map layer set is available for this region.", "layers");
        info.addAction("Later", () -> info.setShowing(false));
        info.addAction("Update", () -> info.setShowing(false));
        Banner warning = new Banner("Connection to the relay is degraded. Positions may be delayed.", "wifi_tethering_error");
        warning.setSeverity(Banner.Severity.WARNING);
        warning.addAction("Details", () -> { });
        Banner error = new Banner("Mission plan rejected: two waypoints are inside a restricted area.", "error");
        error.setSeverity(Banner.Severity.ERROR);
        error.addAction("Review", () -> { });
        Banner success = new Banner("All units synchronized.", "check_circle");
        success.setSeverity(Banner.Severity.SUCCESS);
        success.addAction("Dismiss", () -> success.setShowing(false));
        Button restore = Buttons.text("Show the banners again", "refresh");
        restore.setOnAction(e -> {
            info.setShowing(true);
            success.setShowing(true);
        });
        VBox banners = new VBox(info, warning, error, success);

        StatusLight online = new StatusLight(StatusLight.Status.SUCCESS, "Datalink online");
        StatusLight degraded = new StatusLight(StatusLight.Status.WARNING, "GPS degraded");
        StatusLight down = new StatusLight(StatusLight.Status.ERROR, "Radar offline");
        down.setPulsing(true);
        StatusLight syncing = new StatusLight(StatusLight.Status.INFO, "Syncing");
        syncing.setPulsing(true);
        StatusLight idle = new StatusLight(StatusLight.Status.NEUTRAL, "Standby");

        MenuButton menu = new MenuButton("Menu with sections");
        menu.getItems().addAll(new MenuHeader("Edit"), new MenuItem("Cut", new Icon("content_cut", 20)),
                new MenuItem("Copy", new Icon("content_copy", 20)), new MenuItem("Paste", new Icon("content_paste", 20)),
                new SeparatorMenuItem(), new MenuHeader("Share"), new MenuItem("Email", new Icon("mail", 20)),
                new MenuItem("Copy link", new Icon("link", 20)));
        Button contextTarget = Buttons.outlined("Right-click me", "mouse");
        ContextMenu context = new ContextMenu(new MenuHeader("Waypoint"), new MenuItem("Rename"), new MenuItem("Move"),
                new SeparatorMenuItem(), new MenuItem("Delete", new Icon("delete", 20)));
        contextTarget.setContextMenu(context);

        ColorPicker picker = new ColorPicker(Color.web("#6750A4"));

        return Gallery.page("Status and feedback", "Banners stay until dismissed; status lights show live states. "
                        + "Menus get section headers, and the color picker popup follows the theme.",
                Gallery.section("Banners", banners, restore),
                Gallery.section("Status lights", Gallery.row(online, degraded, down, syncing, idle)),
                Gallery.section("Menus and color picker", Gallery.row(menu, contextTarget, picker)));
    }
}
