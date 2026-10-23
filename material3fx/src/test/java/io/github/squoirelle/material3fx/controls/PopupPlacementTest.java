package io.github.squoirelle.material3fx.controls;

import io.github.squoirelle.material3fx.FxTestSupport;
import io.github.squoirelle.material3fx.MaterialTheme;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.PopupWindow;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Popups opened near the bottom of a window stay inside it (Wayland workaround): parts outside
 * the window would not receive clicks on some compositors.
 */
class PopupPlacementTest {

    /** Every position / size change of the popups, for the failure message (CI has no screen to look at). */
    private final List<String> history = new CopyOnWriteArrayList<>();
    private final long start = System.nanoTime();

    @Test
    void popupsNearTheBottomStayInsideTheWindow() throws Exception {
        ColorPicker[] picker = new ColorPicker[1];
        ComboBox<?>[] combo = new ComboBox<?>[1];
        Stage[] stage = new Stage[1];
        FxTestSupport.onFx(() -> {
            MaterialTheme theme = new MaterialTheme();
            theme.setKeepPopupsInWindow(true);
            theme.applyGlobally();
            Window.getWindows().addListener((ListChangeListener<Window>) change -> {
                while (change.next()) {
                    for (Window w : change.getAddedSubList()) {
                        if (w instanceof PopupWindow popup) {
                            record(popup, "added");
                            popup.yProperty().addListener(o -> record(popup, "y"));
                            popup.heightProperty().addListener(o -> record(popup, "height"));
                            popup.showingProperty().addListener(o -> record(popup, "showing"));
                        }
                    }
                }
            });
            picker[0] = new ColorPicker();
            combo[0] = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                    "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight"));
            VBox root = new VBox(8, combo[0], picker[0]);
            root.setAlignment(Pos.BOTTOM_LEFT);
            stage[0] = new Stage();
            stage[0].setScene(new Scene(root, 700, 600));
            stage[0].show();
            return null;
        });
        // The compositor positions new windows asynchronously; real apps open popups long after.
        Thread.sleep(800);
        FxTestSupport.onFx(() -> {
            picker[0].show();
            return null;
        });
        Thread.sleep(600);
        FxTestSupport.onFx(() -> {
            assertInside(stage[0], "color palette");
            picker[0].hide();
            return null;
        });
        // Let the palette finish closing: its focus changes would auto-hide a popup opened at once.
        Thread.sleep(500);
        FxTestSupport.onFx(() -> {
            combo[0].show();
            return null;
        });
        Thread.sleep(600);
        FxTestSupport.onFx(() -> {
            assertInside(stage[0], "combo box dropdown");
            combo[0].hide();
            stage[0].close();
            return null;
        });
    }

    private void record(PopupWindow popup, String what) {
        history.add(String.format(Locale.ROOT, "%5dms %s %s: y=%.1f h=%.1f anchorY=%.1f showing=%s",
                (System.nanoTime() - start) / 1_000_000, popup.getClass().getSimpleName(),
                Integer.toHexString(System.identityHashCode(popup)), popup.getY(), popup.getHeight(),
                popup.getAnchorY(), popup.isShowing()));
    }

    private void assertInside(Stage stage, String what) {
        PopupWindow popup = (PopupWindow) Window.getWindows().stream().filter(w -> w instanceof PopupWindow && w.isShowing())
                .reduce((a, b) -> b).orElseThrow();
        double bottom = stage.getY() + stage.getScene().getY() + stage.getScene().getHeight();
        assertTrue(popup.getY() + popup.getHeight() <= bottom + 1,
                () -> what + " extends below the window: " + (popup.getY() + popup.getHeight()) + " > " + bottom
                        + diagnostics(stage, popup));
        assertTrue(popup.getY() >= stage.getY() + stage.getScene().getY() - 1,
                () -> what + " above the window" + diagnostics(stage, popup));
    }

    private String diagnostics(Stage stage, PopupWindow popup) {
        Node owner = popup.getOwnerNode();
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        return String.format(Locale.ROOT,
                "%n  window: y=%.1f sceneY=%.1f sceneHeight=%.1f%n  screen visual bounds: %s%n  owner node: %s on screen %s"
                        + "%n  popup: %s x=%.1f y=%.1f w=%.1f h=%.1f anchorY=%.1f autoFix=%s%n  history:%n    %s",
                stage.getY(), stage.getScene().getY(), stage.getScene().getHeight(), screen,
                owner == null ? null : owner.getClass().getSimpleName(),
                owner == null ? null : owner.localToScreen(owner.getLayoutBounds()),
                popup.getClass().getSimpleName(), popup.getX(), popup.getY(), popup.getWidth(), popup.getHeight(),
                popup.getAnchorY(), popup.isAutoFix(), String.join(System.lineSeparator() + "    ", history));
    }
}
