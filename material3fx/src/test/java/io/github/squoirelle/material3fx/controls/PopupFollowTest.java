package io.github.squoirelle.material3fx.controls;

import io.github.squoirelle.material3fx.FxTestSupport;
import io.github.squoirelle.material3fx.MaterialTheme;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.VBox;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Popups move with the control that opened them when the layout moves it inside the window
 * (e.g. a navigation drawer collapsing). Menus shown on a node rely on the theme for this; combo
 * box popups are moved by JavaFX itself, and must not be moved twice.
 */
class PopupFollowTest {

    @BeforeAll
    static void startToolkit() throws Exception {
        FxTestSupport.start();   // the controls below are created before the first onFx
    }

    @Test
    void searchSuggestionsFollowTheirBar() throws Exception {
        SearchBar bar = new SearchBar("Search");
        bar.setSuggestionProvider(text -> List.of("Cordless drill", "Cable"));
        // Suggestions only show for a focused bar.
        assertFollows(bar, b -> ((SearchBar) b).setText("c"), true);
    }

    @Test
    void choiceBoxMenuFollowsItsControl() throws Exception {
        assertFollows(new ChoiceBox<>(FXCollections.observableArrayList("Aisle A", "Aisle B")), b -> ((ChoiceBox<?>) b).show());
    }

    @Test
    void comboBoxDropdownFollowsItsControl() throws Exception {
        assertFollows(new ComboBox<>(FXCollections.observableArrayList("One", "Two")), b -> ((ComboBoxBase<?>) b).show());
    }

    @Test
    void datePickerFollowsItsControl() throws Exception {
        assertFollows(new DatePicker(), b -> ((ComboBoxBase<?>) b).show());
    }

    @Test
    void popupsCloseWhenTheirWindowMoves() throws Exception {
        assertClosesWithWindowMove(new ComboBox<>(FXCollections.observableArrayList("One", "Two")), b -> ((ComboBoxBase<?>) b).show());
        assertClosesWithWindowMove(new ChoiceBox<>(FXCollections.observableArrayList("Aisle A", "Aisle B")), b -> ((ChoiceBox<?>) b).show());
    }

    /** Dragging the window (or resizing it from the left) moves it: its open popups close. */
    private static void assertClosesWithWindowMove(javafx.scene.Node control, Consumer<javafx.scene.Node> open) throws Exception {
        Stage[] stage = new Stage[1];
        FxTestSupport.onFx(() -> {
            new MaterialTheme().applyGlobally();
            stage[0] = new Stage();
            stage[0].setScene(new Scene(new VBox(control), 600, 400));
            stage[0].setX(100);
            stage[0].setY(100);
            stage[0].show();
            return null;
        });
        Thread.sleep(500);
        FxTestSupport.onFx(() -> {
            open.accept(control);
            return null;
        });
        Thread.sleep(500);
        PopupWindow popup = FxTestSupport.onFx(PopupFollowTest::showingPopup);
        double[] before = FxTestSupport.onFx(() -> new double[] {stage[0].getX(), stage[0].getY()});
        FxTestSupport.onFx(() -> {
            stage[0].setX(stage[0].getX() + 150);
            stage[0].setY(stage[0].getY() + 40);
            return null;
        });
        Thread.sleep(700);
        boolean moved = FxTestSupport.onFx(() -> stage[0].getX() != before[0] || stage[0].getY() != before[1]);
        if (!moved) {
            FxTestSupport.onFx(() -> {
                stage[0].close();
                return null;
            });
        }
        // Some window managers (e.g. COSMIC) do not let an application move its own window.
        Assumptions.assumeTrue(moved, "the window manager does not let the test move the window");
        FxTestSupport.onFx(() -> {
            assertFalse(popup.isShowing(), control.getClass().getSimpleName() + " popup closed when the window moved");
            stage[0].close();
            return null;
        });
        Thread.sleep(300);
    }

    private static void assertFollows(javafx.scene.Node control, Consumer<javafx.scene.Node> open) throws Exception {
        assertFollows(control, open, false);
    }

    private static void assertFollows(javafx.scene.Node control, Consumer<javafx.scene.Node> open, boolean needsFocus) throws Exception {
        VBox root = new VBox(control);
        Stage[] stage = new Stage[1];
        FxTestSupport.onFx(() -> {
            new MaterialTheme().applyGlobally();
            stage[0] = new Stage();
            stage[0].setScene(new Scene(root, 800, 500));
            stage[0].show();
            return null;
        });
        Thread.sleep(300);
        if (needsFocus) {
            org.junit.jupiter.api.Assertions.assertTrue(FxTestSupport.focus(control), "the control gets the focus");
        }
        FxTestSupport.onFx(() -> {
            open.accept(control);
            return null;
        });
        Thread.sleep(500);
        double[] before = FxTestSupport.onFx(() -> {
            PopupWindow popup = showingPopup();
            return new double[] {popup.getAnchorX(), popup.getAnchorY()};
        });
        FxTestSupport.onFx(() -> {
            // The layout moves the control 200px right and 30px down inside the window.
            root.setPadding(new Insets(30, 0, 0, 200));
            root.layout();
            return null;
        });
        Thread.sleep(300);
        FxTestSupport.onFx(() -> {
            PopupWindow popup = showingPopup();
            assertEquals(before[0] + 200, popup.getAnchorX(), 0.5, "moved right with its control");
            assertEquals(before[1] + 30, popup.getAnchorY(), 0.5, "moved down with its control");
            popup.hide();
            stage[0].close();
            return null;
        });
    }

    private static PopupWindow showingPopup() {
        return (PopupWindow) Window.getWindows().stream()
                .filter(w -> w instanceof PopupWindow && w.isShowing())
                .reduce((a, b) -> b).orElseThrow();
    }
}
