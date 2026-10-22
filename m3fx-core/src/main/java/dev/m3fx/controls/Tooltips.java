package dev.m3fx.controls;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Plain and rich Material tooltips.
 *
 * <pre>{@code
 * Tooltips.install(saveButton, "Save");
 * Tooltips.installRich(info, "Clearance", "Level 3 grants access to tactical overlays.");
 * }</pre>
 */
public final class Tooltips {

    private Tooltips() {
    }

    public static Tooltip plain(String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.millis(500));
        return tooltip;
    }

    /** A rich tooltip with an optional title and longer supporting text. */
    public static Tooltip rich(String title, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.getStyleClass().add("rich");
        Label body = new Label(text);
        body.getStyleClass().add("rich-tooltip-text");
        body.setWrapText(true);
        body.setMaxWidth(312);
        VBox content = new VBox(4);
        if (title != null) {
            Label header = new Label(title);
            header.getStyleClass().add("rich-tooltip-title");
            content.getChildren().add(header);
        }
        content.getChildren().add(body);
        tooltip.setGraphic(content);
        tooltip.setShowDelay(Duration.millis(500));
        tooltip.setShowDuration(Duration.seconds(30));
        return tooltip;
    }

    public static Tooltip install(Node node, String text) {
        Tooltip tooltip = plain(text);
        Tooltip.install(node, tooltip);
        return tooltip;
    }

    public static Tooltip installRich(Node node, String title, String text) {
        Tooltip tooltip = rich(title, text);
        Tooltip.install(node, tooltip);
        return tooltip;
    }
}
