package io.github.squoirelle.material3fx.controls;

import io.github.squoirelle.material3fx.FxTestSupport;
import io.github.squoirelle.material3fx.MaterialTheme;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Focusing an input must not move its content: the focused border must take the same space. */
class FocusLayoutTest {

    private static final PseudoClass FOCUSED = PseudoClass.getPseudoClass("focused");
    private static final PseudoClass SHOWING = PseudoClass.getPseudoClass("showing");

    @Test
    void focusDoesNotChangeInsets() throws Exception {
        FxTestSupport.onFx(() -> {
            TextField filled = new TextField();
            filled.getStyleClass().add("filled");
            TextField error = new TextField();
            error.getStyleClass().add("error");
            List<Control> controls = List.of(new TextField(), new PasswordField(), new TextArea(), filled, error,
                    new ComboBox<String>(), new ChoiceBox<String>(), new DatePicker(), new Spinner<Integer>(0, 9, 1));
            Scene scene = new Scene(new VBox(controls.toArray(Control[]::new)));
            new MaterialTheme().apply(scene);
            for (Control control : controls) {
                scene.getRoot().applyCss();
                Insets normal = control.getInsets();
                control.pseudoClassStateChanged(FOCUSED, true);
                control.pseudoClassStateChanged(SHOWING, true);
                scene.getRoot().applyCss();
                assertEquals(normal, control.getInsets(), control.getClass().getSimpleName() + " " + control.getStyleClass());
                control.pseudoClassStateChanged(FOCUSED, false);
                control.pseudoClassStateChanged(SHOWING, false);
            }
            return null;
        });
    }
}
