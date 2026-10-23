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
