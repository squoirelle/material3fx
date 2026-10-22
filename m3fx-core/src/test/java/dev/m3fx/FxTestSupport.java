package dev.m3fx;

import javafx.application.Platform;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/** Starts the JavaFX toolkit once and runs code on the FX thread. */
public final class FxTestSupport {

    private static boolean started;

    private FxTestSupport() {
    }

    public static synchronized void start() throws InterruptedException {
        if (started) {
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyStarted) {
            latch.countDown();
        }
        latch.await(10, TimeUnit.SECONDS);
        Platform.setImplicitExit(false);
        started = true;
    }

    public static <T> T onFx(Supplier<T> action) throws Exception {
        start();
        CompletableFuture<T> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                future.complete(action.get());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future.get(30, TimeUnit.SECONDS);
    }
}
