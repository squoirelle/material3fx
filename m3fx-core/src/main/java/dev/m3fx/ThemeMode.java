package dev.m3fx;

/** Light, dark, or following the operating system setting. */
public enum ThemeMode {
    LIGHT,
    DARK,
    /** Follows {@code Platform.getPreferences().getColorScheme()} and switches live. */
    SYSTEM
}
