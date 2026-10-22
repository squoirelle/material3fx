package dev.m3fx.demo;

import dev.m3fx.controls.Avatar;
import dev.m3fx.controls.Carousel;
import dev.m3fx.controls.Divider;
import dev.m3fx.controls.Icon;
import dev.m3fx.controls.IconButton;
import dev.m3fx.controls.ListItem;
import dev.m3fx.controls.MaterialTextField;
import dev.m3fx.controls.SearchBar;
import dev.m3fx.controls.Switch;
import dev.m3fx.controls.TimePicker;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Text fields, search, lists, pickers, carousel. */
final class InputsPage {

    private InputsPage() {
    }

    static Node textFields() {
        MaterialTextField outlined = new MaterialTextField("Callsign");
        outlined.setSupportingText("Three to eight letters");
        outlined.setMaxLength(8);
        outlined.setValidator(t -> t.length() < 3 ? "At least three letters" : null);

        MaterialTextField filled = new MaterialTextField("Unit name", MaterialTextField.Variant.FILLED);
        filled.setText("Alpha squadron");
        filled.setClearable(true);

        MaterialTextField withIcon = new MaterialTextField("Search location");
        withIcon.setLeadingIcon("place");
        withIcon.setTrailing(new Icon("my_location"));

        MaterialTextField filledIcon = new MaterialTextField("Email", MaterialTextField.Variant.FILLED);
        filledIcon.setLeadingIcon("mail");
        filledIcon.setText("ops@example.com");

        MaterialTextField amount = new MaterialTextField("Altitude");
        amount.setText("12000");
        amount.setSuffix("ft");

        MaterialTextField price = new MaterialTextField("Budget", MaterialTextField.Variant.FILLED);
        price.setPrefix("$");
        price.setText("2500");

        MaterialTextField password = MaterialTextField.password("Password");
        password.setText("hunter2");

        MaterialTextField error = new MaterialTextField("Frequency");
        error.setText("1234.5");
        error.setErrorMessage("Out of range (118 - 137 MHz)");
        error.setTrailing(new Icon("error"));

        MaterialTextField disabled = new MaterialTextField("Disabled", MaterialTextField.Variant.FILLED);
        disabled.setText("Read only");
        disabled.setDisable(true);

        SearchBar search = new SearchBar("Search units");
        Avatar me = new Avatar("Ada Lovelace");
        me.setSize(30);
        search.getTrailing().addAll(new IconButton("mic"), me);
        List<String> units = List.of("Alpha", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot", "Golf", "Hotel");
        search.setSuggestionProvider(q -> units.stream().filter(u -> u.toLowerCase().contains(q.toLowerCase())).toList());
        search.setMaxWidth(560);

        return Gallery.page("Text fields and search", "Floating labels, outlined and filled variants, icons, prefix / suffix, counters, validation.",
                Gallery.section("Outlined and filled", baseline(Gallery.row(outlined, filled, password))),
                Gallery.section("Icons, prefix and suffix", baseline(Gallery.row(withIcon, filledIcon, amount, price))),
                Gallery.section("Error and disabled", baseline(Gallery.row(error, disabled))),
                Gallery.section("Search bar (type to get suggestions)", search));
    }

    /** Fields with and without supporting text line up on their input text. */
    private static javafx.scene.layout.FlowPane baseline(javafx.scene.layout.FlowPane row) {
        row.setRowValignment(javafx.geometry.VPos.BASELINE);
        return row;
    }

    static Node lists() {
        ListItem one = new ListItem("One line item");
        one.setLeadingIcon("person");
        one.setTrailing(new CheckBox());
        ListItem two = new ListItem("Two line item", "Supporting text");
        two.setLeading(new Avatar("Alan Turing"));
        two.setTrailingText("100+");
        ListItem three = new ListItem("Three line item",
                "Supporting text that is long enough to wrap onto a second line, as three line items do.");
        three.setOverline("Overline");
        three.setLeading(thumbnail());
        ListItem withSwitch = new ListItem("Night vision", "Applies to all displays");
        withSwitch.setLeadingIcon("dark_mode");
        Switch toggle = new Switch();
        toggle.setSelected(true);
        withSwitch.setTrailing(toggle);
        ListItem clickable = new ListItem("Clickable item", "Hover me");
        clickable.setLeadingIcon("touch_app");
        clickable.setTrailing(new Icon("chevron_right"));
        clickable.setOnAction(e -> System.out.println("clicked"));
        ListItem selected = new ListItem("Selected item");
        selected.setLeadingIcon("check_circle");
        selected.setSelected(true);

        VBox list = new VBox(one, new Divider(), two, Divider.inset(), three, withSwitch, clickable, selected);
        list.getStyleClass().add("md-list");
        list.setPrefWidth(440);

        record Contact(String name, String role) {
        }
        ListView<Contact> contacts = new ListView<>(FXCollections.observableArrayList(
                new Contact("Ada Lovelace", "Analyst"), new Contact("Grace Hopper", "Engineer"),
                new Contact("Alan Turing", "Cryptographer"), new Contact("Hedy Lamarr", "Inventor")));
        contacts.setCellFactory(ListItem.cellFactory(c -> {
            ListItem item = new ListItem(c.name(), c.role());
            item.setLeading(new Avatar(c.name()));
            return item;
        }));
        contacts.getSelectionModel().select(1);
        contacts.setPrefSize(360, 300);
        contacts.getStyleClass().add("outlined");

        return Gallery.page("Lists", "List items with leading and trailing elements, and as ListView cells.",
                Gallery.row(Gallery.section("List items in a VBox", list), Gallery.section("ListItem.cellFactory in a ListView", contacts)));
    }

    private static Node thumbnail() {
        StackPane image = new StackPane();
        image.setPrefSize(56, 56);
        image.setMaxSize(56, 56);
        image.setBackground(new Background(new BackgroundFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#7D5260")), new Stop(1, Color.web("#FFD8E4"))), new javafx.scene.layout.CornerRadii(8), null)));
        return image;
    }

    static Node pickers() {
        TimePicker time = new TimePicker(LocalTime.of(14, 25));
        TimePicker time24 = new TimePicker(LocalTime.of(21, 40));
        time24.set24Hour(true);

        DatePicker datePicker = new DatePicker(LocalDate.of(2026, 10, 23));
        Node calendar = new DatePickerSkin(datePicker).getPopupContent();

        Carousel carousel = new Carousel();
        String[][] colors = {{"#6750A4", "#E8DEF8"}, {"#7D5260", "#FFD8E4"}, {"#386A20", "#B8F397"},
                {"#00639B", "#CDE5FF"}, {"#8B5000", "#FFDCBE"}, {"#006A6A", "#6FF7F6"}};
        String[] names = {"Northern sector", "Harbor", "Ridge line", "Coastal patrol", "Desert base", "Lagoon"};
        for (int i = 0; i < colors.length; i++) {
            StackPane item = new StackPane();
            item.setBackground(new Background(new BackgroundFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web(colors[i][0])), new Stop(1, Color.web(colors[i][1]))), null, null)));
            Label label = new Label(names[i]);
            label.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: 500;");
            StackPane.setAlignment(label, Pos.BOTTOM_LEFT);
            StackPane.setMargin(label, new Insets(16));
            item.getChildren().add(label);
            carousel.getItems().add(item);
        }
        carousel.setPrefSize(820, 200);

        return Gallery.page("Pickers and carousel", "Time picker (12h and 24h dials), the restyled date picker calendar and a multi-browse carousel.",
                Gallery.section("Time pickers", Gallery.row(time, time24, calendar)),
                Gallery.section("Carousel (scroll, drag or use arrow keys)", carousel));
    }
}
