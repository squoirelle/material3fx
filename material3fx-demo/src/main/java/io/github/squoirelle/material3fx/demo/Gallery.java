package io.github.squoirelle.material3fx.demo;

import io.github.squoirelle.material3fx.Typography;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Supplier;

/** The pages of the demo, shared by the app and the screenshot renderer. */
public final class Gallery {

    public record Page(String id, String title, String icon, Supplier<Node> content, double screenshotHeight) {
    }

    private Gallery() {
    }

    public static List<Page> pages() {
        return List.of(
                new Page("theme", "Color scheme", "palette", ThemePage::page, 1020),
                new Page("standard-buttons", "Standard controls", "smart_button", StandardControlsPage::buttons, 760),
                new Page("standard-inputs", "Inputs", "edit", StandardControlsPage::inputs, 900),
                new Page("standard-data", "Data", "table_chart", StandardControlsPage::data, 900),
                new Page("buttons", "Buttons", "touch_app", ComponentsPage::buttons, 640),
                new Page("chips-cards", "Chips and cards", "style", ComponentsPage::chipsAndCards, 760),
                new Page("selection", "Selection", "toggle_on", ComponentsPage::selection, 820),
                new Page("navigation", "Navigation", "explore", NavigationPage::page, 1500),
                new Page("text-fields", "Text fields", "text_fields", InputsPage::textFields, 640),
                new Page("lists", "Lists", "list", InputsPage::lists, 720),
                new Page("pickers", "Pickers", "schedule", InputsPage::pickers, 820),
                new Page("overlays", "Overlays", "layers", OverlaysPage::page, 1100),
                new Page("data-entry", "Data entry", "edit_note", DataEntryPage::page, 1000),
                new Page("feedback", "Status and feedback", "notifications", FeedbackPage::page, 760),
                new Page("adaptive", "Adaptive layout", "devices", AdaptivePage::page, 720)
        );
    }

    static Parent wrapForScreenshot(Page page) {
        VBox box = new VBox(page.content().get());
        box.getStyleClass().add("surface");
        return box;
    }

    // Layout helpers used by the pages

    static VBox page(String title, String subtitle, Node... sections) {
        Label heading = Typography.HEADLINE_MEDIUM.apply(new Label(title));
        Label sub = Typography.BODY_LARGE.apply(new Label(subtitle));
        sub.getStyleClass().add("on-surface-variant");
        sub.setWrapText(true);
        VBox box = new VBox(8, heading, sub);
        VBox.setMargin(sub, new Insets(0, 0, 16, 0));
        box.getChildren().addAll(sections);
        box.setPadding(new Insets(24, 32, 32, 32));
        return box;
    }

    static VBox section(String title, Node... content) {
        Label label = Typography.TITLE_MEDIUM.apply(new Label(title));
        VBox box = new VBox(12, label);
        box.getChildren().addAll(content);
        box.setPadding(new Insets(12, 0, 12, 0));
        return box;
    }

    static FlowPane row(Node... nodes) {
        FlowPane row = new FlowPane(16, 16, nodes);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}
