package io.github.squoirelle.material3fx.sample;

import io.github.squoirelle.material3fx.Density;
import io.github.squoirelle.material3fx.MaterialTheme;
import io.github.squoirelle.material3fx.ThemeMode;
import io.github.squoirelle.material3fx.controls.SegmentedButton;
import io.github.squoirelle.material3fx.controls.Switch;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import javafx.util.StringConverter;

import java.util.Locale;

/** Appearance settings, bound to the application's theme: every change applies at once. */
public class SettingsController {

    private final MaterialTheme theme;

    @FXML private SegmentedButton mode;
    @FXML private ColorPicker seed;
    @FXML private Switch systemAccent;
    @FXML private SegmentedButton contrast;
    @FXML private ComboBox<Density> density;

    public SettingsController(AppContext context) {
        this.theme = context.theme();
    }

    @FXML
    private void initialize() {
        // Theme mode: the segment follows the theme, which the View menu can change too.
        Runnable selectMode = () -> select(mode, theme.getMode().name());
        selectMode.run();
        theme.modeProperty().addListener(o -> selectMode.run());
        mode.getToggleGroup().selectedToggleProperty().addListener((obs, old, toggle) -> {
            if (toggle != null) {
                theme.setMode(ThemeMode.valueOf((String) toggle.getUserData()));
            }
        });

        seed.valueProperty().bindBidirectional(theme.seedColorProperty());
        systemAccent.selectedProperty().bindBidirectional(theme.useSystemAccentProperty());
        seed.disableProperty().bind(systemAccent.selectedProperty());

        Runnable selectContrast = () -> contrast.getButtons().forEach(
                b -> b.setSelected(Double.parseDouble((String) b.getUserData()) == theme.getContrast()));
        selectContrast.run();
        theme.contrastProperty().addListener(o -> selectContrast.run());
        contrast.getToggleGroup().selectedToggleProperty().addListener((obs, old, toggle) -> {
            if (toggle != null) {
                theme.setContrast(Double.parseDouble((String) toggle.getUserData()));
            }
        });

        density.getItems().setAll(Density.values());
        density.setConverter(new StringConverter<>() {
            @Override
            public String toString(Density value) {
                return value == null ? "" : value.name().charAt(0) + value.name().substring(1).toLowerCase(Locale.ROOT);
            }

            @Override
            public Density fromString(String text) {
                return Density.valueOf(text.toUpperCase(Locale.ROOT));
            }
        });
        density.valueProperty().bindBidirectional(theme.densityProperty());
    }

    private static void select(SegmentedButton buttons, String userData) {
        for (ToggleButton button : buttons.getButtons()) {
            if (userData.equals(button.getUserData())) {
                button.setSelected(true);
            }
        }
    }
}
