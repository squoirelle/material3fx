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
