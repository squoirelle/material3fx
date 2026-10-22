package dev.m3fx.controls;

import javafx.geometry.Orientation;
import javafx.scene.control.Separator;

/**
 * A Material divider: a {@link Separator} styled as a thin outline-variant line. Use the
 * {@code inset}, {@code inset-start} or {@code inset-end} style classes for inset dividers.
 */
public class Divider extends Separator {

    public Divider() {
        this(Orientation.HORIZONTAL);
    }

    public Divider(Orientation orientation) {
        super(orientation);
        getStyleClass().add("divider");
    }

    /** An inset divider, indented on the leading side (e.g. to align with list item text). */
    public static Divider inset() {
        Divider divider = new Divider();
        divider.getStyleClass().add("inset-start");
        return divider;
    }
}
