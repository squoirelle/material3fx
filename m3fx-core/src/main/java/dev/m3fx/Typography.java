package dev.m3fx;

import javafx.scene.Node;

/**
 * The Material 3 type scale. Each role is a style class:
 * <pre>{@code
 * Label title = new Label("Settings");
 * Typography.TITLE_LARGE.apply(title);   // or title.getStyleClass().add("title-large")
 * }</pre>
 */
public enum Typography {
    DISPLAY_LARGE("display-large", 57, 64, 400),
    DISPLAY_MEDIUM("display-medium", 45, 52, 400),
    DISPLAY_SMALL("display-small", 36, 44, 400),
    HEADLINE_LARGE("headline-large", 32, 40, 400),
    HEADLINE_MEDIUM("headline-medium", 28, 36, 400),
    HEADLINE_SMALL("headline-small", 24, 32, 400),
    TITLE_LARGE("title-large", 22, 28, 400),
    TITLE_MEDIUM("title-medium", 16, 24, 500),
    TITLE_SMALL("title-small", 14, 20, 500),
    BODY_LARGE("body-large", 16, 24, 400),
    BODY_MEDIUM("body-medium", 14, 20, 400),
    BODY_SMALL("body-small", 12, 16, 400),
    LABEL_LARGE("label-large", 14, 20, 500),
    LABEL_MEDIUM("label-medium", 12, 16, 500),
    LABEL_SMALL("label-small", 11, 16, 500);

    private final String styleClass;
    private final double size;
    private final double lineHeight;
    private final int weight;

    Typography(String styleClass, double size, double lineHeight, int weight) {
        this.styleClass = styleClass;
        this.size = size;
        this.lineHeight = lineHeight;
        this.weight = weight;
    }

    public String styleClass() {
        return styleClass;
    }

    public double size() {
        return size;
    }

    public double lineHeight() {
        return lineHeight;
    }

    public int weight() {
        return weight;
    }

    /** Replaces any other type role on the node with this one. */
    public <T extends Node> T apply(T node) {
        for (Typography t : values()) {
            node.getStyleClass().remove(t.styleClass);
        }
        node.getStyleClass().add(styleClass);
        return node;
    }
}
