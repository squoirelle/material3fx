package io.github.squoirelle.material3fx;

import javafx.animation.Interpolator;
import javafx.util.Duration;

/**
 * Material 3 motion tokens: easing curves and durations.
 */
public final class Motion {

    public static final Interpolator STANDARD = Interpolator.SPLINE(0.2, 0, 0, 1);
    public static final Interpolator STANDARD_DECELERATE = Interpolator.SPLINE(0, 0, 0, 1);
    public static final Interpolator STANDARD_ACCELERATE = Interpolator.SPLINE(0.3, 0, 1, 1);
    public static final Interpolator EMPHASIZED = Interpolator.SPLINE(0.2, 0, 0, 1);
    public static final Interpolator EMPHASIZED_DECELERATE = Interpolator.SPLINE(0.05, 0.7, 0.1, 1);
    public static final Interpolator EMPHASIZED_ACCELERATE = Interpolator.SPLINE(0.3, 0, 0.8, 0.15);

    public static final Duration SHORT_1 = Duration.millis(50);
    public static final Duration SHORT_2 = Duration.millis(100);
    public static final Duration SHORT_3 = Duration.millis(150);
    public static final Duration SHORT_4 = Duration.millis(200);
    public static final Duration MEDIUM_1 = Duration.millis(250);
    public static final Duration MEDIUM_2 = Duration.millis(300);
    public static final Duration MEDIUM_3 = Duration.millis(350);
    public static final Duration MEDIUM_4 = Duration.millis(400);
    public static final Duration LONG_1 = Duration.millis(450);
    public static final Duration LONG_2 = Duration.millis(500);
    public static final Duration LONG_3 = Duration.millis(550);
    public static final Duration LONG_4 = Duration.millis(600);

    private static volatile boolean enabled = true;

    private Motion() {
    }

    /** Globally enables or disables animations (e.g. for reduced-motion preferences or tests). */
    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Whether an animation on {@code node} would be visible: motion is enabled and the node is
     * in a showing window. Components jump straight to their end state otherwise.
     */
    public static boolean shouldAnimate(javafx.scene.Node node) {
        return enabled && node.getScene() != null && node.getScene().getWindow() != null
                && node.getScene().getWindow().isShowing();
    }

    /** {@code duration}, or zero when motion is disabled. */
    public static Duration of(Duration duration) {
        return enabled ? duration : Duration.ONE;
    }
}
