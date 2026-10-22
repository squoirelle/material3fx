package dev.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * A destination shown by {@link NavigationBar}, {@link NavigationRail} and {@link NavigationDrawer}.
 * The same items can be shared by several navigation components (e.g. a rail on wide windows and
 * a bar on narrow ones), they render the item's current state.
 */
public class NavigationItem {

    public enum Kind { DESTINATION, HEADLINE, DIVIDER }

    private final Kind kind;
    private final StringProperty text = new SimpleStringProperty(this, "text");
    private final StringProperty icon = new SimpleStringProperty(this, "icon");
    private final StringProperty selectedIcon = new SimpleStringProperty(this, "selectedIcon");
    private final IntegerProperty badgeCount = new SimpleIntegerProperty(this, "badgeCount", -1);
    private final StringProperty badgeText = new SimpleStringProperty(this, "badgeText");
    private final BooleanProperty disabled = new SimpleBooleanProperty(this, "disabled");
    private final ObjectProperty<Object> userData = new SimpleObjectProperty<>(this, "userData");

    public NavigationItem(String text, String icon) {
        this(Kind.DESTINATION, text, icon);
    }

    private NavigationItem(Kind kind, String text, String icon) {
        this.kind = kind;
        this.text.set(text);
        this.icon.set(icon);
    }

    /** A section headline, rendered by the navigation drawer only. */
    public static NavigationItem headline(String text) {
        return new NavigationItem(Kind.HEADLINE, text, null);
    }

    /** A divider, rendered by the navigation drawer only. */
    public static NavigationItem divider() {
        return new NavigationItem(Kind.DIVIDER, null, null);
    }

    public Kind getKind() {
        return kind;
    }

    public boolean isDestination() {
        return kind == Kind.DESTINATION;
    }

    public final StringProperty textProperty() {
        return text;
    }

    public final String getText() {
        return text.get();
    }

    public final void setText(String value) {
        text.set(value);
    }

    public final StringProperty iconProperty() {
        return icon;
    }

    public final String getIcon() {
        return icon.get();
    }

    public final void setIcon(String value) {
        icon.set(value);
    }

    /** Icon used when selected; when null the filled version of {@link #getIcon()} is used. */
    public final StringProperty selectedIconProperty() {
        return selectedIcon;
    }

    public final String getSelectedIcon() {
        return selectedIcon.get();
    }

    public final void setSelectedIcon(String value) {
        selectedIcon.set(value);
    }

    /** Badge: -1 hides it, 0 shows a dot, a positive value shows the count. */
    public final IntegerProperty badgeCountProperty() {
        return badgeCount;
    }

    public final int getBadgeCount() {
        return badgeCount.get();
    }

    public final void setBadgeCount(int value) {
        badgeCount.set(value);
    }

    /** Short badge text; takes precedence over the count. The drawer shows it as trailing text. */
    public final StringProperty badgeTextProperty() {
        return badgeText;
    }

    public final String getBadgeText() {
        return badgeText.get();
    }

    public final void setBadgeText(String value) {
        badgeText.set(value);
    }

    public final BooleanProperty disabledProperty() {
        return disabled;
    }

    public final boolean isDisabled() {
        return disabled.get();
    }

    public final void setDisabled(boolean value) {
        disabled.set(value);
    }

    public final ObjectProperty<Object> userDataProperty() {
        return userData;
    }

    public final Object getUserData() {
        return userData.get();
    }

    public final void setUserData(Object value) {
        userData.set(value);
    }

    @Override
    public String toString() {
        return "NavigationItem[" + kind + ", " + getText() + "]";
    }
}
