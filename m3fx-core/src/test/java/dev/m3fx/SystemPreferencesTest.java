package dev.m3fx;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SystemPreferencesTest {

    @Test
    void parsesPortalOutputs() {
        assertEquals(1, SystemPreferences.parseColorScheme("(<uint32 1>,)"), "ReadOne");
        assertEquals(2, SystemPreferences.parseColorScheme("(<<uint32 2>>,)"), "Read (older portals)");
        assertEquals(1, SystemPreferences.parseColorScheme(
                "/org/freedesktop/portal/desktop: org.freedesktop.portal.Settings.SettingChanged "
                        + "('org.freedesktop.appearance', 'color-scheme', <uint32 1>)"), "monitor line");
        Color accent = SystemPreferences.parseAccentColor("(<(0.38823533058166504, 0.81568634510040283, 0.87450987100601196)>,)");
        assertEquals(0.388, accent.getRed(), 0.001);
        assertEquals(0.816, accent.getGreen(), 0.001);
        assertNull(SystemPreferences.parseAccentColor("(<(-1.0, -1.0, -1.0)>,)"), "out of range means unset");
        assertNull(SystemPreferences.parseColorScheme("Error: no such method"));
    }

    /** On a Linux desktop with a portal, the theme must follow the portal, not JavaFX's GTK guess. */
    @Test
    void followsThePortalWhenThereIsOne() throws Exception {
        String direct = gdbus("color-scheme");
        Assumptions.assumeTrue(direct != null, "no XDG desktop portal here");
        Integer expected = SystemPreferences.parseColorScheme(direct);
        Assumptions.assumeTrue(expected != null && expected != 0, "portal has no light / dark preference");
        FxTestSupport.start();
        assertEquals(expected == 1, FxTestSupport.onFx(SystemPreferences::isDark), "portal says " + direct);
    }

    private static String gdbus(String key) throws Exception {
        try {
            Process p = new ProcessBuilder("gdbus", "call", "--session", "--timeout", "2", "--dest",
                    "org.freedesktop.portal.Desktop", "--object-path", "/org/freedesktop/portal/desktop", "--method",
                    "org.freedesktop.portal.Settings.ReadOne", "org.freedesktop.appearance", key).start();
            String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return p.waitFor(3, TimeUnit.SECONDS) && p.exitValue() == 0 ? out : null;
        } catch (java.io.IOException e) {
            return null;
        }
    }
}
