package io.github.squoirelle.material3fx.color;

/**
 * CAM16 color appearance model, the perceptual basis of {@link Hct}.
 */
public final class Cam16 {

    static final double[][] XYZ_TO_CAM16RGB = {
            {0.401288, 0.650173, -0.051461},
            {-0.250268, 1.204414, 0.045854},
            {-0.002079, 0.048952, 0.953127},
    };

    private final double hue;
    private final double chroma;
    private final double j;
    private final double jstar;
    private final double astar;
    private final double bstar;

    private Cam16(double hue, double chroma, double j, double jstar, double astar, double bstar) {
        this.hue = hue;
        this.chroma = chroma;
        this.j = j;
        this.jstar = jstar;
        this.astar = astar;
        this.bstar = bstar;
    }

    public double getHue() {
        return hue;
    }

    public double getChroma() {
        return chroma;
    }

    public double getJ() {
        return j;
    }

    /** Perceptual color difference (CAM16-UCS ΔE). */
    public double distance(Cam16 other) {
        double dJ = jstar - other.jstar;
        double dA = astar - other.astar;
        double dB = bstar - other.bstar;
        double dEPrime = Math.sqrt(dJ * dJ + dA * dA + dB * dB);
        return 1.41 * Math.pow(dEPrime, 0.63);
    }

    public static Cam16 fromInt(int argb) {
        return fromIntInViewingConditions(argb, ViewingConditions.DEFAULT);
    }

    static Cam16 fromIntInViewingConditions(int argb, ViewingConditions vc) {
        double redL = ColorUtils.linearized(ColorUtils.redFromArgb(argb));
        double greenL = ColorUtils.linearized(ColorUtils.greenFromArgb(argb));
        double blueL = ColorUtils.linearized(ColorUtils.blueFromArgb(argb));
        double x = 0.41233895 * redL + 0.35762064 * greenL + 0.18051042 * blueL;
        double y = 0.2126 * redL + 0.7152 * greenL + 0.0722 * blueL;
        double z = 0.01932141 * redL + 0.11916382 * greenL + 0.95034478 * blueL;

        double[][] matrix = XYZ_TO_CAM16RGB;
        double rT = x * matrix[0][0] + y * matrix[0][1] + z * matrix[0][2];
        double gT = x * matrix[1][0] + y * matrix[1][1] + z * matrix[1][2];
        double bT = x * matrix[2][0] + y * matrix[2][1] + z * matrix[2][2];

        double rD = vc.rgbD[0] * rT;
        double gD = vc.rgbD[1] * gT;
        double bD = vc.rgbD[2] * bT;

        double rAF = Math.pow(vc.fl * Math.abs(rD) / 100.0, 0.42);
        double gAF = Math.pow(vc.fl * Math.abs(gD) / 100.0, 0.42);
        double bAF = Math.pow(vc.fl * Math.abs(bD) / 100.0, 0.42);
        double rA = Math.signum(rD) * 400.0 * rAF / (rAF + 27.13);
        double gA = Math.signum(gD) * 400.0 * gAF / (gAF + 27.13);
        double bA = Math.signum(bD) * 400.0 * bAF / (bAF + 27.13);

        double a = (11.0 * rA + -12.0 * gA + bA) / 11.0;
        double b = (rA + gA - 2.0 * bA) / 9.0;
        double u = (20.0 * rA + 20.0 * gA + 21.0 * bA) / 20.0;
        double p2 = (40.0 * rA + 20.0 * gA + bA) / 20.0;

        double atanDegrees = Math.toDegrees(Math.atan2(b, a));
        double hue = atanDegrees < 0
                ? atanDegrees + 360.0
                : atanDegrees >= 360 ? atanDegrees - 360.0 : atanDegrees;
        double hueRadians = Math.toRadians(hue);

        double ac = p2 * vc.nbb;
        double j = 100.0 * Math.pow(ac / vc.aw, vc.c * vc.z);

        double huePrime = (hue < 20.14) ? hue + 360 : hue;
        double eHue = 0.25 * (Math.cos(Math.toRadians(huePrime) + 2.0) + 3.8);
        double p1 = 50000.0 / 13.0 * eHue * vc.nc * vc.ncb;
        double t = p1 * Math.hypot(a, b) / (u + 0.305);
        double alpha = Math.pow(1.64 - Math.pow(0.29, vc.n), 0.73) * Math.pow(t, 0.9);
        double c = alpha * Math.sqrt(j / 100.0);
        double m = c * vc.flRoot;

        double jstar = (1.0 + 100.0 * 0.007) * j / (1.0 + 0.007 * j);
        double mstar = 1.0 / 0.0228 * Math.log1p(0.0228 * m);
        double astar = mstar * Math.cos(hueRadians);
        double bstar = mstar * Math.sin(hueRadians);
        return new Cam16(hue, c, j, jstar, astar, bstar);
    }

    public static Cam16 fromJch(double j, double c, double h) {
        ViewingConditions vc = ViewingConditions.DEFAULT;
        double m = c * vc.flRoot;
        double hueRadians = Math.toRadians(h);
        double jstar = (1.0 + 100.0 * 0.007) * j / (1.0 + 0.007 * j);
        double mstar = 1.0 / 0.0228 * Math.log1p(0.0228 * m);
        double astar = mstar * Math.cos(hueRadians);
        double bstar = mstar * Math.sin(hueRadians);
        return new Cam16(h, c, j, jstar, astar, bstar);
    }

    /** Converts back to sRGB; out-of-gamut channels are clamped. */
    public int toInt() {
        ViewingConditions vc = ViewingConditions.DEFAULT;
        double alpha = (chroma == 0.0 || j == 0.0) ? 0.0 : chroma / Math.sqrt(j / 100.0);
        double t = Math.pow(alpha / Math.pow(1.64 - Math.pow(0.29, vc.n), 0.73), 1.0 / 0.9);
        double hRad = Math.toRadians(hue);

        double eHue = 0.25 * (Math.cos(hRad + 2.0) + 3.8);
        double ac = vc.aw * Math.pow(j / 100.0, 1.0 / vc.c / vc.z);
        double p1 = eHue * (50000.0 / 13.0) * vc.nc * vc.ncb;
        double p2 = ac / vc.nbb;

        double hSin = Math.sin(hRad);
        double hCos = Math.cos(hRad);

        double gamma = 23.0 * (p2 + 0.305) * t / (23.0 * p1 + 11.0 * t * hCos + 108.0 * t * hSin);
        double a = gamma * hCos;
        double b = gamma * hSin;
        double rA = (460.0 * p2 + 451.0 * a + 288.0 * b) / 1403.0;
        double gA = (460.0 * p2 - 891.0 * a - 261.0 * b) / 1403.0;
        double bA = (460.0 * p2 - 220.0 * a - 6300.0 * b) / 1403.0;

        double rCBase = Math.max(0, (27.13 * Math.abs(rA)) / (400.0 - Math.abs(rA)));
        double rC = Math.signum(rA) * (100.0 / vc.fl) * Math.pow(rCBase, 1.0 / 0.42);
        double gCBase = Math.max(0, (27.13 * Math.abs(gA)) / (400.0 - Math.abs(gA)));
        double gC = Math.signum(gA) * (100.0 / vc.fl) * Math.pow(gCBase, 1.0 / 0.42);
        double bCBase = Math.max(0, (27.13 * Math.abs(bA)) / (400.0 - Math.abs(bA)));
        double bC = Math.signum(bA) * (100.0 / vc.fl) * Math.pow(bCBase, 1.0 / 0.42);
        double rF = rC / vc.rgbD[0];
        double gF = gC / vc.rgbD[1];
        double bF = bC / vc.rgbD[2];

        double x = 1.86206786 * rF - 1.01125463 * gF + 0.14918677 * bF;
        double y = 0.38752654 * rF + 0.62144744 * gF - 0.00897398 * bF;
        double z = -0.01584150 * rF - 0.03412294 * gF + 1.04996444 * bF;
        return ColorUtils.argbFromXyz(x, y, z);
    }
}
