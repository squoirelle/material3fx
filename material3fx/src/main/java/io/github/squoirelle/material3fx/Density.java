package io.github.squoirelle.material3fx;

/**
 * Material density scale. Each step removes 4px from the height of buttons, text fields, list
 * items, table rows, chips and navigation items. Material's default targets touch screens;
 * desktop and data-heavy applications usually want {@link #COMPACT} or {@link #DENSE}.
 */
public enum Density {
    DEFAULT(0),
    COMFORTABLE(-1),
    COMPACT(-2),
    DENSE(-3);

    private final int scale;

    Density(int scale) {
        this.scale = scale;
    }

    /** 0, -1, -2 or -3. */
    public int scale() {
        return scale;
    }

    /** Height reduction in pixels. */
    public int pixels() {
        return -scale * 4;
    }

    String styleClass() {
        return "md-density-" + (-scale);
    }
}
