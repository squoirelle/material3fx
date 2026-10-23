package dev.m3fx.controls;

import dev.m3fx.FxTestSupport;
import dev.m3fx.MaterialTheme;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarouselTest {

    /** Without input, a carousel must settle: its items must not keep moving from pulse to pulse. */
    @Test
    void carouselIsStableWithoutInput() throws Exception {
        Carousel[] carousel = new Carousel[1];
        Stage[] stage = new Stage[1];
        FxTestSupport.onFx(() -> {
            carousel[0] = new Carousel();
            for (int i = 0; i < 6; i++) {
                StackPane item = new StackPane(new javafx.scene.control.Label("Item " + i));
                carousel[0].getItems().add(item);
            }
            carousel[0].setPrefSize(820, 200);
            // Same nesting as the demo page: FlowPane in a VBox in a fit-to-width ScrollPane.
            javafx.scene.layout.FlowPane row = new javafx.scene.layout.FlowPane(16, 16, carousel[0]);
            javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(new VBox(12, row));
            scroll.setFitToWidth(true);
            stage[0] = new Stage();
            Scene scene = new Scene(scroll, 1000, 400);
            new MaterialTheme().apply(scene);
            stage[0].setScene(scene);
            stage[0].show();
            return null;
        });
        Thread.sleep(500);
        List<Double> first = FxTestSupport.onFx(() -> positions(carousel[0]));
        Thread.sleep(500);
        List<Double> second = FxTestSupport.onFx(() -> positions(carousel[0]));
        FxTestSupport.onFx(() -> {
            stage[0].close();
            return null;
        });
        assertEquals(first, second, "carousel items moved without any input");
        assertEquals(820.0, second.getLast(), "carousel must keep its preferred width");
    }

    /** Realistic input moves the carousel: one wheel notch, a short drag, arrow keys. */
    @Test
    void carouselRespondsToRealisticInput() throws Exception {
        Carousel[] carousel = new Carousel[1];
        Stage[] stage = new Stage[1];
        FxTestSupport.onFx(() -> {
            carousel[0] = new Carousel();
            for (int i = 0; i < 6; i++) {
                carousel[0].getItems().add(new StackPane());
            }
            carousel[0].setPrefSize(820, 200);
            stage[0] = new Stage();
            stage[0].setScene(new Scene(new VBox(carousel[0]), 1000, 400));
            new MaterialTheme().apply(stage[0].getScene());
            stage[0].show();
            return null;
        });
        Thread.sleep(300);
        double start = FxTestSupport.onFx(() -> firstItemX(carousel[0]));

        // One mouse wheel notch (about 40 px) over an item.
        FxTestSupport.onFx(() -> {
            Node item = carousel[0].getChildrenUnmodifiable().get(1);
            javafx.event.Event.fireEvent(item, new javafx.scene.input.ScrollEvent(javafx.scene.input.ScrollEvent.SCROLL,
                    300, 100, 300, 100, false, false, false, false, false, false, 0, -40, 0, -40,
                    javafx.scene.input.ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                    javafx.scene.input.ScrollEvent.VerticalTextScrollUnits.NONE, 0, 0, null));
            return null;
        });
        Thread.sleep(800);
        double afterWheel = FxTestSupport.onFx(() -> firstItemX(carousel[0]));
        assertTrue(afterWheel < start - 100, "one wheel notch must move to the next item (" + start + " -> " + afterWheel + ")");

        // A short drag (60 px) back to the right.
        FxTestSupport.onFx(() -> {
            Node item = carousel[0].getChildrenUnmodifiable().get(2);
            javafx.geometry.Point2D p = carousel[0].localToScene(400, 100);
            javafx.event.Event.fireEvent(item, mouse(javafx.scene.input.MouseEvent.MOUSE_PRESSED, p.getX(), p.getY(), item));
            javafx.event.Event.fireEvent(item, mouse(javafx.scene.input.MouseEvent.MOUSE_DRAGGED, p.getX() + 60, p.getY(), item));
            javafx.event.Event.fireEvent(item, mouse(javafx.scene.input.MouseEvent.MOUSE_RELEASED, p.getX() + 60, p.getY(), item));
            return null;
        });
        Thread.sleep(800);
        double afterDrag = FxTestSupport.onFx(() -> firstItemX(carousel[0]));
        assertEquals(start, afterDrag, 1, "a short drag must move back to the previous item");

        // Arrow key.
        FxTestSupport.onFx(() -> {
            carousel[0].requestFocus();
            javafx.event.Event.fireEvent(carousel[0], new javafx.scene.input.KeyEvent(javafx.scene.input.KeyEvent.KEY_PRESSED,
                    "", "", javafx.scene.input.KeyCode.RIGHT, false, false, false, false));
            return null;
        });
        Thread.sleep(800);
        double afterKey = FxTestSupport.onFx(() -> firstItemX(carousel[0]));
        FxTestSupport.onFx(() -> {
            stage[0].close();
            return null;
        });
        assertTrue(afterKey < start - 100, "Right must move to the next item");
    }

    /** Left edge of the third item: it stays clear of both edges in these tests, so it moves by whole items. */
    private static double firstItemX(Carousel carousel) {
        carousel.layout();
        return carousel.getChildrenUnmodifiable().get(2).getLayoutX();
    }

    private static javafx.scene.input.MouseEvent mouse(javafx.event.EventType<javafx.scene.input.MouseEvent> type,
                                                       double sx, double sy, Node target) {
        return new javafx.scene.input.MouseEvent(type, sx, sy, sx, sy, javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false,
                type != javafx.scene.input.MouseEvent.MOUSE_PRESSED, new javafx.scene.input.PickResult(target, sx, sy));
    }

    /** The same check for every component: nothing may keep re-laying itself out without input. */
    @Test
    void componentsSettleWithoutInput() throws Exception {
        List<javafx.scene.control.Control> controls = new ArrayList<>();
        Stage[] stage = new Stage[1];
        FxTestSupport.onFx(() -> {
            NavigationBar bar = new NavigationBar();
            bar.getItems().addAll(new NavigationItem("A", "home"), new NavigationItem("B", "search"));
            NavigationDrawer drawer = new NavigationDrawer();
            drawer.getItems().addAll(new NavigationItem("A", "home"), NavigationItem.headline("H"), new NavigationItem("B", "star"));
            SearchBar search = new SearchBar();
            search.getTrailing().add(new Avatar("A B"));
            MaterialTextField field = new MaterialTextField("Label");
            field.setSupportingText("Help");
            controls.addAll(List.of(new Chip("Chip", "event", Chip.Variant.ASSIST), new Switch("Switch"), field, search, bar,
                    drawer, new TimePicker(), new Card(Card.Variant.ELEVATED, new javafx.scene.control.Label("Card")),
                    new ListItem("Item", "Supporting"), new SegmentedButton("A", "B"), new Avatar("A B"), new Snackbar("Hi"),
                    new RangeSlider(), new NumberField("N"), new DateRangePicker("Period"), new DateRangeCalendar(),
                    new Banner("Message", "info"), new StatusLight(StatusLight.Status.SUCCESS, "OK"),
                    new SplitButton("Export", null), new ValidationSummary()));
            StatusLight pulsing = new StatusLight(StatusLight.Status.ERROR, "Alert");
            pulsing.setPulsing(true);
            controls.add(pulsing);
            javafx.scene.layout.FlowPane row = new javafx.scene.layout.FlowPane(16, 16);
            row.getChildren().addAll(controls);
            javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(new VBox(12, row));
            scroll.setFitToWidth(true);
            stage[0] = new Stage();
            Scene scene = new Scene(scroll, 1000, 700);
            new MaterialTheme().apply(scene);
            stage[0].setScene(scene);
            stage[0].show();
            return null;
        });
        Thread.sleep(500);
        List<String> first = FxTestSupport.onFx(() -> bounds(controls));
        Thread.sleep(500);
        List<String> second = FxTestSupport.onFx(() -> bounds(controls));
        FxTestSupport.onFx(() -> {
            stage[0].close();
            return null;
        });
        assertEquals(first, second, "a component kept changing size or position without input");
    }

    /**
     * Layout geometry (position and layout size), which a layout loop would change. Not visual
     * bounds: those legitimately change while something animates (e.g. a pulsing status light).
     */
    private static List<String> bounds(List<javafx.scene.control.Control> controls) {
        return controls.stream().map(c -> String.format("%s x=%.1f y=%.1f w=%.1f h=%.1f", c.getClass().getSimpleName(),
                c.getLayoutX(), c.getLayoutY(), c.getWidth(), c.getHeight())).toList();
    }

    private static List<Double> positions(Carousel carousel) {
        List<Double> result = new ArrayList<>();
        for (Node child : carousel.getChildrenUnmodifiable()) {
            result.add(child.getLayoutX());
            result.add(child.getLayoutBounds().getWidth());
        }
        result.add(carousel.getWidth());
        return result;
    }
}
