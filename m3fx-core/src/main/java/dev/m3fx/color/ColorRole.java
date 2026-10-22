package dev.m3fx.color;

import java.util.Locale;

/**
 * Every Material 3 color role, with the tone it takes from its palette in light and dark schemes.
 * <p>
 * Roles that are drawn <em>on top of</em> another role name it as their {@code against} role;
 * raising the scheme's contrast level pushes them further away from it.
 */
public enum ColorRole {
    PRIMARY(Palette.PRIMARY, 40, 80, "SURFACE"),
    ON_PRIMARY(Palette.PRIMARY, 100, 20, "PRIMARY"),
    PRIMARY_CONTAINER(Palette.PRIMARY, 90, 30, null),
    ON_PRIMARY_CONTAINER(Palette.PRIMARY, 30, 90, "PRIMARY_CONTAINER"),
    INVERSE_PRIMARY(Palette.PRIMARY, 80, 40, "INVERSE_SURFACE"),
    PRIMARY_FIXED(Palette.PRIMARY, 90, 90, null),
    PRIMARY_FIXED_DIM(Palette.PRIMARY, 80, 80, null),
    ON_PRIMARY_FIXED(Palette.PRIMARY, 10, 10, "PRIMARY_FIXED"),
    ON_PRIMARY_FIXED_VARIANT(Palette.PRIMARY, 30, 30, "PRIMARY_FIXED"),

    SECONDARY(Palette.SECONDARY, 40, 80, "SURFACE"),
    ON_SECONDARY(Palette.SECONDARY, 100, 20, "SECONDARY"),
    SECONDARY_CONTAINER(Palette.SECONDARY, 90, 30, null),
    ON_SECONDARY_CONTAINER(Palette.SECONDARY, 30, 90, "SECONDARY_CONTAINER"),
    SECONDARY_FIXED(Palette.SECONDARY, 90, 90, null),
    SECONDARY_FIXED_DIM(Palette.SECONDARY, 80, 80, null),
    ON_SECONDARY_FIXED(Palette.SECONDARY, 10, 10, "SECONDARY_FIXED"),
    ON_SECONDARY_FIXED_VARIANT(Palette.SECONDARY, 30, 30, "SECONDARY_FIXED"),

    TERTIARY(Palette.TERTIARY, 40, 80, "SURFACE"),
    ON_TERTIARY(Palette.TERTIARY, 100, 20, "TERTIARY"),
    TERTIARY_CONTAINER(Palette.TERTIARY, 90, 30, null),
    ON_TERTIARY_CONTAINER(Palette.TERTIARY, 30, 90, "TERTIARY_CONTAINER"),
    TERTIARY_FIXED(Palette.TERTIARY, 90, 90, null),
    TERTIARY_FIXED_DIM(Palette.TERTIARY, 80, 80, null),
    ON_TERTIARY_FIXED(Palette.TERTIARY, 10, 10, "TERTIARY_FIXED"),
    ON_TERTIARY_FIXED_VARIANT(Palette.TERTIARY, 30, 30, "TERTIARY_FIXED"),

    ERROR(Palette.ERROR, 40, 80, "SURFACE"),
    ON_ERROR(Palette.ERROR, 100, 20, "ERROR"),
    ERROR_CONTAINER(Palette.ERROR, 90, 30, null),
    ON_ERROR_CONTAINER(Palette.ERROR, 30, 90, "ERROR_CONTAINER"),

    BACKGROUND(Palette.NEUTRAL, 98, 6, null),
    ON_BACKGROUND(Palette.NEUTRAL, 10, 90, "BACKGROUND"),
    SURFACE(Palette.NEUTRAL, 98, 6, null),
    ON_SURFACE(Palette.NEUTRAL, 10, 90, "SURFACE"),
    SURFACE_VARIANT(Palette.NEUTRAL_VARIANT, 90, 30, null),
    ON_SURFACE_VARIANT(Palette.NEUTRAL_VARIANT, 30, 80, "SURFACE"),
    SURFACE_DIM(Palette.NEUTRAL, 87, 6, null),
    SURFACE_BRIGHT(Palette.NEUTRAL, 98, 24, null),
    SURFACE_CONTAINER_LOWEST(Palette.NEUTRAL, 100, 4, null),
    SURFACE_CONTAINER_LOW(Palette.NEUTRAL, 96, 10, null),
    SURFACE_CONTAINER(Palette.NEUTRAL, 94, 12, null),
    SURFACE_CONTAINER_HIGH(Palette.NEUTRAL, 92, 17, null),
    SURFACE_CONTAINER_HIGHEST(Palette.NEUTRAL, 90, 22, null),
    INVERSE_SURFACE(Palette.NEUTRAL, 20, 90, null),
    INVERSE_ON_SURFACE(Palette.NEUTRAL, 95, 20, "INVERSE_SURFACE"),
    OUTLINE(Palette.NEUTRAL_VARIANT, 50, 60, "SURFACE"),
    OUTLINE_VARIANT(Palette.NEUTRAL_VARIANT, 80, 30, "SURFACE"),
    SHADOW(Palette.NEUTRAL, 0, 0, null),
    SCRIM(Palette.NEUTRAL, 0, 0, null),
    SURFACE_TINT(Palette.PRIMARY, 40, 80, null);

    /** The key palettes a scheme is built from. */
    public enum Palette { PRIMARY, SECONDARY, TERTIARY, NEUTRAL, NEUTRAL_VARIANT, ERROR }

    private final Palette palette;
    private final int lightTone;
    private final int darkTone;
    private final String against;

    ColorRole(Palette palette, int lightTone, int darkTone, String against) {
        this.palette = palette;
        this.lightTone = lightTone;
        this.darkTone = darkTone;
        this.against = against;
    }

    public Palette palette() {
        return palette;
    }

    /**
     * Tone of this role.
     *
     * @param contrast 0 for the standard scheme, 0.5 for medium, 1 for high contrast
     */
    public int tone(boolean dark, double contrast) {
        int base = dark ? darkTone : lightTone;
        if (against == null || contrast <= 0) {
            return base;
        }
        ColorRole background = valueOf(against);
        int backgroundTone = dark ? background.darkTone : background.lightTone;
        double k = MathUtils.clampDouble(0, 1, contrast) * 0.5;
        double tone = base <= backgroundTone ? base * (1 - k) : base + (100 - base) * k;
        return (int) Math.round(tone);
    }

    /** The looked-up color name used in stylesheets, e.g. {@code -md-sys-color-on-primary}. */
    public String cssName() {
        return "-md-sys-color-" + name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
