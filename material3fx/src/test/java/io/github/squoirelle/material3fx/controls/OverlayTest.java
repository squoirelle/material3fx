package io.github.squoirelle.material3fx.controls;

import io.github.squoirelle.material3fx.FxTestSupport;
import io.github.squoirelle.material3fx.MaterialTheme;
import io.github.squoirelle.material3fx.Motion;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;

class OverlayTest {

    private Stage stage;

    @AfterEach
    void tearDown() throws Exception {
        FxTestSupport.onFx(() -> {
            if (stage != null) {
                stage.close();
            }
            Motion.setEnabled(false);
            return null;
        });
    }

    /**
     * A snackbar must only catch clicks on itself. JavaFX hit-tests a region and its children
     * separately: the overlay layer itself must contain no point (a transparent background fill
     * would make the whole window unclickable), while the snackbar, a child, stays clickable.
     */
    @Test
    void nonModalOverlayDoesNotBlockClicks() throws Exception {
        FxTestSupport.onFx(() -> {
            Scaffold s = new Scaffold();
            stage = new Stage();
            stage.setScene(new Scene(s, 800, 600));
            new MaterialTheme().apply(stage.getScene());
            stage.show();
            Snackbar.make(s, "Saved").duration(javafx.util.Duration.INDEFINITE).show();
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
            javafx.scene.Node layer = s.lookup(".md-overlay");
            assertFalse(layer.contains(100, 100), "overlay layer catches clicks outside the snackbar");
            javafx.scene.Node scrim = s.lookup(".md-scrim");
            assertFalse(scrim.isVisible() && !scrim.isMouseTransparent(), "non-modal scrim must not be pickable");
            javafx.scene.Node snackbar = s.lookup(".snackbar");
            assertTrue(snackbar.isVisible() && !snackbar.isMouseTransparent() && !layer.isMouseTransparent());
            return null;
        });
    }

    /** A drawer shown modally, then put back in the layout, must not keep its slide-out offset. */
    @Test
    void contentIsResetAfterAnimatedHide() throws Exception {
        Scaffold scaffold = FxTestSupport.onFx(() -> {
            Motion.setEnabled(true);
            Scaffold s = new Scaffold();
            stage = new Stage();
            stage.setScene(new Scene(s, 800, 600));
            new MaterialTheme().apply(stage.getScene());
            stage.show();
            NavigationDrawer drawer = new NavigationDrawer();
            drawer.getItems().add(new NavigationItem("A", "home"));
            drawer.showModal(s);
            return s;
        });
        Thread.sleep(600);
        NavigationDrawer drawer = FxTestSupport.onFx(() -> {
            NavigationDrawer d = (NavigationDrawer) scaffold.lookup(".navigation-drawer");
            d.hideModal();
            return d;
        });
        Thread.sleep(600);
        FxTestSupport.onFx(() -> {
            scaffold.setNavigation(drawer);
            assertSame(scaffold.getNavigation(), drawer);
            assertEquals(0, drawer.getTranslateX(), "drawer still translated off-screen");
            assertEquals(1, drawer.getOpacity());
            assertEquals(1, drawer.getScaleX());
            return null;
        });
    }
}
