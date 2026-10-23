package dev.m3fx;

/** Light, dark, or following the operating system setting. */
public enum ThemeMode {
    LIGHT,
    DARK,
    /**
     * Follows the operating system's light / dark setting and switches live. Needs JavaFX 22 or
     * later ({@code Platform.getPreferences()}); on JavaFX 21 it is light.
     */
    SYSTEM
}
