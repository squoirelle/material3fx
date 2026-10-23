package io.github.squoirelle.material3fx.controls;

import io.github.squoirelle.material3fx.FxTestSupport;
import io.github.squoirelle.material3fx.MaterialTheme;
import io.github.squoirelle.material3fx.Motion;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.HitInfo;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Keyboard navigation, driven by real key events dispatched through the scene. */
class KeyboardTest {

    private Stage stage;

    @BeforeAll
    static void setUp() throws InterruptedException {
        FxTestSupport.start();
        Motion.setEnabled(false);
    }

    @AfterEach
    void tearDown() throws Exception {
        FxTestSupport.onFx(() -> {
            if (stage != null) {
                stage.close();
            }
            return null;
        });
    }

    private Scene show(javafx.scene.Parent root) {
        Scene scene = new Scene(root, 900, 700);
        new MaterialTheme().apply(scene);
        stage = new Stage();
        stage.setScene(scene);
        stage.show();
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        return scene;
    }

    /** Dispatches a key press to the current focus owner, as the keyboard would. */
    private static void press(Scene scene, KeyCode code, boolean shift) {
        Node target = scene.getFocusOwner() != null ? scene.getFocusOwner() : scene.getRoot();
        Event.fireEvent(target, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, shift, false, false, false));
    }

    private static void press(Scene scene, KeyCode code) {
        press(scene, code, false);
    }

    @Test
    void navigationArrowsMoveFocusAndEnterSelects() throws Exception {
        FxTestSupport.onFx(() -> {
            NavigationRail rail = new NavigationRail();
            NavigationItem a = new NavigationItem("A", "home");
            NavigationItem b = new NavigationItem("B", "search");
            NavigationItem c = new NavigationItem("C", "settings");
            rail.getItems().addAll(a, b, c);
            Scene scene = show(new VBox(rail));
            List<Node> items = List.copyOf(rail.lookupAll(".navigation-item"));
            items.getFirst().requestFocus();
            press(scene, KeyCode.DOWN);
            assertSame(items.get(1), scene.getFocusOwner(), "Down moves focus to the next destination");
            assertSame(a, rail.getSelectedItem(), "moving focus does not select");
            press(scene, KeyCode.ENTER);
            assertSame(b, rail.getSelectedItem(), "Enter selects the focused destination");
            press(scene, KeyCode.END);
            assertSame(items.get(2), scene.getFocusOwner());
            press(scene, KeyCode.HOME);
            assertSame(items.getFirst(), scene.getFocusOwner());
            press(scene, KeyCode.UP);
            assertSame(items.getFirst(), scene.getFocusOwner(), "focus stops at the first destination");
            return null;
        });
    }

    @Test
    void segmentedButtonArrowsMoveWithinTheSegments() throws Exception {
        FxTestSupport.onFx(() -> {
            SegmentedButton segmented = new SegmentedButton("Day", "Week", "Month");
            segmented.getButtons().getFirst().setSelected(true);
            Scene scene = show(new VBox(segmented));
            ToggleButton day = segmented.getButtons().get(0);
            ToggleButton week = segmented.getButtons().get(1);
            day.requestFocus();
            press(scene, KeyCode.RIGHT);
            assertSame(week, scene.getFocusOwner(), "Right moves to the next segment (single select)");
            segmented.setMultiSelect(true);
            press(scene, KeyCode.RIGHT);
            assertSame(segmented.getButtons().get(2), scene.getFocusOwner(), "Right moves to the next segment (multi select)");
            return null;
        });
    }

    @Test
    void timePickerDialIsKeyboardOperable() throws Exception {
        FxTestSupport.onFx(() -> {
            TimePicker picker = new TimePicker(LocalTime.of(9, 30));
            Scene scene = show(new VBox(picker));
            Node dial = picker.lookup(".time-picker-dial");
            dial.requestFocus();
            press(scene, KeyCode.UP);
            assertEquals(LocalTime.of(10, 30), picker.getValue(), "Up adds an hour in hour mode");
            press(scene, KeyCode.ENTER);
            press(scene, KeyCode.UP);
            assertEquals(LocalTime.of(10, 31), picker.getValue(), "after Enter, Up adds a minute");
            press(scene, KeyCode.PAGE_DOWN);
            assertEquals(LocalTime.of(10, 26), picker.getValue(), "Page Down removes 5 minutes");
            return null;
        });
    }

    @Test
    void tabStaysInsideAModalDialog() throws Exception {
        FxTestSupport.onFx(() -> {
            Button behind = new Button("Behind");
            StackPane root = new StackPane(new VBox(behind));
            Scene scene = show(root);
            MaterialTextField first = new MaterialTextField("First");
            MaterialTextField second = new MaterialTextField("Second");
            MaterialDialog<javafx.scene.control.ButtonType> dialog = new MaterialDialog<>("Form", null);
            dialog.setContent(new VBox(first, second));
            dialog.getButtonTypes().add(javafx.scene.control.ButtonType.OK);
            dialog.show(root);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            first.requestFocus();
            for (int i = 0; i < 6; i++) {
                press(scene, KeyCode.TAB);
                assertNotSame(behind, scene.getFocusOwner(), "Tab escaped the dialog");
                assertTrue(isInside(scene.getFocusOwner(), dialog), "focus left the dialog: " + scene.getFocusOwner());
            }
            press(scene, KeyCode.TAB, true);
            assertTrue(isInside(scene.getFocusOwner(), dialog));
            return null;
        });
    }

    private static boolean isInside(Node node, Node ancestor) {
        for (Node n = node; n != null; n = n.getParent()) {
            if (n == ancestor) {
                return true;
            }
        }
        return false;
    }

    @Test
    void textFieldHitTestingMatchesTheOffsetTextArea() throws Exception {
        FxTestSupport.onFx(() -> {
            MaterialTextField field = new MaterialTextField("Name");
            field.setLeadingIcon("person");
            field.setText("abcdefghij");
            show(new VBox(field));
            javafx.scene.control.skin.TextFieldSkin skin = (javafx.scene.control.skin.TextFieldSkin) field.getSkin();
            // Clicking just left of the text area (on the icon side) must not land past the start,
            // and clicking far right must land at the end.
            HitInfo start = skin.getIndex(53, 28);
            HitInfo end = skin.getIndex(field.getWidth() - 20, 28);
            assertEquals(0, start.getInsertionIndex(), "click at the text start");
            assertEquals(field.getLength(), end.getInsertionIndex(), "click after the text");
            return null;
        });
    }
}
