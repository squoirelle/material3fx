package dev.m3fx.color;

import javafx.scene.paint.Color;

/**
 * Conversions between ARGB integers, linear RGB, XYZ and L*a*b* lightness.
 * <p>
 * Colors are handled as packed {@code 0xAARRGGBB} integers internally, which is what the
 * Material color utilities use, and converted to JavaFX {@link Color} at the edges.
 */
public final class ColorUtils {

    static final double[][] SRGB_TO_XYZ = {
            {0.41233895, 0.35762064, 0.18051042},
            {0.2126, 0.7152, 0.0722},
            {0.01932141, 0.11916382, 0.95034478},
    };

    static final double[][] XYZ_TO_SRGB = {
            {3.2413774792388685, -1.5376652402851851, -0.49885366846268053},
            {-0.9691452513005321, 1.8758853451067872, 0.04156585616912061},
            {0.05562093689691305, -0.20395524564742123, 1.0571799111220335},
    };

    static final double[] WHITE_POINT_D65 = {95.047, 100.0, 108.883};

    private ColorUtils() {
    }

    public static int argbFromRgb(int red, int green, int blue) {
        return (255 << 24) | ((red & 255) << 16) | ((green & 255) << 8) | (blue & 255);
    }

    public static int alphaFromArgb(int argb) {
        return (argb >> 24) & 255;
    }

    public static int redFromArgb(int argb) {
        return (argb >> 16) & 255;
    }

    public static int greenFromArgb(int argb) {
        return (argb >> 8) & 255;
    }

    public static int blueFromArgb(int argb) {
        return argb & 255;
    }

    public static int argbFromXyz(double x, double y, double z) {
        double[][] matrix = XYZ_TO_SRGB;
        double linearR = matrix[0][0] * x + matrix[0][1] * y + matrix[0][2] * z;
        double linearG = matrix[1][0] * x + matrix[1][1] * y + matrix[1][2] * z;
        double linearB = matrix[2][0] * x + matrix[2][1] * y + matrix[2][2] * z;
        return argbFromRgb(delinearized(linearR), delinearized(linearG), delinearized(linearB));
    }

    public static double[] xyzFromArgb(int argb) {
        double r = linearized(redFromArgb(argb));
        double g = linearized(greenFromArgb(argb));
        double b = linearized(blueFromArgb(argb));
        return MathUtils.matrixMultiply(new double[] {r, g, b}, SRGB_TO_XYZ);
    }

    public static int argbFromLstar(double lstar) {
        double y = yFromLstar(lstar);
        int component = delinearized(y);
        return argbFromRgb(component, component, component);
    }

    public static double lstarFromArgb(int argb) {
        double y = xyzFromArgb(argb)[1];
        return 116.0 * labF(y / 100.0) - 16.0;
    }

    public static double yFromLstar(double lstar) {
        return 100.0 * labInvf((lstar + 16.0) / 116.0);
    }

    public static double lstarFromY(double y) {
        return labF(y / 100.0) * 116.0 - 16.0;
    }

    /** sRGB component (0..255) to linear RGB (0..100). */
    public static double linearized(int rgbComponent) {
        double normalized = rgbComponent / 255.0;
        if (normalized <= 0.040449936) {
            return normalized / 12.92 * 100.0;
        }
        return Math.pow((normalized + 0.055) / 1.055, 2.4) * 100.0;
    }

    /** Linear RGB (0..100) to an sRGB component (0..255), clamped. */
    public static int delinearized(double rgbComponent) {
        double normalized = rgbComponent / 100.0;
        double delinearized;
        if (normalized <= 0.0031308) {
            delinearized = normalized * 12.92;
        } else {
            delinearized = 1.055 * Math.pow(normalized, 1.0 / 2.4) - 0.055;
        }
        return MathUtils.clampInt(0, 255, (int) Math.round(delinearized * 255.0));
    }

    static double labF(double t) {
        double e = 216.0 / 24389.0;
        double kappa = 24389.0 / 27.0;
        if (t > e) {
            return Math.cbrt(t);
        }
        return (kappa * t + 16) / 116;
    }

    static double labInvf(double ft) {
        double e = 216.0 / 24389.0;
        double kappa = 24389.0 / 27.0;
        double ft3 = ft * ft * ft;
        if (ft3 > e) {
            return ft3;
        }
        return (116 * ft - 16) / kappa;
    }

    public static Color toFx(int argb) {
        return Color.rgb(redFromArgb(argb), greenFromArgb(argb), blueFromArgb(argb), alphaFromArgb(argb) / 255.0);
    }

    public static int fromFx(Color color) {
        int a = (int) Math.round(color.getOpacity() * 255);
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** {@code #rrggbb} hex string of the RGB part of an ARGB color. */
    public static String hex(int argb) {
        return String.format("#%06x", argb & 0xFFFFFF);
    }
}
