package io.github.squoirelle.material3fx;

import io.github.squoirelle.material3fx.color.ColorRole;
import io.github.squoirelle.material3fx.color.ColorScheme;
import io.github.squoirelle.material3fx.color.ColorUtils;
import io.github.squoirelle.material3fx.color.Variant;
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
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.PopupWindow;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * Entry point of Material3FX: generates a Material 3 color scheme and applies it, together with the
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
 * <p>
 * Besides Material's roles, the theme always generates semantic status roles, harmonized with the
 * seed: {@code -md-sys-color-success}, {@code -md-sys-color-warning}, their {@code on-},
 * {@code -container} and {@code on-...-container} variants (error is Material's own).
 */
public final class MaterialTheme {

    /** Material's baseline seed color. */
    public static final Color BASELINE = Color.web("#6750A4");

    private static final int[] ALPHAS = {8, 10, 12, 16, 32, 38};
    /** Seeds of the semantic status roles, harmonized with the theme. */
    private static final Map<String, Color> STATUS_COLORS = Map.of(
            "success", Color.web("#2E7D32"),
            "warning", Color.web("#B26A00"));
    private static final String THEME_KEY = MaterialTheme.class.getName();
    private static final String STYLESHEET = Objects.requireNonNull(
            MaterialTheme.class.getResource("material3fx.css"), "material3fx.css missing").toExternalForm();
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
        SystemPreferences.onColorSchemeChange(o -> {
            if (getMode() == ThemeMode.SYSTEM) {
                rebuild();
            }
        });
        SystemPreferences.onAccentColorChange(o -> {
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

    /** URL of the Material3FX component stylesheet, for manual use. {@link #apply(Scene)} adds it for you. */
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
     * tooltips and other popups. Open popups also stay attached: they move with the control that
     * opened them when the layout moves it, and close when their window moves.
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
        if (window instanceof PopupWindow popup) {
            followOwner(popup);
        }
        window.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null && windowListener != null) {
                apply(scene);
            }
        });
    }

    /**
     * Keeps open popups (menus, dropdowns, pickers, suggestions...) attached to what they belong to.
     * <ul>
     * <li>When their window moves (dragged, or resized from the left edge), they close, as native
     * dropdowns do: JavaFX leaves them in place, and following the window is not reliable on
     * every desktop (some compositors report its position late during a drag).</li>
     * <li>A popup shown on a node moves with it when the layout moves that node inside the window
     * (e.g. a navigation drawer collapsing). Combo boxes and pickers have no owner node: JavaFX
     * already keeps their popup under them in that case.</li>
     * </ul>
     */
    private static void followOwner(PopupWindow popup) {
        // Once per popup, even if several themes are applied globally: each would add the move.
        if (popup.getProperties().putIfAbsent("material3fx.followsOwner", Boolean.TRUE) != null) {
            return;
        }
        Window[] window = new Window[1];
        Point2D[] windowPosition = new Point2D[1];
        Node[] node = new Node[1];
        Point2D[] nodePosition = new Point2D[1];
        // Where the popup belongs. The anchor can differ: a popup pushed back on screen while it
        // still had its old size keeps that pushed anchor once it shrinks.
        Point2D[] anchor = new Point2D[1];
        Runnable reanchor = () -> {
            if (anchor[0] != null) {
                popup.setAnchorX(anchor[0].getX());
                popup.setAnchorY(anchor[0].getY());
            }
        };
        // Later, not now: the size changes inside JavaFX's own popup update, which then stores its
        // (pushed) anchor and would undo ours (it does on Windows).
        ChangeListener<Number> resized = (o, old, now) -> Platform.runLater(reanchor);
        InvalidationListener windowMoved = o -> {
            if (window[0] != null && !windowPosition[0].equals(new Point2D(window[0].getX(), window[0].getY()))) {
                popup.hide();
            }
        };
        InvalidationListener nodeMoved = o -> {
            if (node[0] == null) {
                return;
            }
            // Scene coordinates: only the node's move inside its window counts.
            // Reading the transform property (not localToScene) re-arms this invalidation listener.
            Point2D now = node[0].getLocalToSceneTransform().transform(0, 0);
            Point2D delta = now.subtract(nodePosition[0]);
            nodePosition[0] = now;
            if (delta.getX() != 0 || delta.getY() != 0) {
                anchor[0] = anchor[0].add(delta);
                reanchor.run();
            }
        };
        popup.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> {
            window[0] = popup.getOwnerWindow();
            if (window[0] != null) {
                windowPosition[0] = new Point2D(window[0].getX(), window[0].getY());
                window[0].xProperty().addListener(windowMoved);
                window[0].yProperty().addListener(windowMoved);
            }
            node[0] = popup.getOwnerNode();
            if (node[0] != null) {
                node[0].localToSceneTransformProperty().addListener(nodeMoved);
                nodePosition[0] = node[0].getLocalToSceneTransform().transform(0, 0);
                anchor[0] = new Point2D(popup.getAnchorX(), popup.getAnchorY());
                popup.widthProperty().addListener(resized);
                popup.heightProperty().addListener(resized);
            }
        });
        popup.addEventHandler(WindowEvent.WINDOW_HIDDEN, e -> {
            if (window[0] != null) {
                window[0].xProperty().removeListener(windowMoved);
                window[0].yProperty().removeListener(windowMoved);
            }
            if (node[0] != null) {
                node[0].localToSceneTransformProperty().removeListener(nodeMoved);
                popup.widthProperty().removeListener(resized);
                popup.heightProperty().removeListener(resized);
            }
            window[0] = null;
            node[0] = null;
            anchor[0] = null;
        });
    }

    private void rebuild() {
        if (getMode() == ThemeMode.SYSTEM || isUseSystemAccent()) {
            SystemPreferences.watch();
        }
        boolean isDark = switch (getMode()) {
            case LIGHT -> false;
            case DARK -> true;
            case SYSTEM -> SystemPreferences.isDark();
        };
        Color seed = getSeedColor();
        if (isUseSystemAccent() && SystemPreferences.accentColor() != null) {
            seed = SystemPreferences.accentColor();
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
        // Semantic status roles (a Material3FX extension: Material only defines error), always available.
        for (Map.Entry<String, Color> status : STATUS_COLORS.entrySet()) {
            ColorScheme.CustomColor c = s.customColor(status.getKey(), status.getValue(), true);
            appendColor(css, "-md-sys-color-" + status.getKey(), ColorUtils.fromFx(c.color()));
            appendColor(css, "-md-sys-color-on-" + status.getKey(), ColorUtils.fromFx(c.onColor()));
            appendColor(css, "-md-sys-color-" + status.getKey() + "-container", ColorUtils.fromFx(c.container()));
            appendColor(css, "-md-sys-color-on-" + status.getKey() + "-container", ColorUtils.fromFx(c.onContainer()));
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

    /** Uses the operating system accent color as seed when the platform provides one (JavaFX 22 and later). */
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
