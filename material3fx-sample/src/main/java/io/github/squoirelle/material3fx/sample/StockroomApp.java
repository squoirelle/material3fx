package io.github.squoirelle.material3fx.sample;

import io.github.squoirelle.material3fx.MaterialTheme;
import io.github.squoirelle.material3fx.ThemeMode;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Stockroom, a small inventory application: every screen is FXML, styled by Material3FX.
 * Run with {@code ./gradlew :material3fx-sample:run}.
 */
public class StockroomApp extends Application {

    @Override
    public void start(Stage stage) {
        MaterialTheme theme = new MaterialTheme(Color.web("#00696D"));
        theme.setMode(ThemeMode.SYSTEM);
        theme.applyGlobally();

        AppContext context = new AppContext(theme, new InventoryService(1200));
        Parent root = context.load("main.fxml");
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(StockroomApp.class.getResource("stockroom.css").toExternalForm());
        // Themed before setScene, so the first CSS pass already finds the Material colors.
        theme.apply(scene);
        stage.setScene(scene);
        stage.setTitle("Stockroom");
        stage.setMinWidth(420);
        stage.setMinHeight(480);
        stage.show();
        context.refresh();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
