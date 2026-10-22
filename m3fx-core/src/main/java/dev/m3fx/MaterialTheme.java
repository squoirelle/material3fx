package dev.m3fx;

import dev.m3fx.color.ColorRole;
import dev.m3fx.color.ColorScheme;
import dev.m3fx.color.ColorUtils;
import dev.m3fx.color.Variant;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Window;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * Entry point of m3fx: generates a Material 3 color scheme and applies it, together with the
 * Material stylesheet, to scenes.
 *
 * <pre>{@code
 * MaterialTheme theme = new MaterialTheme(Color.web("#6750A4"));
 * theme.setMode(ThemeMode.SYSTEM);
 * theme.setDensity(Density.COMPACT);
 * theme.applyGlobally();   // every window, including dialogs and popups
 * }</pre>
 * <p>
 * Every property can change at runtime; all themed scenes update immediately.
 * Colors are exposed to CSS as looked-up colors named after their role
 * ({@code -md-sys-color-primary}, {@code -md-sys-color-on-surface-variant}...), plus translucent
 * variants for state layers and disabled states ({@code -md-sys-color-on-surface-a12} is
 * on-surface at 12% opacity). Available alpha suffixes: a08, a10, a12, a16, a32, a38.
 */
public final class MaterialTheme {

    /** Material's baseline seed color. */
    public static final Color BASELINE = Color.web("#6750A4");

    private static final int[] ALPHAS = {8, 10, 12, 16, 32, 38};
    private static final String THEME_KEY = MaterialTheme.class.getName();
    private static final String STYLESHEET = Objects.requireNonNull(
            MaterialTheme.class.getResource("m3fx.css"), "m3fx.css missing").toExternalForm();
    private static boolean fontsLoaded;

    private final ObjectProperty<Color> seedColor = new SimpleObjectProperty<>(this, "seedColor", BASELINE);
    private final ObjectProperty<ThemeMode> mode = new SimpleObjectProperty<>(this, "mode", ThemeMode.LIGHT);
    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant", Variant.TONAL_SPOT);
    private final DoubleProperty contrast = new SimpleDoubleProperty(this, "contrast", 0);
    private final ObjectProperty<Density> density = new SimpleObjectProperty<>(this, "density", Density.DEFAULT);
    private final BooleanProperty useSystemAccent = new SimpleBooleanProperty(this, "useSystemAccent", false);
    private final StringProperty fontFamily = new SimpleStringProperty(this, "fontFamily", "Roboto");
    private final ObservableMap<String, Color> customColors = FXCollections.observableHashMap();
    private final BooleanProperty harmonizeCustomColors = new SimpleBooleanProperty(this, "harmonizeCustomColors", true);

    private final ReadOnlyObjectWrapper<ColorScheme> scheme = new ReadOnlyObjectWrapper<>(this, "scheme");
    private final ReadOnlyBooleanWrapper dark = new ReadOnlyBooleanWrapper(this, "dark");

    private final Map<Scene, String> scenes = new WeakHashMap<>();
    private final Map<Scene, ChangeListener<Parent>> rootListeners = new WeakHashMap<>();
    private String tokensStylesheet;
    private ListChangeListener<Window> windowListener;

    public MaterialTheme() {
        this(BASELINE);
    }

    public MaterialTheme(Color seed) {
        loadFonts();
        seedColor.set(Objects.requireNonNull(seed));
        InvalidationListener rebuild = o -> rebuild();
        seedColor.addListener(rebuild);
        mode.addListener(rebuild);
        variant.addListener(rebuild);
        contrast.addListener(rebuild);
        useSystemAccent.addListener(rebuild);
        fontFamily.addListener(rebuild);
        harmonizeCustomColors.addListener(rebuild);
        customColors.addListener((javafx.collections.MapChangeListener<String, Color>) c -> rebuild());
        density.addListener(o -> scenes.keySet().forEach(this::updateRootClasses));
        Platform.Preferences preferences = Platform.getPreferences();
        preferences.colorSchemeProperty().addListener(o -> {
            if (getMode() == ThemeMode.SYSTEM) {
                rebuild();
            }
        });
        preferences.accentColorProperty().addListener(o -> {
            if (isUseSystemAccent()) {
                rebuild();
            }
        });
        rebuild();
    }

    /** Registers the bundled Roboto and Material Icons fonts. Called automatically. */
    public static synchronized void loadFonts() {
        if (fontsLoaded) {
            return;
        }
        fontsLoaded = true;
        for (String font : new String[] {"Roboto-400.ttf", "Roboto-500.ttf", "Roboto-700.ttf",
                "MaterialIcons-Regular.ttf", "MaterialIconsOutlined-Regular.otf"}) {
            try (var in = MaterialTheme.class.getResourceAsStream("fonts/" + font)) {
                if (in != null) {
                    Font.loadFont(in, 14);
                }
            } catch (java.io.IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        }
    }

    /** URL of the m3fx component stylesheet, for manual use. {@link #apply(Scene)} adds it for you. */
    public static String stylesheet() {
        return STYLESHEET;
    }

    /** The theme applied to the scene of {@code node}, or null. */
    public static MaterialTheme of(Node node) {
        return node.getScene() == null ? null : of(node.getScene());
    }

    /** The theme applied to {@code scene}, or null. */
    public static MaterialTheme of(Scene scene) {
        Object theme = scene.getRoot() == null ? null : scene.getRoot().getProperties().get(THEME_KEY);
        return theme instanceof MaterialTheme t ? t : null;
    }

    /** Applies the theme to a scene. */
    public void apply(Scene scene) {
        if (scenes.containsKey(scene)) {
            return;
        }
        MaterialTheme previous = of(scene);
        if (previous != null && previous != this) {
            previous.remove(scene);
        }
        scene.getStylesheets().removeIf(s -> s.equals(STYLESHEET));
        scene.getStylesheets().add(0, tokensStylesheet);
        scene.getStylesheets().add(1, STYLESHEET);
        scenes.put(scene, tokensStylesheet);
        ChangeListener<Parent> rootListener = (obs, old, root) -> {
            if (old != null) {
                clearRootClasses(old);
            }
            updateRootClasses(scene);
        };
        scene.rootProperty().addListener(rootListener);
        rootListeners.put(scene, rootListener);
        updateRootClasses(scene);
    }

    /** Removes the theme from a scene. */
    public void remove(Scene scene) {
        String tokens = scenes.remove(scene);
        if (tokens == null) {
            return;
        }
        scene.getStylesheets().remove(tokens);
        scene.getStylesheets().remove(STYLESHEET);
        ChangeListener<Parent> listener = rootListeners.remove(scene);
        if (listener != null) {
            scene.rootProperty().removeListener(listener);
        }
        if (scene.getRoot() != null) {
            clearRootClasses(scene.getRoot());
        }
    }

    /**
     * Applies the theme to every current and future window: stages, dialogs, alerts, menus,
     * tooltips and other popups.
     * <p>
     * A scene is themed when it is set on a window. When you replace the scene of a window that
     * is <em>already showing</em>, JavaFX styles the new scene during {@code setScene}, before the
     * theme is added, and logs warnings about unresolved {@code -md-*} colors for that first pass.
     * Call {@link #apply(Scene)} on the scene before setting it to avoid this.
     */
    public void applyGlobally() {
        if (windowListener != null) {
            return;
        }
        windowListener = change -> {
            while (change.next()) {
                for (Window window : change.getAddedSubList()) {
                    track(window);
                }
            }
        };
        Window.getWindows().addListener(windowListener);
        Window.getWindows().forEach(this::track);
    }

    private void track(Window window) {
        if (window.getScene() != null) {
            apply(window.getScene());
        }
        window.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null && windowListener != null) {
                apply(scene);
            }
        });
    }

    private void rebuild() {
        boolean isDark = switch (getMode()) {
            case LIGHT -> false;
            case DARK -> true;
            case SYSTEM -> Platform.getPreferences().getColorScheme() == javafx.application.ColorScheme.DARK;
        };
        Color seed = getSeedColor();
        if (isUseSystemAccent() && Platform.getPreferences().getAccentColor() != null) {
            seed = Platform.getPreferences().getAccentColor();
        }
        ColorScheme newScheme = ColorScheme.builder(seed)
                .dark(isDark)
                .variant(getVariant())
                .contrast(getContrast())
                .build();
        dark.set(isDark);
        scheme.set(newScheme);

        String css = generateTokens(newScheme);
        tokensStylesheet = "data:text/css;base64,"
                + Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
        for (Map.Entry<Scene, String> entry : scenes.entrySet()) {
            Scene scene = entry.getKey();
            int index = scene.getStylesheets().indexOf(entry.getValue());
            if (index >= 0) {
                scene.getStylesheets().set(index, tokensStylesheet);
            } else {
                scene.getStylesheets().add(0, tokensStylesheet);
            }
            entry.setValue(tokensStylesheet);
            updateRootClasses(scene);
        }
    }

    private String generateTokens(ColorScheme s) {
        StringBuilder css = new StringBuilder(16_000);
        // Popups (tooltips, menus) live in their own scene; defining the tokens on them too makes
        // them resolve whatever their styleable parent chain looks like.
        css.append(".root, .tooltip, .context-menu {\n");
        for (ColorRole role : ColorRole.values()) {
            appendColor(css, role.cssName(), s.getArgb(role));
        }
        for (Map.Entry<String, Color> custom : customColors.entrySet()) {
            String name = custom.getKey().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
            ColorScheme.CustomColor c = s.customColor(name, custom.getValue(), isHarmonizeCustomColors());
            appendColor(css, "-md-custom-" + name, ColorUtils.fromFx(c.color()));
            appendColor(css, "-md-custom-on-" + name, ColorUtils.fromFx(c.onColor()));
            appendColor(css, "-md-custom-" + name + "-container", ColorUtils.fromFx(c.container()));
            appendColor(css, "-md-custom-on-" + name + "-container", ColorUtils.fromFx(c.onContainer()));
        }
        String family = getFontFamily() == null ? "Roboto" : getFontFamily().replace("\"", "");
        css.append("    -fx-font-family: \"").append(family).append("\";\n");
        css.append("}\n");
        return css.toString();
    }

    private static void appendColor(StringBuilder css, String name, int argb) {
        css.append("    ").append(name).append(": ").append(ColorUtils.hex(argb)).append(";\n");
        int r = ColorUtils.redFromArgb(argb);
        int g = ColorUtils.greenFromArgb(argb);
        int b = ColorUtils.blueFromArgb(argb);
        for (int alpha : ALPHAS) {
            css.append("    ").append(name).append(String.format("-a%02d", alpha))
                    .append(String.format(Locale.ROOT, ": rgba(%d, %d, %d, %.2f);%n", r, g, b, alpha / 100.0));
        }
    }

    private void updateRootClasses(Scene scene) {
        Parent root = scene.getRoot();
        if (root == null) {
            return;
        }
        clearRootClasses(root);
        root.getProperties().put(THEME_KEY, this);
        root.getStyleClass().add(isDark() ? "md-dark" : "md-light");
        root.getStyleClass().add(getDensity().styleClass());
    }

    private static void clearRootClasses(Parent root) {
        root.getStyleClass().removeIf(c -> c.equals("md-dark") || c.equals("md-light") || c.startsWith("md-density-"));
        root.getProperties().remove(THEME_KEY);
    }

    // Properties

    public ObjectProperty<Color> seedColorProperty() {
        return seedColor;
    }

    public Color getSeedColor() {
        return seedColor.get();
    }

    public void setSeedColor(Color value) {
        seedColor.set(Objects.requireNonNull(value));
    }

    public ObjectProperty<ThemeMode> modeProperty() {
        return mode;
    }

    public ThemeMode getMode() {
        return mode.get();
    }

    public void setMode(ThemeMode value) {
        mode.set(Objects.requireNonNull(value));
    }

    public ObjectProperty<Variant> variantProperty() {
        return variant;
    }

    public Variant getVariant() {
        return variant.get();
    }

    public void setVariant(Variant value) {
        variant.set(Objects.requireNonNull(value));
    }

    /** 0 = standard, 0.5 = medium, 1 = high contrast. */
    public DoubleProperty contrastProperty() {
        return contrast;
    }

    public double getContrast() {
        return contrast.get();
    }

    public void setContrast(double value) {
        contrast.set(value);
    }

    public ObjectProperty<Density> densityProperty() {
        return density;
    }

    public Density getDensity() {
        return density.get();
    }

    public void setDensity(Density value) {
        density.set(Objects.requireNonNull(value));
    }

    /** Uses the operating system accent color as seed when the platform provides one. */
    public BooleanProperty useSystemAccentProperty() {
        return useSystemAccent;
    }

    public boolean isUseSystemAccent() {
        return useSystemAccent.get();
    }

    public void setUseSystemAccent(boolean value) {
        useSystemAccent.set(value);
    }

    /** Font family used for all text; Roboto (bundled) by default. */
    public StringProperty fontFamilyProperty() {
        return fontFamily;
    }

    public String getFontFamily() {
        return fontFamily.get();
    }

    public void setFontFamily(String value) {
        fontFamily.set(value);
    }

    /**
     * Extended colors, by name. A custom color {@code "success"} is exposed to CSS as
     * {@code -md-custom-success}, {@code -md-custom-on-success}, {@code -md-custom-success-container}
     * and {@code -md-custom-on-success-container}, generated for the current light/dark mode.
     */
    public ObservableMap<String, Color> getCustomColors() {
        return customColors;
    }

    /** Whether custom colors are shifted towards the primary hue to fit the theme (default true). */
    public BooleanProperty harmonizeCustomColorsProperty() {
        return harmonizeCustomColors;
    }

    public boolean isHarmonizeCustomColors() {
        return harmonizeCustomColors.get();
    }

    public void setHarmonizeCustomColors(boolean value) {
        harmonizeCustomColors.set(value);
    }

    /** The current color scheme. */
    public ReadOnlyObjectProperty<ColorScheme> schemeProperty() {
        return scheme.getReadOnlyProperty();
    }

    public ColorScheme getScheme() {
        return scheme.get();
    }

    /** Shortcut for a color of the current scheme. */
    public Color color(ColorRole role) {
        return getScheme().get(role);
    }

    /** Whether the theme currently renders dark (resolves {@link ThemeMode#SYSTEM}). */
    public ReadOnlyBooleanProperty darkProperty() {
        return dark.getReadOnlyProperty();
    }

    public boolean isDark() {
        return dark.get();
    }
}
