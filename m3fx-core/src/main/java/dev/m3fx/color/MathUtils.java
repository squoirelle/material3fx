package dev.m3fx.color;

/**
 * Small numeric helpers used by the color science code.
 */
public final class MathUtils {

    private MathUtils() {
    }

    public static int signum(double num) {
        if (num < 0) {
            return -1;
        } else if (num == 0) {
            return 0;
        }
        return 1;
    }

    public static double lerp(double start, double stop, double amount) {
        return (1.0 - amount) * start + amount * stop;
    }

    public static int clampInt(int min, int max, int input) {
        return Math.min(max, Math.max(min, input));
    }

    public static double clampDouble(double min, double max, double input) {
        return Math.min(max, Math.max(min, input));
    }

    public static int sanitizeDegreesInt(int degrees) {
        degrees = degrees % 360;
        return degrees < 0 ? degrees + 360 : degrees;
    }

    public static double sanitizeDegreesDouble(double degrees) {
        degrees = degrees % 360.0;
        return degrees < 0 ? degrees + 360.0 : degrees;
    }

    /** Distance between two angles on the circle, in [0, 180]. */
    public static double differenceDegrees(double a, double b) {
        return 180.0 - Math.abs(Math.abs(a - b) - 180.0);
    }

    public static double[] matrixMultiply(double[] row, double[][] matrix) {
        double a = row[0] * matrix[0][0] + row[1] * matrix[0][1] + row[2] * matrix[0][2];
        double b = row[0] * matrix[1][0] + row[1] * matrix[1][1] + row[2] * matrix[1][2];
        double c = row[0] * matrix[2][0] + row[1] * matrix[2][1] + row[2] * matrix[2][2];
        return new double[] {a, b, c};
    }
}
