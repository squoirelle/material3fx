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
                    new ListItem("Item", "Supporting"), new SegmentedButton("A", "B"), new Avatar("A B"), new Snackbar("Hi")));
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

    private static List<String> bounds(List<javafx.scene.control.Control> controls) {
        return controls.stream().map(c -> c.getClass().getSimpleName() + " " + c.getBoundsInParent()).toList();
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
