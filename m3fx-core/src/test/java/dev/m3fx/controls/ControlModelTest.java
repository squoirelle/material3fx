package dev.m3fx.controls;

import dev.m3fx.FxTestSupport;
import dev.m3fx.MaterialTheme;
import dev.m3fx.Motion;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The components follow the JavaFX control model: Control + Skin, standard base classes and semantics. */
class ControlModelTest {

    @BeforeAll
    static void setUp() throws InterruptedException {
        FxTestSupport.start();
        Motion.setEnabled(false);
    }

    private static Scene themed(Node... nodes) {
        Scene scene = new Scene(new VBox(nodes), 800, 1200);
        new MaterialTheme().apply(scene);
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        return scene;
    }

    @Test
    void everyComponentIsAControlWithASkin() throws Exception {
        FxTestSupport.onFx(() -> {
            List<Control> controls = List.of(new Switch("s"), new Chip("c"), new Card(), new ListItem("l"), new Avatar("A B"),
                    new SegmentedButton("a", "b"), new NavigationBar(), new NavigationRail(), new NavigationDrawer(),
                    new TopAppBar("t"), new BottomAppBar(), new MaterialTextField("f"), new SearchBar(), new TimePicker(),
                    new Carousel(), new MaterialDialog<>("d", null), new Snackbar("s"), new BottomSheet(), new SideSheet("s"),
                    new IconButton("menu"), new Fab("add"), new Badge("1"), new RangeSlider(), new NumberField("n"),
                    new DateRangePicker("p"), new DateRangeCalendar(), new Banner("b", null), new StatusLight(),
                    new SplitButton("s", null), new ValidationSummary(new FormPane()));
            themed(controls.toArray(Node[]::new));
            for (Control control : controls) {
                assertNotNull(control.getSkin(), control.getClass().getSimpleName() + " has no skin");
            }
            return null;
        });
    }

    @Test
    void baseClassesMatchTheirJavaFxCounterparts() {
        assertInstanceOf(ToggleButton.class, new Switch());
        assertInstanceOf(ToggleButton.class, new Chip("c"));
        assertInstanceOf(TextField.class, new MaterialTextField());
        assertInstanceOf(TextField.class, new SearchBar());
        assertInstanceOf(Separator.class, new Divider());
    }

    @Test
    void switchIsAToggleButton() throws Exception {
        FxTestSupport.onFx(() -> {
            Switch a = new Switch("A");
            Switch b = new Switch("B");
            ToggleGroup group = new ToggleGroup();
            a.setToggleGroup(group);
            b.setToggleGroup(group);
            AtomicInteger actions = new AtomicInteger();
            a.setOnAction(e -> actions.incrementAndGet());
            themed(a, b);
            a.fire();
            assertTrue(a.isSelected());
            b.fire();
            assertTrue(b.isSelected());
            assertFalse(a.isSelected(), "toggle group must deselect the other switch");
            assertEquals(1, actions.get());
            assertEquals(AccessibleRole.TOGGLE_BUTTON, a.getAccessibleRole());
            return null;
        });
    }

    @Test
    void chipVariantsBehaveLikeButtonsOrToggles() throws Exception {
        FxTestSupport.onFx(() -> {
            AtomicInteger actions = new AtomicInteger();
            Chip assist = new Chip("Assist", Chip.Variant.ASSIST);
            assist.setOnAction(e -> actions.incrementAndGet());
            Chip filter = new Chip("Filter", Chip.Variant.FILTER);
            Chip input = new Chip("Input", Chip.Variant.INPUT);
            VBox box = new VBox(assist, filter, input);
            Scene scene = new Scene(box);
            new MaterialTheme().apply(scene);
            box.applyCss();
            assist.fire();
            assertEquals(1, actions.get());
            assertFalse(assist.isSelected(), "assist chips do not toggle");
            assertEquals(AccessibleRole.BUTTON, assist.getAccessibleRole());
            filter.fire();
            assertTrue(filter.isSelected());
            assertEquals(AccessibleRole.TOGGLE_BUTTON, filter.getAccessibleRole());
            assertNotNull(filter.lookup(".chip-check"), "selected filter chips show a check mark");
            input.close();
            assertFalse(box.getChildren().contains(input), "closing an input chip removes it by default");
            return null;
        });
    }

    @Test
    void cardAndListItemActions() throws Exception {
        FxTestSupport.onFx(() -> {
            AtomicInteger clicks = new AtomicInteger();
            Card card = new Card();
            ListItem item = new ListItem("Item");
            themed(card, item);
            assertFalse(card.isFocusTraversable(), "plain cards are not focusable");
            card.setOnAction(e -> clicks.incrementAndGet());
            item.setOnAction(e -> clicks.incrementAndGet());
            assertTrue(card.isFocusTraversable());
            assertEquals(AccessibleRole.BUTTON, card.getAccessibleRole());
            card.fire();
            item.fire();
            assertEquals(2, clicks.get());
            return null;
        });
    }

    @Test
    void navigationUsesASingleSelectionModel() throws Exception {
        FxTestSupport.onFx(() -> {
            NavigationDrawer drawer = new NavigationDrawer();
            NavigationItem a = new NavigationItem("A", "home");
            NavigationItem headline = NavigationItem.headline("Section");
            NavigationItem b = new NavigationItem("B", "search");
            drawer.getItems().addAll(a, headline, b);
            themed(drawer);
            assertEquals(a, drawer.getSelectionModel().getSelectedItem(), "first destination selected by default");
            drawer.getSelectionModel().select(headline);
            assertEquals(a, drawer.getSelectedItem(), "headlines cannot be selected");
            drawer.getSelectionModel().selectNext();
            assertEquals(b, drawer.getSelectedItem(), "selectNext skips headlines");
            drawer.getItems().addFirst(new NavigationItem("Z", "star"));
            assertEquals(b, drawer.getSelectedItem(), "selection survives insertions");
            Node selectedNode = drawer.lookupAll(".navigation-item").stream()
                    .filter(n -> n.getPseudoClassStates().stream().anyMatch(p -> p.getPseudoClassName().equals("selected")))
                    .findFirst().orElseThrow();
            assertEquals(AccessibleRole.TAB_ITEM, selectedNode.getAccessibleRole());
            assertEquals(Boolean.TRUE, selectedNode.queryAccessibleAttribute(AccessibleAttribute.SELECTED));
            return null;
        });
    }

    @Test
    void segmentedButtonKeepsOneSegmentSelected() throws Exception {
        FxTestSupport.onFx(() -> {
            SegmentedButton segmented = new SegmentedButton("Day", "Week");
            themed(segmented);
            ToggleButton day = segmented.getButtons().get(0);
            day.fire();
            assertTrue(day.isSelected());
            day.fire();
            assertTrue(day.isSelected(), "single select cannot end up with nothing selected");
            segmented.setMultiSelect(true);
            day.fire();
            assertFalse(day.isSelected(), "multi select segments toggle freely");
            return null;
        });
    }

    @Test
    void passwordFieldMasksTextAndHidesItFromAccessibility() throws Exception {
        FxTestSupport.onFx(() -> {
            MaterialTextField password = MaterialTextField.password("Password");
            password.setText("secret");
            themed(password);
            assertEquals(AccessibleRole.PASSWORD_FIELD, password.getAccessibleRole());
            assertNull(password.queryAccessibleAttribute(AccessibleAttribute.TEXT));
            assertTrue(displayedText(password).contains("\u25cf\u25cf\u25cf\u25cf\u25cf\u25cf"));
            password.setPasswordVisible(true);
            password.applyCss();
            assertTrue(displayedText(password).contains("secret"), "revealed password shows its text");
            return null;
        });
    }

    private static String displayedText(Node field) {
        StringBuilder text = new StringBuilder();
        field.lookupAll(".text").forEach(n -> {
            if (n instanceof Text t && t.getText() != null) {
                text.append(t.getText()).append('|');
            }
        });
        return text.toString();
    }

    @Test
    void timePickerValueIsALocalTime() throws Exception {
        FxTestSupport.onFx(() -> {
            TimePicker picker = new TimePicker(java.time.LocalTime.of(9, 30, 45));
            assertEquals(java.time.LocalTime.of(9, 30), picker.getValue(), "seconds are dropped");
            return null;
        });
    }
}
