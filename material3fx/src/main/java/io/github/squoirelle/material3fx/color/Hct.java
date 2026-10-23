package io.github.squoirelle.material3fx.color;

import javafx.scene.paint.Color;

/**
 * HCT (hue, chroma, tone): Material's perceptual color space. Hue and chroma come from CAM16,
 * tone is L* from L*a*b*, so a tone difference of 50 always guarantees a 4.5:1 contrast ratio.
 * <p>
 * Requesting a hue/chroma/tone combination outside the sRGB gamut yields the closest
 * in-gamut color that keeps hue and tone, reducing chroma first.
 */
public final class Hct {

    private static final double CHROMA_SEARCH_ENDPOINT = 0.4;
    private static final double DE_MAX = 1.0;
    private static final double DL_MAX = 0.2;
    private static final double LIGHTNESS_SEARCH_ENDPOINT = 0.01;

    private final double hue;
    private final double chroma;
    private final double tone;
    private final int argb;

    private Hct(int argb) {
        Cam16 cam = Cam16.fromInt(argb);
        this.hue = cam.getHue();
        this.chroma = cam.getChroma();
        this.tone = ColorUtils.lstarFromArgb(argb);
        this.argb = argb;
    }

    public static Hct from(double hue, double chroma, double tone) {
        return new Hct(solveToInt(hue, chroma, tone));
    }

    public static Hct fromInt(int argb) {
        return new Hct(argb);
    }

    public static Hct fromColor(Color color) {
        return new Hct(ColorUtils.fromFx(color) | 0xFF000000);
    }

    public double getHue() {
        return hue;
    }

    public double getChroma() {
        return chroma;
    }

    public double getTone() {
        return tone;
    }

    public int toInt() {
        return argb;
    }

    public Color toColor() {
        return ColorUtils.toFx(argb);
    }

    public Hct withTone(double newTone) {
        return from(hue, chroma, newTone);
    }

    @Override
    public String toString() {
        return String.format("Hct[h=%.1f, c=%.1f, t=%.1f, %s]", hue, chroma, tone, ColorUtils.hex(argb));
    }

    /**
     * Finds the sRGB color closest to the requested HCT: bisects on chroma, and for each
     * candidate chroma bisects on CAM16 J until the clipped color has the requested tone.
     */
    static int solveToInt(double hue, double chroma, double tone) {
        if (chroma < 1.0 || Math.round(tone) <= 0.0 || Math.round(tone) >= 100.0) {
            return ColorUtils.argbFromLstar(tone);
        }
        hue = MathUtils.sanitizeDegreesDouble(hue);

        double high = chroma;
        double mid = chroma;
        double low = 0.0;
        boolean isFirstLoop = true;
        Cam16 answer = null;
        while (Math.abs(low - high) >= CHROMA_SEARCH_ENDPOINT) {
            Cam16 possibleAnswer = findCamByJ(hue, mid, tone);
            if (isFirstLoop) {
                if (possibleAnswer != null) {
                    return possibleAnswer.toInt();
                }
                isFirstLoop = false;
                mid = low + (high - low) / 2.0;
                continue;
            }
            if (possibleAnswer == null) {
                high = mid;
            } else {
                answer = possibleAnswer;
                low = mid;
            }
            mid = low + (high - low) / 2.0;
        }
        if (answer == null) {
            return ColorUtils.argbFromLstar(tone);
        }
        return answer.toInt();
    }

    private static Cam16 findCamByJ(double hue, double chroma, double tone) {
        double low = 0.0;
        double high = 100.0;
        double bestdL = 1000.0;
        double bestdE = 1000.0;
        Cam16 bestCam = null;
        while (Math.abs(low - high) > LIGHTNESS_SEARCH_ENDPOINT) {
            double mid = low + (high - low) / 2;
            Cam16 camBeforeClip = Cam16.fromJch(mid, chroma, hue);
            int clipped = camBeforeClip.toInt();
            double clippedLstar = ColorUtils.lstarFromArgb(clipped);
            double dL = Math.abs(tone - clippedLstar);

            if (dL < DL_MAX) {
                Cam16 camClipped = Cam16.fromInt(clipped);
                double dE = camClipped.distance(Cam16.fromJch(camClipped.getJ(), camClipped.getChroma(), hue));
                if (dE <= DE_MAX && dE <= bestdE) {
                    bestdL = dL;
                    bestdE = dE;
                    bestCam = camClipped;
                }
            }

            if (bestdL == 0 && bestdE == 0) {
                break;
            }

            if (clippedLstar < tone) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return bestCam;
    }
}
