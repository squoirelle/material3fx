package dev.m3fx.controls;

import dev.m3fx.MaterialTheme;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A Material icon, rendered from the bundled Material Icons fonts.
 * <p>
 * Icons are looked up by their Material name ({@code "home"}, {@code "settings"},
 * {@code "arrow_back"}...); see <a href="https://fonts.google.com/icons">fonts.google.com/icons</a>.
 * Outlined icons are the default, filled icons are used for selected/active states.
 * <p>
 * Used as the graphic of a {@link Labeled} (button, label...), the icon follows the labeled's text
 * color, so it gets the right color in every state without extra CSS.
 */
public class Icon extends Text {

    private static final Map<String, Character> OUTLINED = loadCodepoints("fonts/MaterialIconsOutlined-Regular.codepoints");
    private static final Map<String, Character> FILLED = loadCodepoints("fonts/MaterialIcons-Regular.codepoints");

    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final BooleanProperty filled = new SimpleBooleanProperty(this, "filled");
    private final DoubleProperty size = new SimpleDoubleProperty(this, "size", 24);
    private final BooleanProperty followTextFill = new SimpleBooleanProperty(this, "followTextFill", true);

    private Labeled followed;
    private final ChangeListener<Paint> textFillListener = (obs, old, fill) -> applyFollowedFill();

    public Icon() {
        this(null);
    }

    public Icon(String name) {
        this(name, 24);
    }

    public Icon(String name, double size) {
        MaterialTheme.loadFonts();
        getStyleClass().add("md-icon");
        this.name.addListener((obs, old, value) -> updateGlyph());
        this.filled.addListener((obs, old, value) -> {
            pseudoClassStateChanged(PseudoClasses.FILLED, value);
            updateSize();
            updateGlyph();
        });
        this.size.addListener((obs, old, value) -> updateSize());
        this.followTextFill.addListener((obs, old, value) -> follow(getParent()));
        parentProperty().addListener((obs, old, parent) -> follow(parent));
        setName(name);
        setSize(size);
        updateSize();
        updateGlyph();
    }

    /** Creates a filled icon. */
    public static Icon filled(String name) {
        Icon icon = new Icon(name);
        icon.setFilled(true);
        return icon;
    }

    /** Whether an icon name exists in the bundled fonts. */
    public static boolean exists(String name) {
        return OUTLINED.containsKey(name) || FILLED.containsKey(name);
    }

    /** All available icon names. */
    public static Set<String> names() {
        return Collections.unmodifiableSet(OUTLINED.keySet());
    }

    private void updateGlyph() {
        String iconName = getName();
        Character glyph = null;
        if (iconName != null) {
            glyph = isFilled() ? FILLED.get(iconName) : OUTLINED.get(iconName);
            if (glyph == null) {
                glyph = isFilled() ? OUTLINED.get(iconName) : FILLED.get(iconName);
            }
        }
        setText(glyph == null ? "" : String.valueOf(glyph));
    }

    private void updateSize() {
        // Inline style: wins over the font inherited from the surrounding text.
        String family = isFilled() ? "Material Icons" : "Material Icons Outlined";
        setStyle("-fx-font-family: \"" + family + "\"; -fx-font-size: " + getSize() + "px;");
    }

    private void follow(Parent parent) {
        if (followed != null) {
            followed.textFillProperty().removeListener(textFillListener);
            followed = null;
        }
        boolean following = isFollowTextFill() && parent instanceof Labeled;
        if (following) {
            followed = (Labeled) parent;
            followed.textFillProperty().addListener(textFillListener);
            applyFollowedFill();
        }
        // Icons not inside a labeled get their color from CSS (.md-icon:standalone).
        pseudoClassStateChanged(PseudoClasses.STANDALONE, !following);
    }

    private void applyFollowedFill() {
        if (followed != null) {
            setFill(followed.getTextFill());
        }
    }

    private static Map<String, Character> loadCodepoints(String resource) {
        Map<String, Character> map = new HashMap<>();
        try (InputStream in = MaterialTheme.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource " + resource);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.US_ASCII));
            String line;
            while ((line = reader.readLine()) != null) {
                int space = line.indexOf(' ');
                if (space > 0) {
                    map.putIfAbsent(line.substring(0, space), (char) Integer.parseInt(line.substring(space + 1).trim(), 16));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return map;
    }

    public final StringProperty nameProperty() {
        return name;
    }

    public final String getName() {
        return name.get();
    }

    public final void setName(String value) {
        name.set(value);
    }

    public final BooleanProperty filledProperty() {
        return filled;
    }

    public final boolean isFilled() {
        return filled.get();
    }

    public final void setFilled(boolean value) {
        filled.set(value);
    }

    public final DoubleProperty sizeProperty() {
        return size;
    }

    public final double getSize() {
        return size.get();
    }

    public final void setSize(double value) {
        size.set(value);
    }

    /** When true (the default), the icon takes the text fill of the labeled it is the graphic of. */
    public final BooleanProperty followTextFillProperty() {
        return followTextFill;
    }

    public final boolean isFollowTextFill() {
        return followTextFill.get();
    }

    public final void setFollowTextFill(boolean value) {
        followTextFill.set(value);
    }
}
