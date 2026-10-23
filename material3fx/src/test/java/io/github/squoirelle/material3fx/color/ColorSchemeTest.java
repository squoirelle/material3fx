package io.github.squoirelle.material3fx.color;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColorSchemeTest {

    private static final Color BASELINE = Color.web("#6750A4");

    @Test
    void everyRoleHasItsTone() {
        for (boolean dark : new boolean[] {false, true}) {
            ColorScheme scheme = ColorScheme.of(BASELINE, dark);
            for (ColorRole role : ColorRole.values()) {
                double tone = ColorUtils.lstarFromArgb(scheme.getArgb(role));
                assertEquals(role.tone(dark, 0), tone, 1.0, role + " dark=" + dark);
            }
        }
    }

    @Test
    void onColorsMeetContrastRequirements() {
        for (Variant variant : Variant.values()) {
            for (boolean dark : new boolean[] {false, true}) {
                ColorScheme s = ColorScheme.builder(BASELINE).variant(variant).dark(dark).build();
                assertContrast(s, ColorRole.ON_PRIMARY, ColorRole.PRIMARY, 4.5);
                assertContrast(s, ColorRole.ON_PRIMARY_CONTAINER, ColorRole.PRIMARY_CONTAINER, 4.5);
                assertContrast(s, ColorRole.ON_SURFACE, ColorRole.SURFACE, 7.0);
                assertContrast(s, ColorRole.ON_SURFACE_VARIANT, ColorRole.SURFACE_CONTAINER_HIGHEST, 4.5);
                assertContrast(s, ColorRole.ON_ERROR_CONTAINER, ColorRole.ERROR_CONTAINER, 4.5);
            }
        }
    }

    @Test
    void highContrastIncreasesContrast() {
        ColorScheme standard = ColorScheme.builder(BASELINE).build();
        ColorScheme high = ColorScheme.builder(BASELINE).contrast(1).build();
        assertTrue(ratio(high, ColorRole.OUTLINE, ColorRole.SURFACE) > ratio(standard, ColorRole.OUTLINE, ColorRole.SURFACE));
        assertTrue(ratio(high, ColorRole.ON_PRIMARY_CONTAINER, ColorRole.PRIMARY_CONTAINER)
                > ratio(standard, ColorRole.ON_PRIMARY_CONTAINER, ColorRole.PRIMARY_CONTAINER));
    }

    @Test
    void primaryKeepsSeedHue() {
        ColorScheme scheme = ColorScheme.of(BASELINE, false);
        double seedHue = Hct.fromColor(BASELINE).getHue();
        double primaryHue = Hct.fromInt(scheme.getArgb(ColorRole.PRIMARY)).getHue();
        assertTrue(MathUtils.differenceDegrees(seedHue, primaryHue) < 3);
    }

    @Test
    void harmonizeRotatesTowardsSourceByAtMost15Degrees() {
        int red = 0xFFFF0000;
        int blue = 0xFF0000FF;
        double before = Hct.fromInt(red).getHue();
        double after = Hct.fromInt(Blend.harmonize(red, blue)).getHue();
        double moved = MathUtils.differenceDegrees(before, after);
        assertTrue(moved > 10 && moved <= 15.5, "moved " + moved);
    }

    private static void assertContrast(ColorScheme s, ColorRole fg, ColorRole bg, double min) {
        double ratio = ratio(s, fg, bg);
        assertTrue(ratio >= min - 0.1, fg + " on " + bg + " " + s.variant() + " dark=" + s.isDark() + " ratio=" + ratio);
    }

    private static double ratio(ColorScheme s, ColorRole fg, ColorRole bg) {
        double y1 = ColorUtils.yFromLstar(ColorUtils.lstarFromArgb(s.getArgb(fg)));
        double y2 = ColorUtils.yFromLstar(ColorUtils.lstarFromArgb(s.getArgb(bg)));
        return (Math.max(y1, y2) + 5) / (Math.min(y1, y2) + 5);
    }
}
