package dev.m3fx.demo;

import dev.m3fx.controls.BottomSheet;
import dev.m3fx.controls.Buttons;
import dev.m3fx.controls.Chip;
import dev.m3fx.controls.DatePickerDialog;
import dev.m3fx.controls.ListItem;
import dev.m3fx.controls.MaterialDialog;
import dev.m3fx.controls.MaterialTextField;
import dev.m3fx.controls.NavigationDrawer;
import dev.m3fx.controls.SideSheet;
import dev.m3fx.controls.Snackbar;
import dev.m3fx.controls.TimePicker;
import dev.m3fx.controls.Tooltips;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;

/** Dialogs, snackbars, sheets, tooltips: interactive in the app, rendered inline for screenshots. */
final class OverlaysPage {

    private OverlaysPage() {
    }

    static Node page() {
        Button basic = Buttons.tonal("Basic dialog", "chat_bubble");
        basic.setOnAction(e -> {
            MaterialDialog dialog = new MaterialDialog("Reset settings?",
                    "This will reset your app preferences back to their default settings. The following accounts will also be signed out.");
            dialog.setIcon("restart_alt");
            dialog.addAction("Cancel");
            dialog.addPrimaryAction("Accept");
            dialog.show(basic).thenAccept(result -> Snackbar.show(basic, "Dialog result: " + result));
        });

        Button confirm = Buttons.tonal("Confirm", "help");
        confirm.setOnAction(e -> MaterialDialog.confirm(confirm, "Discard draft?", "Your changes will be lost.", "Discard", "Keep editing")
                .thenAccept(ok -> Snackbar.show(confirm, ok ? "Draft discarded" : "Kept editing")));

        Button form = Buttons.tonal("Dialog with form", "edit_note");
        form.setOnAction(e -> {
            MaterialTextField name = new MaterialTextField("Mission name");
            MaterialTextField code = new MaterialTextField("Code");
            MaterialDialog dialog = new MaterialDialog("New mission", null);
            dialog.setContent(new VBox(16, name, code));
            dialog.addAction("Cancel");
            dialog.addPrimaryAction("Create");
            dialog.show(form);
        });

        Button fullScreen = Buttons.tonal("Full-screen dialog", "open_in_full");
        fullScreen.setOnAction(e -> {
            MaterialDialog dialog = new MaterialDialog("New event", null);
            dialog.setFullScreen(true);
            dialog.setContent(new VBox(16, new MaterialTextField("Title"), new MaterialTextField("Location"), new CheckBox("All day")));
            dialog.addPrimaryAction("Save");
            dialog.show(fullScreen);
        });

        Button snackbar = Buttons.tonal("Snackbar", "info");
        snackbar.setOnAction(e -> Snackbar.make(snackbar, "Waypoint deleted").action("Undo", () -> Snackbar.show(snackbar, "Restored")).show());

        Button longSnackbar = Buttons.tonal("Snackbar, two lines + close", "info");
        longSnackbar.setOnAction(e -> Snackbar.make(longSnackbar,
                "Connection to the relay station was lost. Data will be synchronized when it is back.")
                .action("Retry", () -> { }).closeable().show());

        Button bottomSheet = Buttons.tonal("Bottom sheet", "vertical_align_top");
        bottomSheet.setOnAction(e -> new BottomSheet(sheetContent()).show(bottomSheet));

        Button sideSheet = Buttons.tonal("Side sheet", "side_navigation");
        sideSheet.setOnAction(e -> {
            SideSheet sheet = new SideSheet("Filters", filters());
            sheet.addAction("Apply", sheet::hide);
            sheet.addAction("Cancel", sheet::hide);
            sheet.show(sideSheet);
        });

        Button modalDrawer = Buttons.tonal("Modal drawer", "menu");
        modalDrawer.setOnAction(e -> {
            NavigationDrawer drawer = new NavigationDrawer();
            drawer.setTitle("m3fx demo");
            drawer.getItems().setAll(NavigationPage.destinations());
            drawer.showModal(modalDrawer);
        });

        Button date = Buttons.tonal("Date picker dialog", "calendar_month");
        date.setOnAction(e -> DatePickerDialog.show(date, LocalDate.now())
                .thenAccept(d -> Snackbar.show(date, d == null ? "Cancelled" : "Picked " + d)));

        Button time = Buttons.tonal("Time picker dialog", "schedule");
        time.setOnAction(e -> TimePicker.showDialog(time, LocalTime.of(9, 30))
                .thenAccept(t -> Snackbar.show(time, t == null ? "Cancelled" : "Picked " + t)));

        Button plainTip = Buttons.outlined("Plain tooltip");
        Tooltips.install(plainTip, "Saves the current plan");
        Button richTip = Buttons.outlined("Rich tooltip");
        Tooltips.installRich(richTip, "Clearance levels", "Level 3 grants access to tactical overlays and live unit positions.");

        FlowPane buttons = Gallery.row(basic, confirm, form, fullScreen, snackbar, longSnackbar, bottomSheet, sideSheet,
                modalDrawer, date, time);

        // Static renderings, so the components also show up in screenshots.
        MaterialDialog staticDialog = new MaterialDialog("Reset settings?",
                "This will reset your app preferences back to their default settings.");
        staticDialog.setIcon("restart_alt");
        staticDialog.addAction("Cancel");
        staticDialog.addPrimaryAction("Accept");
        Snackbar staticSnackbar = Snackbar.make(basic, "Waypoint deleted").action("Undo", () -> { });
        BottomSheet staticSheet = new BottomSheet(sheetContent());
        staticSheet.setPrefWidth(420);
        SideSheet staticSide = new SideSheet("Filters", filters());
        staticSide.addAction("Apply", () -> { });
        staticSide.addAction("Cancel", () -> { });
        staticSide.setPrefHeight(360);

        return Gallery.page("Dialogs, sheets, snackbars", "Overlays open inside the window, above a scrim. Escape or a click outside dismisses them.",
                Gallery.section("Open an overlay", buttons, Gallery.row(plainTip, richTip)),
                Gallery.section("Rendered inline", Gallery.row(staticDialog, staticSide),
                        Gallery.row(staticSheet, staticSnackbar)));
    }

    private static Node sheetContent() {
        VBox box = new VBox(
                item("Share", "share"), item("Get link", "link"), item("Edit name", "edit"), item("Delete", "delete"));
        box.setPadding(new Insets(0, 0, 8, 0));
        return box;
    }

    private static ListItem item(String text, String icon) {
        ListItem item = new ListItem(text);
        item.setLeadingIcon(icon);
        item.setOnAction(e -> { });
        return item;
    }

    private static Node filters() {
        FlowPane chips = new FlowPane(8, 8, new Chip("Air", Chip.Variant.FILTER), new Chip("Land", Chip.Variant.FILTER),
                new Chip("Sea", Chip.Variant.FILTER));
        ((Chip) chips.getChildren().getFirst()).setSelected(true);
        VBox box = new VBox(16, chips, new CheckBox("Show inactive units"), new CheckBox("Show trails"));
        box.setPadding(new Insets(0, 24, 0, 24));
        return box;
    }
}
