package io.github.squoirelle.material3fx.demo;

import io.github.squoirelle.material3fx.controls.Avatar;
import io.github.squoirelle.material3fx.controls.BottomAppBar;
import io.github.squoirelle.material3fx.controls.Fab;
import io.github.squoirelle.material3fx.controls.IconButton;
import io.github.squoirelle.material3fx.controls.NavigationBar;
import io.github.squoirelle.material3fx.controls.NavigationDrawer;
import io.github.squoirelle.material3fx.controls.NavigationItem;
import io.github.squoirelle.material3fx.controls.NavigationRail;
import io.github.squoirelle.material3fx.controls.TopAppBar;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/** Navigation bar, rail, drawer, top and bottom app bars. */
final class NavigationPage {

    private NavigationPage() {
    }

    static List<NavigationItem> destinations() {
        NavigationItem map = new NavigationItem("Map", "map");
        NavigationItem units = new NavigationItem("Units", "groups");
        units.setBadgeCount(3);
        NavigationItem alerts = new NavigationItem("Alerts", "notifications");
        alerts.setBadgeCount(0);
        NavigationItem reports = new NavigationItem("Reports", "description");
        return List.of(map, units, alerts, reports);
    }

    static Node page() {
        NavigationBar bar = new NavigationBar();
        bar.getItems().setAll(destinations());
        bar.setSelectedItem(bar.getItems().get(1));
        bar.setPrefWidth(420);

        NavigationRail rail = new NavigationRail();
        rail.getItems().setAll(destinations());
        IconButton menu = new IconButton("menu");
        Fab fab = new Fab("edit");
        fab.setColor(Fab.Color.TERTIARY);
        VBox railHeader = new VBox(8, menu, fab);
        railHeader.setAlignment(Pos.TOP_CENTER);
        rail.setHeader(railHeader);
        rail.setPrefHeight(440);
        rail.getStyleClass().add("surface-container-low");

        NavigationDrawer drawer = new NavigationDrawer();
        drawer.setTitle("Operations");
        NavigationItem inbox = new NavigationItem("Inbox", "inbox");
        inbox.setBadgeText("24");
        NavigationItem outbox = new NavigationItem("Outbox", "send");
        NavigationItem favorites = new NavigationItem("Favorites", "favorite");
        NavigationItem trash = new NavigationItem("Trash", "delete");
        drawer.getItems().setAll(inbox, outbox, favorites, trash, NavigationItem.divider(),
                NavigationItem.headline("Labels"), new NavigationItem("Family", "label"), new NavigationItem("Work", "label"));
        drawer.setPrefHeight(440);

        TopAppBar small = new TopAppBar("Small");
        small.setNavigationIcon("arrow_back");
        small.addAction("attach_file", "Attach");
        small.addAction("event", "Event");
        small.addAction("more_vert", "More");

        TopAppBar center = new TopAppBar("Center aligned");
        center.setVariant(TopAppBar.Variant.CENTER_ALIGNED);
        center.setNavigationIcon("menu");
        Avatar avatar = new Avatar("Grace Hopper");
        avatar.setSize(32);
        center.getActions().add(avatar);

        TopAppBar medium = new TopAppBar("Medium top app bar");
        medium.setVariant(TopAppBar.Variant.MEDIUM);
        medium.setNavigationIcon("arrow_back");
        medium.addAction("more_vert", "More");
        medium.setScrolled(true);

        TopAppBar large = new TopAppBar("Large top app bar");
        large.setVariant(TopAppBar.Variant.LARGE);
        large.setNavigationIcon("arrow_back");
        large.addAction("more_vert", "More");

        BottomAppBar bottom = new BottomAppBar();
        bottom.addAction("check_box", "Tasks");
        bottom.addAction("edit", "Edit");
        bottom.addAction("mic", "Voice");
        bottom.addAction("image", "Image");
        bottom.setFab(new Fab("add"));

        VBox bars = new VBox(16, small, center, medium, large, bottom);
        bars.setPrefWidth(560);

        return Gallery.page("Navigation", "Navigation bar, rail and drawer share the same NavigationItem model.",
                Gallery.section("Navigation rail and drawer", new HBox(24, rail, drawer)),
                Gallery.section("Navigation bar", bar),
                Gallery.section("Top and bottom app bars", bars));
    }
}
