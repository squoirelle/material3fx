package dev.m3fx;

import javafx.css.CssParser;
import javafx.css.Stylesheet;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StylesheetTest {

    @Test
    void stylesheetParsesWithoutErrors() throws IOException {
        URL url = MaterialTheme.class.getResource("m3fx.css");
        assertNotNull(url);
        CssParser.errorsProperty().clear();
        Stylesheet stylesheet = new CssParser().parse(url);
        assertTrue(stylesheet.getRules().size() > 300, "rules: " + stylesheet.getRules().size());
        assertTrue(CssParser.errorsProperty().isEmpty(), () -> "CSS errors: " + CssParser.errorsProperty());
    }
}
