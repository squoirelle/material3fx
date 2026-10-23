package dev.m3fx;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The operating system's color scheme (light / dark) and accent color.
 * <p>
 * <b>JavaFX.</b> {@code Platform.getPreferences()} (JavaFX 22+) is reached by reflection, so m3fx
 * also runs on JavaFX 21, where the system is reported as light with no accent color.
 * <p>
 * <b>Linux.</b> JavaFX derives light / dark from the GTK theme of the X11 session, which does not
 * follow the desktop setting on several desktops (COSMIC, through XWayland, reports a light GTK
 * theme while the desktop is dark). On Linux the XDG desktop portal
 * ({@code org.freedesktop.appearance}), which COSMIC, GNOME and KDE implement, is therefore asked
 * first, with the {@code gdbus} tool; changes are followed with {@code gdbus monitor}. Without a
 * portal or {@code gdbus}, or when the portal has no preference, JavaFX's values are used.
 */
final class SystemPreferences {

    private static final ObservableValue<?> FX_COLOR_SCHEME = fxProperty("colorSchemeProperty");
    private static final ObservableValue<?> FX_ACCENT_COLOR = fxProperty("accentColorProperty");
    private static final boolean LINUX = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("linux");
    private static final String PORTAL_DEST = "org.freedesktop.portal.Desktop";
    private static final String PORTAL_PATH = "/org/freedesktop/portal/desktop";

    private static final Pattern UINT32 = Pattern.compile("uint32 (\\d+)");
    private static final Pattern RGB = Pattern.compile("\\(([-\\d.eE+]+), ([-\\d.eE+]+), ([-\\d.eE+]+)\\)");

    private static final List<InvalidationListener> SCHEME_LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<InvalidationListener> ACCENT_LISTENERS = new CopyOnWriteArrayList<>();

    // Portal values: color-scheme 0 = no preference, 1 = dark, 2 = light; null when unknown.
    private static Integer portalScheme;
    private static Color portalAccent;
    private static boolean portalRead;
    private static boolean watching;

    private SystemPreferences() {
    }

    private static ObservableValue<?> fxProperty(String name) {
        try {
            Object preferences = Platform.class.getMethod("getPreferences").invoke(null);
            Method method = Class.forName("javafx.application.Platform$Preferences").getMethod(name);
            return (ObservableValue<?>) method.invoke(preferences);
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;   // JavaFX 21: no platform preferences
        }
    }

    static synchronized boolean isDark() {
        readPortalOnce();
        if (portalScheme != null && portalScheme != 0) {
            return portalScheme == 1;
        }
        // javafx.application.ColorScheme.DARK, compared by name (the enum is JavaFX 22+ too).
        return FX_COLOR_SCHEME != null && FX_COLOR_SCHEME.getValue() != null && "DARK".equals(FX_COLOR_SCHEME.getValue().toString());
    }

    /** The system accent color, or null when unknown. */
    static synchronized Color accentColor() {
        readPortalOnce();
        if (portalAccent != null) {
            return portalAccent;
        }
        return FX_ACCENT_COLOR != null && FX_ACCENT_COLOR.getValue() instanceof Color c ? c : null;
    }

    static void onColorSchemeChange(InvalidationListener listener) {
        SCHEME_LISTENERS.add(listener);
        if (FX_COLOR_SCHEME != null) {
            FX_COLOR_SCHEME.addListener(listener);
        }
    }

    static void onAccentColorChange(InvalidationListener listener) {
        ACCENT_LISTENERS.add(listener);
        if (FX_ACCENT_COLOR != null) {
            FX_ACCENT_COLOR.addListener(listener);
        }
    }

    /** Starts following portal changes (Linux), once. Called when a theme actually uses system values. */
    static synchronized void watch() {
        readPortalOnce();
        if (watching || portalScheme == null) {
            return;
        }
        watching = true;
        Thread thread = new Thread(SystemPreferences::monitorPortal, "m3fx-portal-monitor");
        thread.setDaemon(true);
        thread.start();
    }

    private static void readPortalOnce() {
        if (portalRead || !LINUX) {
            return;
        }
        portalRead = true;
        String scheme = readSetting("color-scheme");
        portalScheme = scheme == null ? null : parseColorScheme(scheme);
        String accent = readSetting("accent-color");
        portalAccent = accent == null ? null : parseAccentColor(accent);
    }

    /** Output of the portal's ReadOne (or, on older portals, Read) for an appearance setting; null on failure. */
    private static String readSetting(String key) {
        String result = gdbusCall("org.freedesktop.portal.Settings.ReadOne", key);
        return result != null ? result : gdbusCall("org.freedesktop.portal.Settings.Read", key);
    }

    private static String gdbusCall(String method, String key) {
        try {
            Process process = new ProcessBuilder("gdbus", "call", "--session", "--timeout", "2", "--dest", PORTAL_DEST,
                    "--object-path", PORTAL_PATH, "--method", method, "org.freedesktop.appearance", key)
                    .redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!process.waitFor(3, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return null;
            }
            return process.exitValue() == 0 ? output.trim() : null;
        } catch (IOException e) {
            return null;   // no gdbus
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private static void monitorPortal() {
        try {
            Process process = new ProcessBuilder("gdbus", "monitor", "--session", "--dest", PORTAL_DEST,
                    "--object-path", PORTAL_PATH).redirectErrorStream(true).start();
            Runtime.getRuntime().addShutdownHook(new Thread(process::destroy));
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    onMonitorLine(line);
                }
            }
        } catch (IOException e) {
            // No monitor: values stay as read at startup.
        }
    }

    /** Handles one line of {@code gdbus monitor} output (package-private for tests). */
    static void onMonitorLine(String line) {
        if (!line.contains("SettingChanged") || !line.contains("'org.freedesktop.appearance'")) {
            return;
        }
        if (line.contains("'color-scheme'")) {
            Integer scheme = parseColorScheme(line);
            if (scheme != null) {
                synchronized (SystemPreferences.class) {
                    portalScheme = scheme;
                }
                notifyLater(SCHEME_LISTENERS);
            }
        } else if (line.contains("'accent-color'")) {
            Color accent = parseAccentColor(line);
            synchronized (SystemPreferences.class) {
                portalAccent = accent;
            }
            notifyLater(ACCENT_LISTENERS);
        }
    }

    private static void notifyLater(List<InvalidationListener> listeners) {
        Platform.runLater(() -> listeners.forEach(l -> l.invalidated(null)));
    }

    /** Parses a portal color-scheme value ("(<uint32 1>,)", "<<uint32 1>>", or a SettingChanged line). */
    static Integer parseColorScheme(String text) {
        Matcher m = UINT32.matcher(text);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
    }

    /** Parses a portal accent color "(r, g, b)" in [0, 1]; out-of-range components mean "unset" (null). */
    static Color parseAccentColor(String text) {
        Matcher m = RGB.matcher(text);
        if (!m.find()) {
            return null;
        }
        try {
            double r = Double.parseDouble(m.group(1));
            double g = Double.parseDouble(m.group(2));
            double b = Double.parseDouble(m.group(3));
            if (r < 0 || r > 1 || g < 0 || g > 1 || b < 0 || b > 1) {
                return null;
            }
            return Color.color(r, g, b);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // For tests.
    static synchronized Integer portalScheme() {
        readPortalOnce();
        return portalScheme;
    }
}
