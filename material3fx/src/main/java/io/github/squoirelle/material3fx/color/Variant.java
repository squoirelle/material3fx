package io.github.squoirelle.material3fx.color;

/**
 * Scheme variants, matching the ones offered by Material Theme Builder. Each variant derives the
 * five key palettes (primary, secondary, tertiary, neutral, neutral variant) differently from
 * the seed color.
 */
public enum Variant {
    /** The default Material 3 look: calm, slightly tinted, seed hue kept for primary. */
    TONAL_SPOT,
    /** Near-greyscale with a hint of the seed hue. Good for dense, professional tools. */
    NEUTRAL,
    /** High chroma primary, colorful surfaces. */
    VIBRANT,
    /** Rotated hues, playful. */
    EXPRESSIVE,
    /** Pure greys. */
    MONOCHROME,
    /** Keeps the seed color's chroma, so the primary looks like the seed itself. Good for brand colors. */
    FIDELITY,
    /** Like fidelity, with tertiary taken from the color wheel's analogous hue. */
    CONTENT;

    TonalPalette[] palettes(Hct seed) {
        double h = seed.getHue();
        double c = seed.getChroma();
        return switch (this) {
            case TONAL_SPOT -> new TonalPalette[] {
                    TonalPalette.fromHueAndChroma(h, 36),
                    TonalPalette.fromHueAndChroma(h, 16),
                    TonalPalette.fromHueAndChroma(h + 60, 24),
                    TonalPalette.fromHueAndChroma(h, 6),
                    TonalPalette.fromHueAndChroma(h, 8),
            };
            case NEUTRAL -> new TonalPalette[] {
                    TonalPalette.fromHueAndChroma(h, 12),
                    TonalPalette.fromHueAndChroma(h, 8),
                    TonalPalette.fromHueAndChroma(h + 60, 16),
                    TonalPalette.fromHueAndChroma(h, 2),
                    TonalPalette.fromHueAndChroma(h, 2),
            };
            case VIBRANT -> new TonalPalette[] {
                    TonalPalette.fromHueAndChroma(h, 200),
                    TonalPalette.fromHueAndChroma(h + rotation(h, VIBRANT_SECONDARY), 24),
                    TonalPalette.fromHueAndChroma(h + rotation(h, VIBRANT_TERTIARY), 32),
                    TonalPalette.fromHueAndChroma(h, 10),
                    TonalPalette.fromHueAndChroma(h, 12),
            };
            case EXPRESSIVE -> new TonalPalette[] {
                    TonalPalette.fromHueAndChroma(h + 240, 40),
                    TonalPalette.fromHueAndChroma(h + rotation(h, EXPRESSIVE_SECONDARY), 24),
                    TonalPalette.fromHueAndChroma(h + rotation(h, EXPRESSIVE_TERTIARY), 32),
                    TonalPalette.fromHueAndChroma(h + 15, 8),
                    TonalPalette.fromHueAndChroma(h + 15, 12),
            };
            case MONOCHROME -> new TonalPalette[] {
                    TonalPalette.fromHueAndChroma(h, 0),
                    TonalPalette.fromHueAndChroma(h, 0),
                    TonalPalette.fromHueAndChroma(h, 0),
                    TonalPalette.fromHueAndChroma(h, 0),
                    TonalPalette.fromHueAndChroma(h, 0),
            };
            case FIDELITY -> new TonalPalette[] {
                    TonalPalette.fromHueAndChroma(h, c),
                    TonalPalette.fromHueAndChroma(h, Math.max(c - 32.0, c * 0.5)),
                    TonalPalette.fromHueAndChroma(h + 60, Math.max(c - 32.0, c * 0.5) + 8),
                    TonalPalette.fromHueAndChroma(h, c / 8.0),
                    TonalPalette.fromHueAndChroma(h, c / 8.0 + 4.0),
            };
            case CONTENT -> new TonalPalette[] {
                    TonalPalette.fromHueAndChroma(h, c),
                    TonalPalette.fromHueAndChroma(h, Math.max(c - 32.0, c * 0.5)),
                    TonalPalette.fromHueAndChroma(h - 50, Math.max(c - 32.0, c * 0.5) + 16),
                    TonalPalette.fromHueAndChroma(h, c / 8.0),
                    TonalPalette.fromHueAndChroma(h, c / 8.0 + 4.0),
            };
        };
    }

    // Hue rotation tables from Material's SchemeVibrant / SchemeExpressive: for a seed hue in
    // [HUES[i], HUES[i+1]), the palette hue is rotated by ROTATIONS[i].
    private static final double[] HUES = {0, 41, 61, 101, 131, 181, 251, 301, 360};
    private static final double[] VIBRANT_SECONDARY = {18, 15, 10, 12, 15, 18, 15, 12, 12};
    private static final double[] VIBRANT_TERTIARY = {35, 30, 20, 25, 30, 35, 30, 25, 25};
    private static final double[] EXPRESSIVE_SECONDARY = {45, 95, 45, 20, 45, 90, 45, 45, 45};
    private static final double[] EXPRESSIVE_TERTIARY = {120, 120, 20, 45, 20, 15, 20, 120, 120};

    private static double rotation(double hue, double[] rotations) {
        for (int i = 0; i < HUES.length - 1; i++) {
            if (hue >= HUES[i] && hue < HUES[i + 1]) {
                return rotations[i];
            }
        }
        return rotations[rotations.length - 1];
    }
}
