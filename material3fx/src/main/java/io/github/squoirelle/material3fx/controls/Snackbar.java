package io.github.squoirelle.material3fx.controls;

import io.github.squoirelle.material3fx.skins.SnackbarSkin;
import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Material snackbar: a brief message at the bottom of the window, with an optional action.
 * Snackbars are queued per window: only one is visible at a time.
 *
 * <pre>{@code
 * Snackbar.make(owner, "Track deleted").action("Undo", this::undo).show();
 * Snackbar.show(owner, "Saved");
 *
 * Snackbar snackbar = new Snackbar("Saved");      // or as a regular control
 * snackbar.setActionText("Open");
 * snackbar.setOnAction(e -> open());
 * snackbar.show(owner);
 * }</pre>
 */
public class Snackbar extends Control {

    public static final Duration SHORT = Duration.seconds(4);
    public static final Duration LONG = Duration.seconds(10);
    /** Stays until dismissed or its action is clicked. */
    public static final Duration INDEFINITE = Duration.INDEFINITE;

    private static final String QUEUE_KEY = "material3fx.snackbar.queue";

    private final StringProperty message = new SimpleStringProperty(this, "message");
    private final StringProperty actionText = new SimpleStringProperty(this, "actionText");
    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>(this, "onAction");
    private final BooleanProperty closeable = new SimpleBooleanProperty(this, "closeable");
    private final ObjectProperty<Duration> duration = new SimpleObjectProperty<>(this, "duration", SHORT);
    private final ObjectProperty<EventHandler<ActionEvent>> onDismissed = new SimpleObjectProperty<>(this, "onDismissed");
    private Node owner;
    private Overlay overlay;

    public Snackbar() {
        this(null);
    }

    public Snackbar(String message) {
        getStyleClass().add("snackbar");
        setFocusTraversable(false);
        setAccessibleRole(AccessibleRole.TEXT);
        accessibleTextProperty().bind(this.message);
        this.message.set(message);
    }

    /** Creates a snackbar for {@code owner}'s window; call {@link #show()} to queue it. */
    public static Snackbar make(Node owner, String message) {
        Snackbar snackbar = new Snackbar(message);
        snackbar.owner = owner;
        return snackbar;
    }

    /** Shows a simple snackbar for {@link #SHORT}. */
    public static Snackbar show(Node owner, String message) {
        Snackbar snackbar = make(owner, message);
        snackbar.show();
        return snackbar;
    }

    /** Fluent: adds an action button running {@code handler}; clicking it also dismisses the snackbar. */
    public Snackbar action(String text, Runnable handler) {
        setActionText(text);
        setOnAction(e -> handler.run());
        return this;
    }

    /** Fluent: adds a close icon button. */
    public Snackbar closeable() {
        setCloseable(true);
        return this;
    }

    /** Fluent: sets the display duration. */
    public Snackbar duration(Duration value) {
        setDuration(value);
        return this;
    }

    /** Fluent: called once the snackbar is dismissed (timeout, action or close). */
    public Snackbar onDismissed(Runnable callback) {
        setOnDismissed(e -> callback.run());
        return this;
    }

    /** Runs the action and dismisses the snackbar, as a click on the action button does. */
    public void fireAction() {
        if (getOnAction() != null) {
            getOnAction().handle(new ActionEvent(this, this));
        }
        dismiss();
    }

    /** Queues the snackbar in the window of the owner given to {@link #make(Node, String)}. */
    public void show() {
        if (owner == null) {
            throw new IllegalStateException("No owner: use show(Node) or Snackbar.make(owner, text)");
        }
        show(owner);
    }

    /** Queues the snackbar in the window of {@code owner}; it shows once the previous one is gone. */
    public void show(Node owner) {
        this.owner = owner;
        Deque<Snackbar> queue = queue(Overlay.findHost(owner));
        queue.addLast(this);
        if (queue.size() == 1) {
            display();
        }
    }

    @SuppressWarnings("unchecked")
    private static Deque<Snackbar> queue(StackPane host) {
        return (Deque<Snackbar>) host.getProperties().computeIfAbsent(QUEUE_KEY, k -> new ArrayDeque<Snackbar>());
    }

    private void display() {
        StackPane.setMargin(this, new javafx.geometry.Insets(16));
        overlay = Overlay.show(owner, this, Overlay.Position.BOTTOM, false, false);
        overlay.setOnHidden(this::next);
        if (getDuration() != null && !getDuration().isIndefinite()) {
            PauseTransition pause = new PauseTransition(getDuration());
            pause.setOnFinished(e -> dismiss());
            pause.play();
        }
    }

    private void next() {
        if (getOnDismissed() != null) {
            getOnDismissed().handle(new ActionEvent(this, this));
        }
        Deque<Snackbar> queue = queue(Overlay.findHost(owner));
        queue.remove(this);
        if (!queue.isEmpty()) {
            queue.peekFirst().display();
        }
    }

    public void dismiss() {
        if (overlay != null) {
            overlay.hide();
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SnackbarSkin(this);
    }

    public final StringProperty messageProperty() {
        return message;
    }

    public final String getMessage() {
        return message.get();
    }

    public final void setMessage(String value) {
        message.set(value);
    }

    public final StringProperty actionTextProperty() {
        return actionText;
    }

    public final String getActionText() {
        return actionText.get();
    }

    public final void setActionText(String value) {
        actionText.set(value);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    public final EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    public final void setOnAction(EventHandler<ActionEvent> value) {
        onAction.set(value);
    }

    public final BooleanProperty closeableProperty() {
        return closeable;
    }

    public final boolean isCloseable() {
        return closeable.get();
    }

    public final void setCloseable(boolean value) {
        closeable.set(value);
    }

    public final ObjectProperty<Duration> durationProperty() {
        return duration;
    }

    public final Duration getDuration() {
        return duration.get();
    }

    public final void setDuration(Duration value) {
        duration.set(value);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onDismissedProperty() {
        return onDismissed;
    }

    public final EventHandler<ActionEvent> getOnDismissed() {
        return onDismissed.get();
    }

    public final void setOnDismissed(EventHandler<ActionEvent> value) {
        onDismissed.set(value);
    }
}
