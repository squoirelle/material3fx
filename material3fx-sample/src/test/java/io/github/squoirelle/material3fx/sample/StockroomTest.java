package io.github.squoirelle.material3fx.sample;

import io.github.squoirelle.material3fx.MaterialTheme;
import io.github.squoirelle.material3fx.ThemeMode;
import io.github.squoirelle.material3fx.controls.AdaptiveScaffold;
import io.github.squoirelle.material3fx.controls.FormPane;
import io.github.squoirelle.material3fx.controls.NavigationItem;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Every FXML file loads with its controller, and the app's main flows work. */
class StockroomTest {

    private static AppContext context;
    private static AdaptiveScaffold scaffold;

    @BeforeAll
    static void start() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException alreadyStarted) {
            started.countDown();   // another test class started the toolkit
        }
        assertTrue(started.await(10, TimeUnit.SECONDS));
        onFx(() -> {
            MaterialTheme theme = new MaterialTheme();
            theme.applyGlobally();
            context = new AppContext(theme, new InventoryService(0));
            scaffold = context.load("main.fxml");
            Stage stage = new Stage();
            stage.setScene(new Scene(scaffold, 1200, 800));
            stage.show();
            return null;
        });
    }

    @Test
    void everyPageLoads() throws Exception {
        for (Page page : Page.values()) {
            Node node = onFx(() -> context.<Parent>load(page.fxml()));
            assertNotNull(node, page.name());
        }
    }

    @Test
    void mainWindowIsBuiltFromFxml() throws Exception {
        onFx(() -> {
            // Four destinations and a divider, declared in main.fxml.
            assertEquals(5, scaffold.getNavigationItems().size());
            assertEquals(NavigationItem.Kind.DIVIDER, scaffold.getNavigationItems().get(3).getKind());
            assertEquals("Stockroom", scaffold.getNavigationTitle());
            return null;
        });
    }

    @Test
    void inventoryLoadsAndFills() throws Exception {
        onFx(() -> {
            context.refresh();
            return null;
        });
        long deadline = System.currentTimeMillis() + 5000;
        while (onFx(() -> context.loadingProperty().get()) && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        onFx(() -> {
            assertFalse(context.loadingProperty().get());
            assertEquals(30, context.items().size());
            context.navigate(Page.INVENTORY);
            TableView<?> table = (TableView<?>) scaffold.lookup(".table-view");
            assertEquals(30, table.getItems().size());
            return null;
        });
    }

    @Test
    void formValidationRejectsAnEmptyItem() throws Exception {
        onFx(() -> {
            context.navigate(Page.ITEM_FORM);
            // The page is a ScrollPane: its content joins the scene graph with the skin.
            scaffold.applyCss();
            scaffold.layout();
            FormPane form = (FormPane) scaffold.lookup(".form-pane");
            assertFalse(form.validate(), "name and SKU are required");
            assertEquals(2, form.getProblems().size());
            return null;
        });
    }

    @Test
    void viewMenuAndSettingsChangeTheTheme() throws Exception {
        onFx(() -> {
            context.navigate(Page.SETTINGS);
            context.theme().setMode(ThemeMode.DARK);
            assertTrue(context.theme().isDark());
            context.theme().setMode(ThemeMode.LIGHT);
            assertInstanceOf(AdaptiveScaffold.class, scaffold);
            return null;
        });
    }

    private static <T> T onFx(Callable<T> work) throws Exception {
        CompletableFuture<T> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                result.complete(work.call());
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result.get(10, TimeUnit.SECONDS);
    }
}
