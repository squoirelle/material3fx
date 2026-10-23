package dev.m3fx;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;

import java.lang.reflect.Method;

/**
 * The operating system's color scheme and accent color, from {@code Platform.getPreferences()}.
 * That API exists since JavaFX 22: it is reached by reflection so m3fx also runs on JavaFX 21,
 * where the system is reported as light with no accent color.
 */
final class SystemPreferences {

    private static final ObservableValue<?> COLOR_SCHEME = property("colorSchemeProperty");
    private static final ObservableValue<?> ACCENT_COLOR = property("accentColorProperty");

    private SystemPreferences() {
    }

    private static ObservableValue<?> property(String name) {
        try {
            Object preferences = Platform.class.getMethod("getPreferences").invoke(null);
            Method method = Class.forName("javafx.application.Platform$Preferences").getMethod(name);
            return (ObservableValue<?>) method.invoke(preferences);
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;   // JavaFX 21: no platform preferences
        }
    }

    /** Whether the platform preferences are available (JavaFX 22 and later). */
    static boolean isSupported() {
        return COLOR_SCHEME != null;
    }

    static boolean isDark() {
        // javafx.application.ColorScheme.DARK, compared by name (the enum is JavaFX 22+ too).
        return COLOR_SCHEME != null && COLOR_SCHEME.getValue() != null && "DARK".equals(COLOR_SCHEME.getValue().toString());
    }

    /** The system accent color, or null when unknown. */
    static Color accentColor() {
        return ACCENT_COLOR != null && ACCENT_COLOR.getValue() instanceof Color c ? c : null;
    }

    static void onColorSchemeChange(InvalidationListener listener) {
        if (COLOR_SCHEME != null) {
            COLOR_SCHEME.addListener(listener);
        }
    }

    static void onAccentColorChange(InvalidationListener listener) {
        if (ACCENT_COLOR != null) {
            ACCENT_COLOR.addListener(listener);
        }
    }
}
