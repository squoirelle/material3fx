package dev.m3fx;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

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
 * first, over the session bus with a small built-in D-Bus client ({@link DBusConnection}: no
 * process, no native code), and its {@code SettingChanged} signals are followed. Without a session
 * bus or portal, or when the portal has no preference, JavaFX's values are used.
 */
final class SystemPreferences {

    private static final ObservableValue<?> FX_COLOR_SCHEME = fxProperty("colorSchemeProperty");
    private static final ObservableValue<?> FX_ACCENT_COLOR = fxProperty("accentColorProperty");
    private static final boolean LINUX = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("linux");
    private static final String PORTAL_DEST = "org.freedesktop.portal.Desktop";
    private static final String PORTAL_PATH = "/org/freedesktop/portal/desktop";

    private static final String SETTINGS = "org.freedesktop.portal.Settings";
    private static final String APPEARANCE = "org.freedesktop.appearance";
    private static final long READ_TIMEOUT_MILLIS = 2000;

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
        Object[] values = new Object[2];
        // A daemon thread with a deadline: a stuck bus must not block the caller (often the FX thread).
        DBusConnection[] connection = new DBusConnection[1];
        Thread reader = new Thread(() -> {
            try (DBusConnection bus = DBusConnection.openSessionBus()) {
                connection[0] = bus;
                values[0] = readSetting(bus, "color-scheme");
                values[1] = readSetting(bus, "accent-color");
            } catch (IOException | RuntimeException e) {
                // No session bus or no portal: JavaFX's values are used.
            }
        }, "m3fx-portal-read");
        reader.setDaemon(true);
        reader.start();
        try {
            reader.join(READ_TIMEOUT_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (reader.isAlive()) {
            closeQuietly(connection[0]);
            return;
        }
        portalScheme = colorScheme(values[0]);
        portalAccent = accentColor(values[1]);
    }

    /** The portal's ReadOne (or, on older portals, Read) value for an appearance setting; null if unset. */
    private static Object readSetting(DBusConnection bus, String key) throws IOException {
        DBusConnection.Message reply;
        try {
            reply = bus.call(PORTAL_DEST, PORTAL_PATH, SETTINGS, "ReadOne", APPEARANCE, key);
        } catch (IOException e) {
            try {
                reply = bus.call(PORTAL_DEST, PORTAL_PATH, SETTINGS, "Read", APPEARANCE, key);
            } catch (IOException notFound) {
                return null;   // the portal does not know this key
            }
        }
        return reply.body().isEmpty() ? null : reply.body().get(0);
    }

    private static void monitorPortal() {
        try (DBusConnection bus = DBusConnection.openSessionBus()) {
            bus.call("org.freedesktop.DBus", "/org/freedesktop/DBus", "org.freedesktop.DBus", "AddMatch",
                    "type='signal',interface='" + SETTINGS + "',member='SettingChanged',path='" + PORTAL_PATH
                            + "',arg0='" + APPEARANCE + "'");
            while (true) {
                DBusConnection.Message message = bus.readMessage();
                if (message.type() == DBusConnection.SIGNAL && "SettingChanged".equals(message.member())
                        && message.body().size() == 3) {
                    onSettingChanged(String.valueOf(message.body().get(0)), String.valueOf(message.body().get(1)),
                            message.body().get(2));
                }
            }
        } catch (IOException | RuntimeException e) {
            // Bus gone: values stay as last read.
        }
    }

    /** Handles a portal SettingChanged signal (package-private for tests). */
    static void onSettingChanged(String namespace, String key, Object value) {
        if (!APPEARANCE.equals(namespace)) {
            return;
        }
        if ("color-scheme".equals(key)) {
            Integer scheme = colorScheme(value);
            if (scheme != null) {
                synchronized (SystemPreferences.class) {
                    portalScheme = scheme;
                }
                notifyLater(SCHEME_LISTENERS);
            }
        } else if ("accent-color".equals(key)) {
            Color accent = accentColor(value);
            synchronized (SystemPreferences.class) {
                portalAccent = accent;
            }
            notifyLater(ACCENT_LISTENERS);
        }
    }

    private static void notifyLater(List<InvalidationListener> listeners) {
        Platform.runLater(() -> listeners.forEach(l -> l.invalidated(null)));
    }

    private static void closeQuietly(DBusConnection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException ignored) {
                // closing anyway
            }
        }
    }

    /** A decoded color-scheme value (uint32 0 / 1 / 2), or null when it is not one. */
    static Integer colorScheme(Object value) {
        return value instanceof Long l && l >= 0 && l <= 2 ? l.intValue() : null;
    }

    /** A decoded accent color (r, g, b) in [0, 1]; out-of-range components mean "unset" (null). */
    static Color accentColor(Object value) {
        if (!(value instanceof List<?> rgb) || rgb.size() != 3) {
            return null;
        }
        double[] c = new double[3];
        for (int i = 0; i < 3; i++) {
            if (!(rgb.get(i) instanceof Double d) || d < 0 || d > 1) {
                return null;
            }
            c[i] = d;
        }
        return Color.color(c[0], c[1], c[2]);
    }

    // For tests.
    static synchronized Integer portalScheme() {
        readPortalOnce();
        return portalScheme;
    }
}
