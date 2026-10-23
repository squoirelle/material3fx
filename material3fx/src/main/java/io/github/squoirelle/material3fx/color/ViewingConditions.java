/*
 * Copyright 2021 Google LLC
 * Modifications copyright 2026 Lilian Stefanuto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Ported from material-color-utilities
 * (https://github.com/material-foundation/material-color-utilities), with changes.
 */
package io.github.squoirelle.material3fx.color;

/**
 * CAM16 viewing conditions. Only the default (sRGB, average surround, mid-grey background)
 * conditions are used by Material, but the factory is kept general.
 */
public final class ViewingConditions {

    public static final ViewingConditions DEFAULT = make(
            ColorUtils.WHITE_POINT_D65,
            200.0 / Math.PI * ColorUtils.yFromLstar(50.0) / 100.0,
            50.0,
            2.0,
            false);

    final double aw;
    final double nbb;
    final double ncb;
    final double c;
    final double nc;
    final double n;
    final double[] rgbD;
    final double fl;
    final double flRoot;
    final double z;

    private ViewingConditions(double n, double aw, double nbb, double ncb, double c, double nc,
                              double[] rgbD, double fl, double flRoot, double z) {
        this.n = n;
        this.aw = aw;
        this.nbb = nbb;
        this.ncb = ncb;
        this.c = c;
        this.nc = nc;
        this.rgbD = rgbD;
        this.fl = fl;
        this.flRoot = flRoot;
        this.z = z;
    }

    public static ViewingConditions make(double[] whitePoint, double adaptingLuminance, double backgroundLstar,
                                         double surround, boolean discountingIlluminant) {
        backgroundLstar = Math.max(0.1, backgroundLstar);
        double[][] matrix = Cam16.XYZ_TO_CAM16RGB;
        double[] xyz = whitePoint;
        double rW = xyz[0] * matrix[0][0] + xyz[1] * matrix[0][1] + xyz[2] * matrix[0][2];
        double gW = xyz[0] * matrix[1][0] + xyz[1] * matrix[1][1] + xyz[2] * matrix[1][2];
        double bW = xyz[0] * matrix[2][0] + xyz[1] * matrix[2][1] + xyz[2] * matrix[2][2];
        double f = 0.8 + (surround / 10.0);
        double c = (f >= 0.9)
                ? MathUtils.lerp(0.59, 0.69, ((f - 0.9) * 10.0))
                : MathUtils.lerp(0.525, 0.59, ((f - 0.8) * 10.0));
        double d = discountingIlluminant
                ? 1.0
                : f * (1.0 - ((1.0 / 3.6) * Math.exp((-adaptingLuminance - 42.0) / 92.0)));
        d = MathUtils.clampDouble(0.0, 1.0, d);
        double nc = f;
        double[] rgbD = {
                d * (100.0 / rW) + 1.0 - d,
                d * (100.0 / gW) + 1.0 - d,
                d * (100.0 / bW) + 1.0 - d,
        };
        double k = 1.0 / (5.0 * adaptingLuminance + 1.0);
        double k4 = k * k * k * k;
        double k4F = 1.0 - k4;
        double fl = (k4 * adaptingLuminance) + (0.1 * k4F * k4F * Math.cbrt(5.0 * adaptingLuminance));
        double n = (ColorUtils.yFromLstar(backgroundLstar) / whitePoint[1]);
        double z = 1.48 + Math.sqrt(n);
        double nbb = 0.725 / Math.pow(n, 0.2);
        double ncb = nbb;
        double[] rgbAFactors = {
                Math.pow(fl * rgbD[0] * rW / 100.0, 0.42),
                Math.pow(fl * rgbD[1] * gW / 100.0, 0.42),
                Math.pow(fl * rgbD[2] * bW / 100.0, 0.42),
        };
        double[] rgbA = {
                (400.0 * rgbAFactors[0]) / (rgbAFactors[0] + 27.13),
                (400.0 * rgbAFactors[1]) / (rgbAFactors[1] + 27.13),
                (400.0 * rgbAFactors[2]) / (rgbAFactors[2] + 27.13),
        };
        double aw = ((2.0 * rgbA[0]) + rgbA[1] + (0.05 * rgbA[2])) * nbb;
        return new ViewingConditions(n, aw, nbb, ncb, c, nc, rgbD, fl, Math.pow(fl, 0.25), z);
    }
}
