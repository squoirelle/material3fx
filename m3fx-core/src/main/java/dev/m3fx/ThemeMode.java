package dev.m3fx;

/** Light, dark, or following the operating system setting. */
public enum ThemeMode {
    LIGHT,
    DARK,
    /**
     * Follows the operating system's light / dark setting and switches live. On Linux it is read
     * from the XDG desktop portal (COSMIC, GNOME, KDE...); elsewhere from
     * {@code Platform.getPreferences()}, which needs JavaFX 22 (on JavaFX 21 it is light).
     */
    SYSTEM
}
