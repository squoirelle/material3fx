package io.github.squoirelle.material3fx.controls;

import io.github.squoirelle.material3fx.FxTestSupport;
import io.github.squoirelle.material3fx.MaterialTheme;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Popups opened near the bottom of a window stay inside it (Wayland workaround): parts outside
 * the window would not receive clicks on some compositors.
 */
class PopupPlacementTest {

    @Test
    void popupsNearTheBottomStayInsideTheWindow() throws Exception {
        ColorPicker[] picker = new ColorPicker[1];
        ComboBox<?>[] combo = new ComboBox<?>[1];
        Stage[] stage = new Stage[1];
        FxTestSupport.onFx(() -> {
            MaterialTheme theme = new MaterialTheme();
            theme.setKeepPopupsInWindow(true);
            theme.applyGlobally();
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

    private static void assertInside(Stage stage, String what) {
        Window popup = Window.getWindows().stream().filter(w -> w instanceof PopupWindow && w.isShowing())
                .reduce((a, b) -> b).orElseThrow();
        double bottom = stage.getY() + stage.getScene().getY() + stage.getScene().getHeight();
        assertTrue(popup.getY() + popup.getHeight() <= bottom + 1,
                what + " extends below the window: " + (popup.getY() + popup.getHeight()) + " > " + bottom);
        assertTrue(popup.getY() >= stage.getY() + stage.getScene().getY() - 1, what + " above the window");
    }
}
