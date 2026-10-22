package dev.m3fx.demo;

import dev.m3fx.Typography;
import dev.m3fx.controls.Avatar;
import dev.m3fx.controls.Badge;
import dev.m3fx.controls.Buttons;
import dev.m3fx.controls.Card;
import dev.m3fx.controls.Chip;
import dev.m3fx.controls.Divider;
import dev.m3fx.controls.Fab;
import dev.m3fx.controls.Icon;
import dev.m3fx.controls.IconButton;
import dev.m3fx.controls.SegmentedButton;
import dev.m3fx.controls.Switch;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/** m3fx components: buttons, chips, cards, switches, badges. */
final class ComponentsPage {

    private ComponentsPage() {
    }

    static Node buttons() {
        FlowPane iconButtons = Gallery.row(
                new IconButton("settings").tooltip("Standard"),
                new IconButton("edit", IconButton.Variant.FILLED).tooltip("Filled"),
                new IconButton("tune", IconButton.Variant.TONAL).tooltip("Tonal"),
                new IconButton("share", IconButton.Variant.OUTLINED).tooltip("Outlined"),
                toggle("favorite", IconButton.Variant.STANDARD, true),
                toggle("bookmark", IconButton.Variant.FILLED, true),
                toggle("bookmark", IconButton.Variant.FILLED, false),
                toggle("push_pin", IconButton.Variant.TONAL, true),
                toggle("visibility", IconButton.Variant.OUTLINED, true),
                disabled(new IconButton("delete", IconButton.Variant.FILLED)));

        Fab small = new Fab("add");
        small.setSize(Fab.Size.SMALL);
        Fab regular = new Fab("edit");
        Fab large = new Fab("navigation");
        large.setSize(Fab.Size.LARGE);
        Fab secondary = new Fab("call");
        secondary.setColor(Fab.Color.SECONDARY);
        Fab tertiary = new Fab("photo_camera");
        tertiary.setColor(Fab.Color.TERTIARY);
        Fab surface = new Fab("add");
        surface.setColor(Fab.Color.SURFACE);
        Fab extended = new Fab("edit", "Compose");
        Fab extendedTertiary = new Fab("add", "New mission");
        extendedTertiary.setColor(Fab.Color.TERTIARY);

        SegmentedButton single = new SegmentedButton("Day", "Week", "Month", "Year");
        single.getButtons().get(1).setSelected(true);
        SegmentedButton icons = new SegmentedButton();
        icons.addSegment("Map", "map");
        icons.addSegment("Satellite", "satellite_alt").setSelected(true);
        icons.addSegment("Terrain", "terrain");
        SegmentedButton multi = new SegmentedButton("$", "$$", "$$$", "$$$$");
        multi.setMultiSelect(true);
        multi.getButtons().get(0).setSelected(true);
        multi.getButtons().get(1).setSelected(true);

        return Gallery.page("Buttons", "Common buttons, icon buttons, FABs and segmented buttons.",
                Gallery.section("Common buttons (Buttons factory)", Gallery.row(
                        Buttons.elevated("Elevated", "add"), Buttons.filled("Filled", "send"), Buttons.tonal("Tonal", "tune"),
                        Buttons.outlined("Outlined", "download"), Buttons.text("Text", "open_in_new"))),
                Gallery.section("Icon buttons: standard, filled, tonal, outlined, toggles", iconButtons),
                Gallery.section("Floating action buttons", Gallery.row(small, regular, large, secondary, tertiary, surface, extended, extendedTertiary)),
                Gallery.section("Segmented buttons", Gallery.row(single, icons, multi)));
    }

    private static IconButton toggle(String icon, IconButton.Variant variant, boolean selected) {
        IconButton b = new IconButton(icon, variant);
        b.setToggle(true);
        b.setSelected(selected);
        return b;
    }

    private static <T extends Node> T disabled(T node) {
        node.setDisable(true);
        return node;
    }

    static Node chipsAndCards() {
        Chip assist = new Chip("Add to calendar", "event", Chip.Variant.ASSIST);
        Chip assistElevated = new Chip("Directions", "directions", Chip.Variant.ASSIST);
        assistElevated.setElevated(true);
        Chip filter1 = new Chip("Radar", Chip.Variant.FILTER);
        filter1.setSelected(true);
        Chip filter2 = new Chip("Sonar", Chip.Variant.FILTER);
        Chip filter3 = new Chip("Infrared", Chip.Variant.FILTER);
        filter3.setSelected(true);
        Chip input1 = new Chip("Ada Lovelace", Chip.Variant.INPUT);
        Avatar avatar = new Avatar("Ada Lovelace");
        avatar.setSize(24);
        input1.setAvatar(avatar);
        Chip input2 = new Chip("report.pdf", "description", Chip.Variant.INPUT);
        Chip suggestion1 = new Chip("Sounds good", Chip.Variant.SUGGESTION);
        Chip suggestion2 = new Chip("On my way", Chip.Variant.SUGGESTION);
        Chip disabledChip = disabled(new Chip("Disabled", "block", Chip.Variant.ASSIST));

        Card elevated = card(Card.Variant.ELEVATED, "Elevated card", "Surface container low with a shadow.");
        Card filled = card(Card.Variant.FILLED, "Filled card", "Surface container highest, no outline.");
        Card outlined = card(Card.Variant.OUTLINED, "Outlined card", "Surface with an outline variant border.");
        Card clickable = card(Card.Variant.ELEVATED, "Clickable card", "Hover it: state layer and ripple.");
        clickable.setOnAction(e -> System.out.println("Card clicked"));


        Region image = new Region();
        image.setPrefSize(300, 140);
        image.setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(
                new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#6750A4")), new Stop(1, Color.web("#E8DEF8"))), null, null)));
        Label title = new Label("Media card");
        title.getStyleClass().add("card-title");
        Label sub = new Label("Media bleeds to the rounded corners.");
        sub.getStyleClass().add("card-subtitle");
        HBox actions = new HBox(8, Buttons.text("Share"), Buttons.filled("Open"));
        actions.setAlignment(Pos.CENTER_RIGHT);
        VBox body = new VBox(4, title, sub, actions);
        body.setPadding(new Insets(16));
        VBox.setMargin(actions, new Insets(12, 0, 0, 0));
        Card media = new Card(Card.Variant.OUTLINED, image, body);
        media.getStyleClass().add("flush");
        media.setPrefWidth(300);

        return Gallery.page("Chips and cards", "Assist, filter, input and suggestion chips; elevated, filled, outlined and clickable cards.",
                Gallery.section("Chips", Gallery.row(assist, assistElevated, filter1, filter2, filter3),
                        Gallery.row(input1, input2, suggestion1, suggestion2, disabledChip)),
                Gallery.section("Cards", Gallery.row(elevated, filled, outlined, clickable), Gallery.row(media)));
    }

    private static Card card(Card.Variant variant, String title, String text) {
        Label t = new Label(title);
        t.getStyleClass().add("card-title");
        Label s = new Label(text);
        s.getStyleClass().add("card-subtitle");
        s.setWrapText(true);
        Card card = new Card(variant, t, s);
        card.setPrefSize(220, 110);
        return card;
    }

    static Node selection() {
        Switch s1 = new Switch();
        Switch s2 = new Switch();
        s2.setSelected(true);
        Switch s3 = new Switch();
        s3.setShowIcons(true);
        Switch s4 = new Switch();
        s4.setShowIcons(true);
        s4.setSelected(true);
        Switch s5 = disabled(new Switch());
        s5.setSelected(true);

        VBox settings = new VBox(4,
                labeled(new Switch("Wi-Fi"), true),
                labeled(new Switch("Bluetooth"), false),
                labeled(new Switch("Airplane mode"), false));
        settings.setMaxWidth(320);

        Node mail = Badge.attach(new Icon("mail"), new Badge());
        Node notifications = Badge.attach(new Icon("notifications"), Badge.count(3));
        Node chat = Badge.attach(new Icon("chat"), Badge.count(1200));
        Node alert = Badge.attach(new Icon("warning"), new Badge("NEW"));

        Avatar a1 = new Avatar("Grace Hopper");
        Avatar a2 = new Avatar("Alan Turing");
        a2.setSize(56);
        Avatar a3 = new Avatar("hedy");
        a3.setSize(32);

        VBox dividers = new VBox(12, new Label("Full width divider"), new Divider(), new Label("Inset divider"), Divider.inset(),
                new Label("End"));
        dividers.setPrefWidth(320);
        HBox vertical = new HBox(12, new Label("Left"), new Divider(Orientation.VERTICAL), new Label("Right"));
        vertical.setPrefHeight(40);
        vertical.setAlignment(Pos.CENTER_LEFT);

        StackPane spacer = new StackPane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        return Gallery.page("Selection, badges, avatars", "Switches with and without icons, badges, avatars and dividers.",
                Gallery.section("Switches", Gallery.row(s1, s2, s3, s4, s5), settings),
                Gallery.section("Badges", Gallery.row(mail, notifications, chat, alert)),
                Gallery.section("Avatars", Gallery.row(a3, a1, a2)),
                Gallery.section("Dividers", Gallery.row(dividers, vertical)),
                Typography.LABEL_SMALL.apply(new Label("Tip: every component follows the theme live.")));
    }

    private static Switch labeled(Switch s, boolean selected) {
        s.setSelected(selected);
        return s;
    }
}
