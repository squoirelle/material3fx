package io.github.squoirelle.material3fx.controls;

import io.github.squoirelle.material3fx.FxTestSupport;
import io.github.squoirelle.material3fx.MaterialTheme;
import io.github.squoirelle.material3fx.Motion;
import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Range slider, number field, date range, banner, status light, forms, split button, adaptive scaffold. */
class NewComponentsTest {

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

    private Scene show(javafx.scene.Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        new MaterialTheme().apply(scene);
        stage = new Stage();
        stage.setScene(scene);
        stage.show();
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        return scene;
    }

    private static void key(Scene scene, KeyCode code) {
        key(scene, code, false);
    }

    private static void key(Scene scene, KeyCode code, boolean alt) {
        Node target = scene.getFocusOwner() != null ? scene.getFocusOwner() : scene.getRoot();
        Event.fireEvent(target, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, alt, false));
    }

    /** Types text into a text field like the keyboard does: through replaceText at the caret. */
    private static void type(javafx.scene.control.TextInputControl field, String text) {
        for (char c : text.toCharArray()) {
            field.replaceSelection(String.valueOf(c));
        }
    }

    private static MouseEvent mouse(javafx.event.EventType<MouseEvent> type, Node node, double localX, double localY) {
        Point2D p = node.localToScene(localX, localY);
        return new MouseEvent(type, p.getX(), p.getY(), p.getX(), p.getY(), MouseButton.PRIMARY, 1, false, false, false,
                false, true, false, false, false, false, type != MouseEvent.MOUSE_PRESSED, new PickResult(node, p.getX(), p.getY()));
    }

    // ---------- Range slider ----------

    @Test
    void rangeSliderDragKeyboardAndOrdering() throws Exception {
        FxTestSupport.onFx(() -> {
            RangeSlider slider = new RangeSlider(0, 100, 20, 80);
            slider.setPrefWidth(420);
            show(new VBox(slider), 600, 200);
            // Drag the low thumb (at 20%) to 50%.
            double y = slider.getHeight() / 2;
            double start = 10 + 10 + 0.2 * (slider.getWidth() - 20 - 20);
            double target = 10 + 10 + 0.5 * (slider.getWidth() - 20 - 20);
            Event.fireEvent(slider, mouse(MouseEvent.MOUSE_PRESSED, slider, start, y));
            assertTrue(slider.isLowValueChanging());
            Event.fireEvent(slider, mouse(MouseEvent.MOUSE_DRAGGED, slider, target, y));
            Event.fireEvent(slider, mouse(MouseEvent.MOUSE_RELEASED, slider, target, y));
            assertEquals(50, slider.getLowValue(), 1, "low thumb follows the drag");
            assertFalse(slider.isLowValueChanging());
            // Dragging the low thumb past the high one stops at the high value.
            Event.fireEvent(slider, mouse(MouseEvent.MOUSE_PRESSED, slider, target, y));
            Event.fireEvent(slider, mouse(MouseEvent.MOUSE_DRAGGED, slider, slider.getWidth() - 5, y));
            Event.fireEvent(slider, mouse(MouseEvent.MOUSE_RELEASED, slider, slider.getWidth() - 5, y));
            assertEquals(80, slider.getLowValue(), 0.001, "low never passes high");
            // Keyboard on the high thumb.
            Node high = slider.lookup(".range-slider-thumb.high");
            high.requestFocus();
            key(slider.getScene(), KeyCode.RIGHT);
            assertEquals(81, slider.getHighValue(), 0.001);
            key(slider.getScene(), KeyCode.END);
            assertEquals(100, slider.getHighValue(), 0.001);
            key(slider.getScene(), KeyCode.PAGE_DOWN);
            assertEquals(90, slider.getHighValue(), 0.001);
            slider.setSnapToTicks(true);
            slider.setMajorTickUnit(25);
            slider.adjustLowValue(37);
            assertEquals(25, slider.getLowValue(), 0.001, "snaps to ticks");
            return null;
        });
    }

    // ---------- Number field ----------

    @Test
    void numberFieldParsesLocaleStepsAndValidates() throws Exception {
        FxTestSupport.onFx(() -> {
            NumberField field = new NumberField("Altitude");
            field.setLocale(Locale.FRANCE);
            field.setMin(0);
            field.setMax(1000);
            field.setStep(10);
            Scene scene = show(new VBox(field, new Button("other")), 500, 200);
            field.requestFocus();
            type(field, "12,5");
            assertEquals(12.5, field.getValue(), 1e-9, "French decimal comma");
            type(field, "x");
            assertEquals("12,5", field.getText(), "letters are rejected");
            key(scene, KeyCode.UP);
            assertEquals(22.5, field.getValue(), 1e-9, "Up adds one step");
            key(scene, KeyCode.PAGE_DOWN);
            assertEquals(0, field.getValue(), 1e-9, "Page Down clamps at min");
            field.selectAll();
            type(field, "5000");
            assertTrue(field.isError(), "out of range shows an error while typing");
            field.commit();
            assertEquals(1000, field.getValue(), 1e-9, "commit clamps to max");
            assertFalse(field.isError());
            field.setValue(3.14159);
            assertEquals("3,14", field.getText(), "value is shown with the field's decimals");
            return null;
        });
    }

    // ---------- Date range ----------

    @Test
    void dateRangeCalendarKeyboardSelection() throws Exception {
        FxTestSupport.onFx(() -> {
            DateRangeCalendar calendar = new DateRangeCalendar();
            calendar.setFocusedDate(LocalDate.of(2026, 10, 10));
            Scene scene = show(new VBox(calendar), 500, 500);
            calendar.requestFocus();
            key(scene, KeyCode.ENTER);
            assertEquals(LocalDate.of(2026, 10, 10), calendar.getPendingStart(), "first Enter picks the start");
            assertNull(calendar.getValue());
            key(scene, KeyCode.DOWN);
            key(scene, KeyCode.RIGHT);
            key(scene, KeyCode.ENTER);
            assertEquals(new DateRange(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 18)), calendar.getValue());
            key(scene, KeyCode.PAGE_DOWN);
            assertEquals(java.time.YearMonth.of(2026, 11), calendar.getMonth(), "Page Down shows the next month");
            calendar.select(LocalDate.of(2026, 11, 20));
            calendar.select(LocalDate.of(2026, 11, 5));
            assertEquals(new DateRange(LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 20)), calendar.getValue(),
                    "end before start is reordered");
            return null;
        });
    }

    @Test
    void dateRangePickerOpensDialogAndTakesTheRange() throws Exception {
        FxTestSupport.onFx(() -> {
            DateRangePicker picker = new DateRangePicker("Period");
            Scaffold scaffold = new Scaffold(new VBox(picker));
            Scene scene = show(scaffold, 800, 700);
            picker.requestFocus();
            key(scene, KeyCode.ENTER);
            scene.getRoot().applyCss();
            DateRangeCalendar calendar = (DateRangeCalendar) scaffold.lookup(".date-range-calendar");
            assertNotNull(calendar, "Enter opens the range dialog");
            calendar.select(LocalDate.of(2026, 3, 1));
            calendar.select(LocalDate.of(2026, 3, 7));
            Button ok = (Button) scaffold.lookupAll(".button").stream()
                    .filter(n -> n instanceof Button b && "OK".equals(b.getText())).findFirst().orElseThrow();
            ok.fire();
            assertEquals(new DateRange(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 7)), picker.getValue());
            return null;
        });
    }

    // ---------- Forms ----------

    @Test
    void formValidationAndSummary() throws Exception {
        FxTestSupport.onFx(() -> {
            MaterialTextField callsign = new MaterialTextField("Callsign");
            callsign.setValidator(t -> t.length() < 3 ? "At least 3 letters" : null);
            DateRangePicker period = new DateRangePicker("Period");
            period.setRequired(true);
            NumberField crew = new NumberField("Crew");
            crew.setMax(10);
            FormPane form = new FormPane();
            ValidationSummary summary = new ValidationSummary(form);
            form.addNode(summary);
            form.addSection("Mission");
            form.addRow("Callsign", callsign).setRequired(true);
            form.addRow("Period", period);
            form.addRow(crew);
            show(new StackPane(form), 900, 700);
            assertFalse(summary.isVisible(), "no summary before validation");
            assertFalse(form.validate());
            assertEquals(2, form.getProblems().size(), "callsign and period are invalid, crew (empty) is fine");
            assertEquals("Callsign", form.getProblems().get(0).label());
            assertTrue(summary.isVisible());
            form.applyCss();
            Hyperlink first = (Hyperlink) summary.lookup(".validation-summary-link");
            first.fire();
            assertSame(callsign, form.getScene().getFocusOwner(), "clicking a problem focuses the field");
            callsign.setText("Alpha");
            period.setValue(DateRange.of(LocalDate.now()));
            assertTrue(form.validate());
            assertFalse(summary.isVisible());
            return null;
        });
    }

    @Test
    void formLabelsMoveAboveFieldsWhenNarrow() throws Exception {
        FxTestSupport.onFx(() -> {
            FormPane form = new FormPane();
            MaterialTextField field = new MaterialTextField("Name");
            FormRow row = form.addRow("Name", field);
            show(new StackPane(form), 900, 400);
            assertFalse(form.labelsOnTopProperty().get());
            assertTrue(field.getLayoutX() > 0, "label beside the field on wide forms");
            // Window resizes are applied asynchronously: resize the form directly.
            form.resize(450, form.getHeight());
            form.layout();
            assertTrue(form.labelsOnTopProperty().get());
            assertEquals(0, field.getLayoutX(), 0.5, "label above the field on narrow forms");
            assertTrue(field.getLayoutY() > 0);
            assertNotNull(row);
            return null;
        });
    }

    @Test
    void formHeightMatchesItsLabelPlacementInsideACard() throws Exception {
        FxTestSupport.onFx(() -> {
            // A card does not pass its content's bias on: it asks for the height without a width, and
            // the form must not answer for labels on top (its narrow pref width) once laid out wide.
            FormPane form = new FormPane();
            for (String label : List.of("Name", "SKU", "Location")) {
                form.addRow(label, new MaterialTextField());
            }
            Scene scene = show(new VBox(new Card(Card.Variant.OUTLINED, new VBox(form))), 900, 600);
            scene.getRoot().layout();
            assertFalse(form.labelsOnTopProperty().get());
            assertEquals(form.prefHeight(form.getWidth()), form.getHeight(), 1, "no empty space below the rows");
            return null;
        });
    }

    @Test
    void fxmlFriendlyConstructorsWork() {
        // FXML creates these with no-arg or @NamedArg constructors, then sets properties.
        FormRow row = new FormRow();
        assertFalse(row.hasLabel());
        row.setLabel("Name");
        assertTrue(row.hasLabel());
        assertTrue(row.labelNode().isVisible(), "a label set after construction shows");
        NavigationItem divider = new NavigationItem(NavigationItem.Kind.DIVIDER, null, null);
        assertFalse(divider.isDestination());
        assertEquals("Edit", setTitle(new MenuHeader(), "Edit").getTitle());
        assertTrue(new SegmentedButton().getButtons().isEmpty());
    }

    private static MenuHeader setTitle(MenuHeader header, String title) {
        header.setTitle(title);
        return header;
    }

    // ---------- Banner, status light ----------

    @Test
    void bannerCollapsesAndStatusLightReportsItsState() throws Exception {
        FxTestSupport.onFx(() -> {
            Banner banner = new Banner("Connection lost", "cloud_off");
            AtomicInteger retries = new AtomicInteger();
            banner.addAction("Retry", retries::incrementAndGet);
            StatusLight light = new StatusLight(StatusLight.Status.WARNING, "Degraded");
            show(new VBox(banner, light), 700, 300);
            assertTrue(banner.getHeight() > 40);
            ((Button) banner.lookup(".button")).fire();
            assertEquals(1, retries.get());
            banner.setShowing(false);
            banner.getScene().getRoot().layout();
            assertEquals(0, banner.getHeight(), 0.5, "hidden banners take no space");
            assertFalse(banner.isVisible());
            assertEquals("Degraded (warning)", light.queryAccessibleAttribute(javafx.scene.AccessibleAttribute.TEXT));
            assertTrue(light.getStyleClass().contains("warning"));
            return null;
        });
    }

    // ---------- Split button, menus ----------

    @Test
    void splitButtonFiresOrOpensItsMenu() throws Exception {
        FxTestSupport.onFx(() -> {
            AtomicInteger main = new AtomicInteger();
            AtomicInteger pdf = new AtomicInteger();
            MenuItem pdfItem = new MenuItem("PDF");
            pdfItem.setOnAction(e -> pdf.incrementAndGet());
            SplitButton split = new SplitButton("Export", null, pdfItem, new MenuItem("CSV"));
            split.setOnAction(e -> main.incrementAndGet());
            Scene scene = show(new VBox(split), 400, 300);
            Button leading = (Button) split.lookup(".split-button-leading");
            Button trailing = (Button) split.lookup(".split-button-trailing");
            leading.fire();
            assertEquals(1, main.get(), "the leading part fires the main action");
            assertFalse(split.isShowing());
            trailing.fire();
            assertTrue(split.isShowing(), "the trailing part opens the menu");
            assertTrue(Window.getWindows().stream().anyMatch(w -> w instanceof ContextMenu c && c.isShowing()));
            split.hide();
            leading.requestFocus();
            key(scene, KeyCode.DOWN, true);
            assertTrue(split.isShowing(), "Alt+Down opens the menu");
            split.hide();
            return null;
        });
    }

    /** A real shown menu: Material item height, and no opaque box behind the rounded popup. */
    @Test
    void shownMenusHaveMaterialItemsAndATransparentWindowRoot() throws Exception {
        ContextMenu[] menu = new ContextMenu[1];
        FxTestSupport.onFx(() -> {
            MaterialTheme theme = new MaterialTheme();
            theme.applyGlobally();
            Button owner = new Button("x");
            show(new VBox(owner), 400, 300);
            menu[0] = new ContextMenu(new MenuHeader("Edit"), new MenuItem("Cut"), new MenuItem("Copy"));
            menu[0].show(owner, javafx.geometry.Side.BOTTOM, 0, 0);
            return null;
        });
        Thread.sleep(400);
        FxTestSupport.onFx(() -> {
            Scene popup = menu[0].getScene();
            javafx.scene.layout.Region root = (javafx.scene.layout.Region) popup.getRoot();
            assertTrue(root.getBackground() == null || root.getBackground().getFills().stream()
                    .allMatch(f -> javafx.scene.paint.Color.TRANSPARENT.equals(f.getFill())), "popup root paints a box");
            java.util.List<Node> items = java.util.List.copyOf(popup.getRoot().lookupAll(".menu-item"));
            assertEquals(32, ((javafx.scene.layout.Region) items.get(0)).getHeight(), 0.5, "header height");
            assertEquals(48, ((javafx.scene.layout.Region) items.get(1)).getHeight(), 0.5, "item height");
            menu[0].hide();
            return null;
        });
    }

    @Test
    void menuHeaderIsNotSelectable() {
        MenuHeader header = new MenuHeader("Edit");
        assertTrue(header.isDisable(), "keyboard navigation skips disabled items");
        assertFalse(header.isHideOnClick());
        assertEquals("Edit", header.getTitle());
    }

    // ---------- Actions of inner buttons must not trigger the enclosing control ----------

    @Test
    void innerButtonsDoNotFireTheEnclosingControl() throws Exception {
        FxTestSupport.onFx(() -> {
            AtomicInteger card = new AtomicInteger();
            AtomicInteger item = new AtomicInteger();
            AtomicInteger field = new AtomicInteger();
            AtomicInteger search = new AtomicInteger();
            Button inCard = new Button("Open");
            Card c = new Card(Card.Variant.OUTLINED, inCard);
            c.setOnAction(e -> card.incrementAndGet());
            Switch inItem = new Switch();
            ListItem li = new ListItem("Night mode");
            li.setTrailing(inItem);
            li.setOnAction(e -> item.incrementAndGet());
            MaterialTextField text = new MaterialTextField("Name");
            text.setText("x");
            text.setClearable(true);
            text.setOnAction(e -> field.incrementAndGet());
            IconButton mic = new IconButton("mic");
            SearchBar bar = new SearchBar();
            bar.getTrailing().add(mic);
            bar.setOnAction(e -> search.incrementAndGet());
            show(new VBox(c, li, text, bar), 600, 600);
            inCard.fire();
            inItem.fire();
            ((Button) text.lookup(".md-field-trailing")).fire();
            mic.fire();
            assertEquals(0, card.get(), "button inside a card");
            assertEquals(0, item.get(), "switch inside a list item");
            assertEquals(0, field.get(), "clear button inside a text field");
            assertEquals(0, search.get(), "mic button inside a search bar");
            c.fire();
            li.fire();
            assertEquals(1, card.get(), "the card's own action still works");
            assertEquals(1, item.get(), "the item's own action still works");
            return null;
        });
    }

    @Test
    void searchSuggestionsAreAsWideAsTheBarAndFollowItsWidth() throws Exception {
        SearchBar[] bar = new SearchBar[1];
        FxTestSupport.onFx(() -> {
            bar[0] = new SearchBar("Search");
            bar[0].setSuggestionProvider(text -> List.of("Cordless drill", "Cable"));
            show(new VBox(bar[0]), 800, 400);
            return null;
        });
        // Suggestions only show for a focused bar.
        assertTrue(FxTestSupport.focus(bar[0]), "the search bar gets the focus");
        FxTestSupport.onFx(() -> {
            bar[0].setText("c");
            return null;
        });
        Thread.sleep(400);
        FxTestSupport.onFx(() -> {
            assertEquals(bar[0].getWidth(), suggestionMenu().getWidth(), 0.5, "as wide as the bar");
            // The window resizes while the suggestions show.
            bar[0].setMaxWidth(500);
            bar[0].getParent().layout();
            return null;
        });
        Thread.sleep(400);
        FxTestSupport.onFx(() -> {
            assertEquals(500, suggestionMenu().getWidth(), 0.5, "follows the bar's new width");
            bar[0].getScene().getWindow().hide();
            return null;
        });
    }

    private static javafx.scene.layout.Region suggestionMenu() {
        Window popup = Window.getWindows().stream()
                .filter(w -> w.isShowing() && w.getScene().getRoot().lookup(".search-suggestions") != null)
                .findFirst().orElseThrow();
        return (javafx.scene.layout.Region) popup.getScene().getRoot().lookup(".search-suggestions");
    }

    // ---------- Adaptive scaffold ----------

    @Test
    void adaptiveScaffoldFollowsSizeClasses() throws Exception {
        FxTestSupport.onFx(() -> {
            AdaptiveScaffold scaffold = new AdaptiveScaffold();
            for (String name : new String[] {"Map", "Units", "Alerts", "Reports"}) {
                scaffold.getNavigationItems().add(new NavigationItem(name, "home"));
            }
            scaffold.setListPane(new StackPane());
            scaffold.setDetailPane(new StackPane());
            show(scaffold, 500, 600);
            assertEquals(AdaptiveScaffold.SizeClass.COMPACT, scaffold.getSizeClass());
            assertEquals(AdaptiveScaffold.NavigationMode.BAR, scaffold.getEffectiveNavigationMode());
            assertSame(scaffold.getNavigationBar(), scaffold.getBottomBar());
            assertFalse(scaffold.isTwoPane());

            resize(scaffold, 700);
            assertEquals(AdaptiveScaffold.SizeClass.MEDIUM, scaffold.getSizeClass());
            assertSame(scaffold.getNavigationRail(), scaffold.getNavigation());
            assertNull(scaffold.getBottomBar());

            resize(scaffold, 1000);
            assertEquals(AdaptiveScaffold.SizeClass.EXPANDED, scaffold.getSizeClass());
            assertTrue(scaffold.isTwoPane(), "list and detail side by side from expanded");

            resize(scaffold, 1300);
            assertEquals(AdaptiveScaffold.NavigationMode.DRAWER, scaffold.getEffectiveNavigationMode());
            assertSame(scaffold.getNavigationDrawer(), scaffold.getNavigation());

            // One selection, whichever component shows it.
            NavigationItem alerts = scaffold.getNavigationItems().get(2);
            scaffold.getNavigationDrawer().getSelectionModel().select(alerts);
            assertSame(alerts, scaffold.getSelectedItem());
            assertSame(alerts, scaffold.getNavigationBar().getSelectedItem());

            // Too many destinations for a bar: modal drawer behind the navigation button.
            for (int i = 0; i < 6; i++) {
                scaffold.getNavigationItems().add(new NavigationItem("Extra " + i, "star"));
            }
            resize(scaffold, 500);
            assertEquals(AdaptiveScaffold.NavigationMode.MODAL_DRAWER, scaffold.getEffectiveNavigationMode());
            assertTrue(scaffold.getNavigationButton().isVisible());
            assertNull(scaffold.getBottomBar());
            return null;
        });
    }

    private void resize(AdaptiveScaffold scaffold, double width) {
        stage.setWidth(width);
        scaffold.resize(width, scaffold.getHeight());
        scaffold.getScene().getRoot().applyCss();
        scaffold.getScene().getRoot().layout();
    }
}
