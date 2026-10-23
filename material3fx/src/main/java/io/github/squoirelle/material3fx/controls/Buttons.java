package io.github.squoirelle.material3fx.controls;

import javafx.scene.control.Button;

/**
 * Factories for the five Material button styles. They return plain {@link Button}s with the
 * matching style class, so they behave exactly like any JavaFX button.
 *
 * <pre>{@code
 * Button save = Buttons.filled("Save", "save");
 * Button cancel = Buttons.text("Cancel");
 * }</pre>
 */
public final class Buttons {

    private Buttons() {
    }

    public static Button filled(String text) {
        return create(text, null, null);
    }

    public static Button filled(String text, String icon) {
        return create(text, icon, null);
    }

    public static Button tonal(String text) {
        return create(text, null, "tonal");
    }

    public static Button tonal(String text, String icon) {
        return create(text, icon, "tonal");
    }

    public static Button elevated(String text) {
        return create(text, null, "elevated");
    }

    public static Button elevated(String text, String icon) {
        return create(text, icon, "elevated");
    }

    public static Button outlined(String text) {
        return create(text, null, "outlined");
    }

    public static Button outlined(String text, String icon) {
        return create(text, icon, "outlined");
    }

    public static Button text(String text) {
        return create(text, null, "text");
    }

    public static Button text(String text, String icon) {
        return create(text, icon, "text");
    }

    private static Button create(String text, String icon, String styleClass) {
        Button button = new Button(text);
        if (styleClass != null) {
            button.getStyleClass().add(styleClass);
        }
        if (icon != null) {
            button.setGraphic(new Icon(icon, 18));
            button.getStyleClass().add("icon-leading");
        }
        return button;
    }
}
