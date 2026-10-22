package dev.m3fx.color;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A hue and chroma, from which any tone (0 = black, 100 = white) can be requested.
 */
public final class TonalPalette {

    private final double hue;
    private final double chroma;
    private final Map<Integer, Integer> cache = new ConcurrentHashMap<>();

    private TonalPalette(double hue, double chroma) {
        this.hue = hue;
        this.chroma = chroma;
    }

    public static TonalPalette fromHueAndChroma(double hue, double chroma) {
        return new TonalPalette(MathUtils.sanitizeDegreesDouble(hue), chroma);
    }

    public static TonalPalette fromHct(Hct hct) {
        return new TonalPalette(hct.getHue(), hct.getChroma());
    }

    public double getHue() {
        return hue;
    }

    public double getChroma() {
        return chroma;
    }

    /** ARGB color at the given tone. */
    public int tone(int tone) {
        return cache.computeIfAbsent(tone, t -> Hct.solveToInt(hue, chroma, t));
    }
}
