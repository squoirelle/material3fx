package dev.m3fx.demo;

import dev.m3fx.controls.Icon;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

/** Plain JavaFX controls, styled only by the m3fx stylesheet (phase 1). */
final class StandardControlsPage {

    private StandardControlsPage() {
    }

    static Node buttons() {
        Button filled = new Button("Filled");
        Button tonal = new Button("Tonal");
        tonal.getStyleClass().add("tonal");
        Button elevated = new Button("Elevated");
        elevated.getStyleClass().add("elevated");
        Button outlined = new Button("Outlined");
        outlined.getStyleClass().add("outlined");
        Button text = new Button("Text");
        text.getStyleClass().add("text");
        Button disabled = new Button("Disabled");
        disabled.setDisable(true);
        Button withIcon = new Button("Add item", new Icon("add", 18));
        withIcon.getStyleClass().add("icon-leading");
        Button danger = new Button("Delete", new Icon("delete", 18));
        danger.getStyleClass().addAll("error-button", "icon-leading");
        filled.setTooltip(new Tooltip("Filled buttons have the most visual impact"));

        ToggleGroup group = new ToggleGroup();
        ToggleButton day = new ToggleButton("Day");
        ToggleButton week = new ToggleButton("Week");
        ToggleButton month = new ToggleButton("Month");
        for (ToggleButton b : new ToggleButton[] {day, week, month}) {
            b.setToggleGroup(group);
        }
        week.setSelected(true);

        CheckBox checked = new CheckBox("Checked");
        checked.setSelected(true);
        CheckBox unchecked = new CheckBox("Unchecked");
        CheckBox indeterminate = new CheckBox("Indeterminate");
        indeterminate.setAllowIndeterminate(true);
        indeterminate.setIndeterminate(true);
        CheckBox disabledCheck = new CheckBox("Disabled");
        disabledCheck.setSelected(true);
        disabledCheck.setDisable(true);
        CheckBox error = new CheckBox("Error");
        error.getStyleClass().add("error");

        ToggleGroup radios = new ToggleGroup();
        RadioButton r1 = new RadioButton("Option A");
        RadioButton r2 = new RadioButton("Option B");
        RadioButton r3 = new RadioButton("Disabled");
        r1.setToggleGroup(radios);
        r2.setToggleGroup(radios);
        r1.setSelected(true);
        r3.setDisable(true);

        MenuButton menuButton = new MenuButton("Menu button", null,
                new MenuItem("Cut", new Icon("content_cut", 20)), new MenuItem("Copy", new Icon("content_copy", 20)),
                new MenuItem("Paste", new Icon("content_paste", 20)));
        SplitMenuButton split = new SplitMenuButton(new MenuItem("Save as..."), new MenuItem("Export"));
        split.setText("Save");

        return Gallery.page("Standard controls",
                "Plain JavaFX controls. No m3fx classes involved: the stylesheet and skins do all the work.",
                Gallery.section("Buttons", Gallery.row(filled, tonal, elevated, outlined, text, disabled),
                        Gallery.row(withIcon, danger, new Hyperlink("Hyperlink"))),
                Gallery.section("Toggle buttons and menu buttons", Gallery.row(new HBox(8, day, week, month), menuButton, split)),
                Gallery.section("Checkboxes", Gallery.row(checked, unchecked, indeterminate, error, disabledCheck)),
                Gallery.section("Radio buttons", Gallery.row(r1, r2, r3)));
    }

    static Node inputs() {
        TextField field = new TextField();
        field.setPromptText("Outlined text field");
        TextField filled = new TextField("Filled text field");
        filled.getStyleClass().add("filled");
        PasswordField password = new PasswordField();
        password.setText("secret");
        TextField errorField = new TextField("Invalid value");
        errorField.getStyleClass().add("error");
        TextField disabled = new TextField("Disabled");
        disabled.setDisable(true);

        ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList("Alpha", "Bravo", "Charlie", "Delta"));
        combo.getSelectionModel().select(1);
        ComboBox<String> editable = new ComboBox<>(FXCollections.observableArrayList("Paris", "Lyon", "Toulouse"));
        editable.setEditable(true);
        editable.setPromptText("Editable");
        ChoiceBox<String> choice = new ChoiceBox<>(FXCollections.observableArrayList("Low", "Medium", "High"));
        choice.getSelectionModel().select(2);
        DatePicker date = new DatePicker(LocalDate.of(2026, 10, 22));
        Spinner<Integer> spinner = new Spinner<>(0, 100, 42);
        spinner.setEditable(true);

        TextArea area = new TextArea("A multi-line text area.\nIt uses the outlined style too.");
        area.setPrefRowCount(3);
        area.setPrefWidth(420);

        Slider slider = new Slider(0, 100, 40);
        slider.setPrefWidth(320);
        Slider ticks = new Slider(0, 10, 7);
        ticks.setShowTickMarks(true);
        ticks.setMajorTickUnit(1);
        ticks.setMinorTickCount(0);
        ticks.setSnapToTicks(true);
        ticks.setPrefWidth(320);
        Slider disabledSlider = new Slider(0, 100, 60);
        disabledSlider.setDisable(true);

        ProgressBar determinate = new ProgressBar(0.6);
        determinate.setPrefWidth(320);
        ProgressBar indeterminate = new ProgressBar(-1);
        indeterminate.setPrefWidth(320);
        ProgressIndicator circular = new ProgressIndicator(0.7);
        ProgressIndicator spinning = new ProgressIndicator(-1);
        ProgressIndicator small = new ProgressIndicator(-1);
        small.getStyleClass().add("small");

        return Gallery.page("Inputs", "Text fields, dropdowns, pickers, sliders and progress indicators.",
                Gallery.section("Text fields", Gallery.row(field, filled, password), Gallery.row(errorField, disabled)),
                Gallery.section("Dropdowns and pickers", Gallery.row(combo, editable, choice, date, spinner)),
                Gallery.section("Text area", area),
                Gallery.section("Sliders", Gallery.row(slider, ticks, disabledSlider)),
                Gallery.section("Progress", Gallery.row(new VBox(24, determinate, indeterminate), circular, spinning, small)));
    }

    record Person(String name, String role, String location, int clearance) {
    }

    static Node data() {
        ListView<String> list = new ListView<>(FXCollections.observableArrayList(
                "Inbox", "Starred", "Sent", "Drafts", "Spam", "Trash"));
        list.getSelectionModel().select(1);
        list.setPrefSize(260, 300);
        list.getStyleClass().add("outlined");

        TableView<Person> table = new TableView<>(FXCollections.observableArrayList(
                new Person("Ada Lovelace", "Analyst", "London", 3),
                new Person("Grace Hopper", "Engineer", "Arlington", 5),
                new Person("Alan Turing", "Cryptographer", "Bletchley", 5),
                new Person("Hedy Lamarr", "Inventor", "Vienna", 2),
                new Person("Margaret Hamilton", "Lead engineer", "Cambridge", 4)));
        table.getColumns().add(column("Name", Person::name, 180));
        table.getColumns().add(column("Role", Person::role, 140));
        table.getColumns().add(column("Location", Person::location, 120));
        table.getColumns().add(column("Clearance", p -> String.valueOf(p.clearance()), 100));
        table.getSelectionModel().select(1);
        table.setPrefSize(580, 300);

        TreeItem<String> root = new TreeItem<>("Project");
        TreeItem<String> src = new TreeItem<>("src");
        src.getChildren().setAll(java.util.List.of(new TreeItem<>("Main.java"), new TreeItem<>("Theme.java")));
        src.setExpanded(true);
        root.getChildren().setAll(java.util.List.of(src, new TreeItem<>("README.md"), new TreeItem<>("build.gradle.kts")));
        root.setExpanded(true);
        TreeView<String> tree = new TreeView<>(root);
        tree.setPrefSize(260, 300);
        tree.getStyleClass().add("outlined");

        TabPane tabs = new TabPane(new Tab("Overview"), new Tab("Specifications"), new Tab("History"));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().forEach(t -> t.setContent(new VBox()));
        tabs.setPrefSize(520, 60);

        TitledPane general = new TitledPane("General", new VBox(8, new CheckBox("Enable telemetry"), new CheckBox("Auto update")));
        TitledPane advanced = new TitledPane("Advanced", new VBox(8, new TextField("localhost:8080")));
        Accordion accordion = new Accordion(general, advanced);
        accordion.setExpandedPane(general);
        accordion.setPrefWidth(360);

        return Gallery.page("Data", "Lists, tables, trees, tabs and titled panes.",
                Gallery.section("List, table, tree", Gallery.row(list, table, tree)),
                Gallery.section("Tabs and accordion", Gallery.row(tabs, accordion)));
    }

    private static TableColumn<Person, String> column(String title, java.util.function.Function<Person, String> getter, double width) {
        TableColumn<Person, String> col = new TableColumn<>(title);
        col.setCellValueFactory(c -> new SimpleStringProperty(getter.apply(c.getValue())));
        col.setPrefWidth(width);
        return col;
    }
}
