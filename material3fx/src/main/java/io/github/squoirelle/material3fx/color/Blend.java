package io.github.squoirelle.material3fx.color;

/**
 * Color blending in HCT.
 */
public final class Blend {

    private Blend() {
    }

    /**
     * Shifts {@code designColor}'s hue towards {@code sourceColor}'s by at most 15°, keeping
     * chroma and tone, so a fixed semantic color (e.g. a red "danger") blends into a theme.
     */
    public static int harmonize(int designColor, int sourceColor) {
        Hct fromHct = Hct.fromInt(designColor);
        Hct toHct = Hct.fromInt(sourceColor);
        double differenceDegrees = MathUtils.differenceDegrees(fromHct.getHue(), toHct.getHue());
        double rotationDegrees = Math.min(differenceDegrees * 0.5, 15.0);
        double outputHue = MathUtils.sanitizeDegreesDouble(
                fromHct.getHue() + rotationDegrees * rotationDirection(fromHct.getHue(), toHct.getHue()));
        return Hct.from(outputHue, fromHct.getChroma(), fromHct.getTone()).toInt();
    }

    private static double rotationDirection(double from, double to) {
        double increasingDifference = MathUtils.sanitizeDegreesDouble(to - from);
        return increasingDifference <= 180.0 ? 1.0 : -1.0;
    }
}
