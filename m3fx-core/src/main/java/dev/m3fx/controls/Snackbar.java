package dev.m3fx.controls;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
 * }</pre>
 */
public class Snackbar extends HBox {

    public static final Duration SHORT = Duration.seconds(4);
    public static final Duration LONG = Duration.seconds(10);
    /** Stays until dismissed or its action is clicked. */
    public static final Duration INDEFINITE = Duration.INDEFINITE;

    private static final String QUEUE_KEY = "m3fx.snackbar.queue";

    private final Node owner;
    private final Label message = new Label();
    private Duration duration = SHORT;
    private Overlay overlay;
    private Runnable onDismissed;

    private Snackbar(Node owner, String text) {
        this.owner = owner;
        getStyleClass().add("snackbar");
        setMaxHeight(USE_PREF_SIZE);
        message.getStyleClass().add("snackbar-text");
        message.setText(text);
        message.setWrapText(true);
        message.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(message, Priority.ALWAYS);
        setAlignment(Pos.CENTER_LEFT);
        getChildren().add(message);
    }

    public static Snackbar make(Node owner, String text) {
        return new Snackbar(owner, text);
    }

    /** Shows a simple snackbar for {@link #SHORT}. */
    public static Snackbar show(Node owner, String text) {
        Snackbar snackbar = make(owner, text);
        snackbar.show();
        return snackbar;
    }

    /** Adds an action button; clicking it runs {@code handler} and dismisses the snackbar. */
    public Snackbar action(String text, Runnable handler) {
        Button button = Buttons.text(text);
        button.getStyleClass().add("snackbar-action");
        button.setOnAction(e -> {
            handler.run();
            dismiss();
        });
        getChildren().add(button);
        return this;
    }

    /** Adds a close icon button. */
    public Snackbar closeable() {
        IconButton close = new IconButton("close");
        close.getStyleClass().add("snackbar-close");
        close.setOnAction(e -> dismiss());
        getChildren().add(close);
        return this;
    }

    public Snackbar duration(Duration value) {
        this.duration = value;
        return this;
    }

    /** Called when the snackbar has been dismissed (by timeout, action or close). */
    public Snackbar onDismissed(Runnable callback) {
        this.onDismissed = callback;
        return this;
    }

    /** Queues the snackbar; it shows as soon as the previous one is gone. */
    public void show() {
        StackPane host = Overlay.findHost(owner);
        @SuppressWarnings("unchecked")
        Deque<Snackbar> queue = (Deque<Snackbar>) host.getProperties().computeIfAbsent(QUEUE_KEY, k -> new ArrayDeque<Snackbar>());
        queue.addLast(this);
        if (queue.size() == 1) {
            display();
        }
    }

    private void display() {
        StackPane.setMargin(this, new javafx.geometry.Insets(16));
        overlay = Overlay.show(owner, this, Overlay.Position.BOTTOM, false, false);
        overlay.setOnHidden(this::next);
        if (!duration.isIndefinite()) {
            PauseTransition pause = new PauseTransition(duration);
            pause.setOnFinished(e -> dismiss());
            pause.play();
        }
    }

    private void next() {
        StackPane host = Overlay.findHost(owner);
        @SuppressWarnings("unchecked")
        Deque<Snackbar> queue = (Deque<Snackbar>) host.getProperties().get(QUEUE_KEY);
        if (onDismissed != null) {
            onDismissed.run();
        }
        if (queue != null) {
            queue.remove(this);
            if (!queue.isEmpty()) {
                queue.peekFirst().display();
            }
        }
    }

    public void dismiss() {
        if (overlay != null) {
            overlay.hide();
        }
    }

}
