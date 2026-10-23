/**
 * Stockroom: a small inventory application built with Material3FX and FXML.
 */
module io.github.squoirelle.material3fx.sample {
    requires io.github.squoirelle.material3fx;
    requires javafx.fxml;

    // FXML injects the controllers' @FXML fields and calls their handlers.
    opens io.github.squoirelle.material3fx.sample to javafx.fxml;
    exports io.github.squoirelle.material3fx.sample;
}
