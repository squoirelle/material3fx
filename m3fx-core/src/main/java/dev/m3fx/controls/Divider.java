package dev.m3fx.controls;

import javafx.geometry.Orientation;
import javafx.scene.layout.Region;

/**
 * A thin line grouping content in lists and layouts. Use the {@code inset}, {@code inset-start}
 * or {@code inset-end} style classes for inset dividers.
 */
public class Divider extends Region {

    private final Orientation orientation;

    public Divider() {
        this(Orientation.HORIZONTAL);
    }

    public Divider(Orientation orientation) {
        this.orientation = orientation;
        getStyleClass().addAll("divider", orientation == Orientation.HORIZONTAL ? "horizontal" : "vertical");
        if (orientation == Orientation.HORIZONTAL) {
            setMinHeight(1);
            setPrefHeight(1);
            setMaxHeight(1);
            setMaxWidth(Double.MAX_VALUE);
        } else {
            setMinWidth(1);
            setPrefWidth(1);
            setMaxWidth(1);
            setMaxHeight(Double.MAX_VALUE);
        }
    }

    /** An inset divider, indented on the leading side (e.g. to align with list item text). */
    public static Divider inset() {
        Divider divider = new Divider();
        divider.getStyleClass().add("inset-start");
        return divider;
    }

    public Orientation getOrientation() {
        return orientation;
    }
}
