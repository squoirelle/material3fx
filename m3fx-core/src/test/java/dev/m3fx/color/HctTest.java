package dev.m3fx.color;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Reference values from Google's material-color-utilities test suite.
class HctTest {

    @Test
    void convertsPrimaries() {
        assertHct(0xFFFF0000, 27.408, 113.357, 53.237);
        assertHct(0xFF00FF00, 142.139, 108.410, 87.737);
        assertHct(0xFF0000FF, 282.788, 87.230, 32.302);
        assertHct(0xFFFFFFFF, 209.492, 2.869, 100.0);
        assertEquals(0.0, Hct.fromInt(0xFF000000).getTone(), 0.001);
    }

    @Test
    void roundTripsInGamutColors() {
        for (int argb : new int[] {0xFF6750A4, 0xFF006A6A, 0xFFB3261E, 0xFF7D5260, 0xFF3A6A1A}) {
            Hct hct = Hct.fromInt(argb);
            int solved = Hct.from(hct.getHue(), hct.getChroma(), hct.getTone()).toInt();
            assertClose(argb, solved, 2);
        }
    }

    @Test
    void outOfGamutKeepsHueAndTone() {
        Hct hct = Hct.from(120, 200, 50);
        assertEquals(50, hct.getTone(), 0.5);
        assertTrue(MathUtils.differenceDegrees(120, hct.getHue()) < 2.0, hct.toString());
        assertTrue(hct.getChroma() < 200);
    }

    @Test
    void tonalPaletteOfBlue() {
        TonalPalette palette = TonalPalette.fromHct(Hct.fromInt(0xFF0000FF));
        assertClose(0xFFFFFFFF, palette.tone(100), 0);
        assertClose(0xFFF1EFFF, palette.tone(95), 2);
        assertClose(0xFFE0E0FF, palette.tone(90), 2);
        assertClose(0xFFBEC2FF, palette.tone(80), 2);
        assertClose(0xFF9DA3FF, palette.tone(70), 2);
        assertClose(0xFF7C84FF, palette.tone(60), 2);
        assertClose(0xFF5A64FF, palette.tone(50), 2);
        assertClose(0xFF343DFF, palette.tone(40), 2);
        assertClose(0xFF0000EF, palette.tone(30), 3);
        assertClose(0xFF0001AC, palette.tone(20), 3);
        assertClose(0xFF00006E, palette.tone(10), 3);
        assertClose(0xFF000000, palette.tone(0), 0);
    }

    @Test
    void toneMatchesLstar() {
        TonalPalette palette = TonalPalette.fromHueAndChroma(260, 36);
        for (int tone = 5; tone <= 95; tone += 5) {
            assertEquals(tone, ColorUtils.lstarFromArgb(palette.tone(tone)), 0.6, "tone " + tone);
        }
    }

    private static void assertHct(int argb, double hue, double chroma, double tone) {
        Hct hct = Hct.fromInt(argb);
        assertEquals(hue, hct.getHue(), 0.001);
        assertEquals(chroma, hct.getChroma(), 0.001);
        assertEquals(tone, hct.getTone(), 0.01);
    }

    private static void assertClose(int expected, int actual, int tolerance) {
        String message = ColorUtils.hex(expected) + " vs " + ColorUtils.hex(actual);
        assertTrue(Math.abs(ColorUtils.redFromArgb(expected) - ColorUtils.redFromArgb(actual)) <= tolerance, message);
        assertTrue(Math.abs(ColorUtils.greenFromArgb(expected) - ColorUtils.greenFromArgb(actual)) <= tolerance, message);
        assertTrue(Math.abs(ColorUtils.blueFromArgb(expected) - ColorUtils.blueFromArgb(actual)) <= tolerance, message);
    }
}
