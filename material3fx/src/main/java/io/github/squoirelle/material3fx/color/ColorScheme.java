package io.github.squoirelle.material3fx.color;

import javafx.scene.paint.Color;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * A complete, immutable Material 3 color scheme: a color for every {@link ColorRole}.
 *
 * <pre>{@code
 * ColorScheme scheme = ColorScheme.builder(Color.web("#6750A4"))
 *         .dark(true)
 *         .variant(Variant.TONAL_SPOT)
 *         .build();
 * Color primary = scheme.get(ColorRole.PRIMARY);
 * }</pre>
 */
public final class ColorScheme {

    private final Hct seed;
    private final boolean dark;
    private final Variant variant;
    private final double contrast;
    private final Map<ColorRole.Palette, TonalPalette> palettes;
    private final Map<ColorRole, Integer> colors = new EnumMap<>(ColorRole.class);

    private ColorScheme(Builder builder) {
        this.seed = Hct.fromInt(builder.seed);
        this.dark = builder.dark;
        this.variant = builder.variant;
        this.contrast = builder.contrast;

        TonalPalette[] base = variant.palettes(seed);
        palettes = new EnumMap<>(ColorRole.Palette.class);
        palettes.put(ColorRole.Palette.PRIMARY, base[0]);
        palettes.put(ColorRole.Palette.SECONDARY, override(builder.secondary, base[1]));
        palettes.put(ColorRole.Palette.TERTIARY, override(builder.tertiary, base[2]));
        palettes.put(ColorRole.Palette.NEUTRAL, override(builder.neutral, base[3]));
        palettes.put(ColorRole.Palette.NEUTRAL_VARIANT, override(builder.neutralVariant, base[4]));
        palettes.put(ColorRole.Palette.ERROR, override(builder.error, TonalPalette.fromHueAndChroma(25, 84)));

        for (ColorRole role : ColorRole.values()) {
            colors.put(role, palettes.get(role.palette()).tone(role.tone(dark, contrast)));
        }
    }

    private static TonalPalette override(Integer argb, TonalPalette fallback) {
        return argb == null ? fallback : TonalPalette.fromHct(Hct.fromInt(argb));
    }

    public static ColorScheme of(Color seed, boolean dark) {
        return builder(seed).dark(dark).build();
    }

    public static Builder builder(Color seed) {
        return new Builder(ColorUtils.fromFx(seed) | 0xFF000000);
    }

    public Color get(ColorRole role) {
        return ColorUtils.toFx(colors.get(role));
    }

    public int getArgb(ColorRole role) {
        return colors.get(role);
    }

    public TonalPalette palette(ColorRole.Palette palette) {
        return palettes.get(palette);
    }

    public Hct seed() {
        return seed;
    }

    public boolean isDark() {
        return dark;
    }

    public Variant variant() {
        return variant;
    }

    public double contrast() {
        return contrast;
    }

    /**
     * Builds the four roles of a custom (extended) color, e.g. a "success" or "warning" color,
     * harmonized towards this scheme's primary so it fits the palette.
     */
    public CustomColor customColor(String name, Color color, boolean harmonize) {
        int argb = ColorUtils.fromFx(color) | 0xFF000000;
        if (harmonize) {
            argb = Blend.harmonize(argb, colors.get(ColorRole.PRIMARY));
        }
        TonalPalette palette = TonalPalette.fromHueAndChroma(Hct.fromInt(argb).getHue(),
                Math.max(Hct.fromInt(argb).getChroma(), 48));
        return new CustomColor(name,
                ColorUtils.toFx(palette.tone(dark ? 80 : 40)),
                ColorUtils.toFx(palette.tone(dark ? 20 : 100)),
                ColorUtils.toFx(palette.tone(dark ? 30 : 90)),
                ColorUtils.toFx(palette.tone(dark ? 90 : 30)));
    }

    /** The four roles of a custom color. */
    public record CustomColor(String name, Color color, Color onColor, Color container, Color onContainer) {
    }

    public static final class Builder {
        private final int seed;
        private boolean dark;
        private Variant variant = Variant.TONAL_SPOT;
        private double contrast;
        private Integer secondary;
        private Integer tertiary;
        private Integer neutral;
        private Integer neutralVariant;
        private Integer error;

        private Builder(int seed) {
            this.seed = seed;
        }

        public Builder dark(boolean dark) {
            this.dark = dark;
            return this;
        }

        public Builder variant(Variant variant) {
            this.variant = Objects.requireNonNull(variant);
            return this;
        }

        /** 0 = standard, 0.5 = medium, 1 = high contrast. */
        public Builder contrast(double contrast) {
            this.contrast = MathUtils.clampDouble(0, 1, contrast);
            return this;
        }

        /** Overrides the secondary key color instead of deriving it from the seed. */
        public Builder secondary(Color color) {
            this.secondary = color == null ? null : ColorUtils.fromFx(color) | 0xFF000000;
            return this;
        }

        public Builder tertiary(Color color) {
            this.tertiary = color == null ? null : ColorUtils.fromFx(color) | 0xFF000000;
            return this;
        }

        public Builder neutral(Color color) {
            this.neutral = color == null ? null : ColorUtils.fromFx(color) | 0xFF000000;
            return this;
        }

        public Builder neutralVariant(Color color) {
            this.neutralVariant = color == null ? null : ColorUtils.fromFx(color) | 0xFF000000;
            return this;
        }

        public Builder error(Color color) {
            this.error = color == null ? null : ColorUtils.fromFx(color) | 0xFF000000;
            return this;
        }

        public ColorScheme build() {
            return new ColorScheme(this);
        }
    }
}
