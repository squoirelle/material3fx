package dev.m3fx.controls;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Material badge: a small dot, or a pill with a count or short text, shown on an icon.
 *
 * <pre>{@code
 * Badge badge = Badge.count(3);
 * Node icon = Badge.attach(new Icon("mail"), badge);
 * }</pre>
 */
public class Badge extends Label {

    private final IntegerProperty count = new SimpleIntegerProperty(this, "count", -1);
    private final IntegerProperty max = new SimpleIntegerProperty(this, "max", 999);

    /** A small dot badge. */
    public Badge() {
        this(null);
    }

    /** A large badge with text. */
    public Badge(String text) {
        super(text);
        getStyleClass().add("badge");
        setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setAlignment(Pos.CENTER);
        setMouseTransparent(true);
        textProperty().addListener(o -> {
            updateSmall();
            if (getParent() != null) {
                getParent().requestLayout();
            }
        });
        count.addListener(o -> updateCount());
        max.addListener(o -> updateCount());
        updateSmall();
    }

    public static Badge count(int count) {
        Badge badge = new Badge();
        badge.setCount(count);
        return badge;
    }

    /**
     * Wraps {@code target} so the badge sits on its top-right corner. The badge keeps its own
     * visibility, so hide it with {@code badge.setVisible(false)}.
     */
    public static StackPane attach(Node target, Badge badge) {
        return new Anchor(target, badge);
    }

    /** A stack pane that positions its badge relative to the target after layout. */
    private static final class Anchor extends StackPane {
        private final Node target;
        private final Badge badge;

        Anchor(Node target, Badge badge) {
            super(target, badge);
            this.target = target;
            this.badge = badge;
            getStyleClass().add("badge-anchor");
            badge.setManaged(false);
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            badge.autosize();
            javafx.geometry.Bounds b = target.getBoundsInParent();
            if (badge.isSmall()) {
                badge.relocate(b.getMaxX() - badge.getWidth() - 1, b.getMinY() + 1);
            } else {
                badge.relocate(b.getMaxX() - badge.getWidth() / 2 - 6, b.getMinY() - 4);
            }
        }
    }

    private void updateCount() {
        int c = getCount();
        if (c < 0) {
            setText(null);
        } else {
            setText(c > getMax() ? getMax() + "+" : String.valueOf(c));
        }
    }

    private void updateSmall() {
        boolean small = getText() == null || getText().isEmpty();
        pseudoClassStateChanged(SMALL, small);
    }

    private static final javafx.css.PseudoClass SMALL = javafx.css.PseudoClass.getPseudoClass("small");

    public boolean isSmall() {
        return getText() == null || getText().isEmpty();
    }

    /** Count shown in the badge; negative for none. */
    public final IntegerProperty countProperty() {
        return count;
    }

    public final int getCount() {
        return count.get();
    }

    public final void setCount(int value) {
        count.set(value);
    }

    /** Counts above this show as "max+" (default 999). */
    public final IntegerProperty maxProperty() {
        return max;
    }

    public final int getMax() {
        return max.get();
    }

    public final void setMax(int value) {
        max.set(value);
    }
}
