package dev.m3fx.demo;

import dev.m3fx.Density;
import dev.m3fx.MaterialTheme;
import dev.m3fx.ThemeMode;
import dev.m3fx.Typography;
import dev.m3fx.color.SeedExtractor;
import dev.m3fx.color.Variant;
import dev.m3fx.controls.Chip;
import dev.m3fx.controls.Icon;
import dev.m3fx.controls.SegmentedButton;
import dev.m3fx.controls.SideSheet;
import dev.m3fx.controls.Snackbar;
import dev.m3fx.controls.Switch;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Side sheet editing the live theme: seed, mode, variant, contrast, density. */
final class ThemeEditor {

    private static final Map<String, String> SEEDS = Map.of(
            "Baseline", "#6750A4", "Blue", "#0061A4", "Green", "#386A20", "Teal", "#006A6A",
            "Orange", "#8B5000", "Red", "#B3261E", "Pink", "#984061", "Olive", "#5B6300");
    private static final List<String> SEED_ORDER = List.of("Baseline", "Blue", "Teal", "Green", "Olive", "Orange", "Red", "Pink");

    private ThemeEditor() {
    }

    static SideSheet create(MaterialTheme theme) {
        FlowPane seeds = new FlowPane(8, 8);
        for (String name : SEED_ORDER) {
            Color color = Color.web(SEEDS.get(name));
            Button swatch = new Button();
            swatch.getStyleClass().add("seed-swatch");
            swatch.setStyle("-fx-background-color: " + SEEDS.get(name) + "; -fx-padding: 0; -fx-min-width: 40;"
                    + " -fx-pref-width: 40; -fx-min-height: 40; -fx-pref-height: 40; -fx-effect: null;");
            swatch.setTooltip(new Tooltip(name));
            Icon check = new Icon("check", 20);
            check.setFollowTextFill(false);
            check.setStyle(check.getStyle() + "-fx-fill: white;");
            check.visibleProperty().bind(theme.seedColorProperty().isEqualTo(color));
            swatch.setGraphic(check);
            swatch.setOnAction(e -> theme.setSeedColor(color));
            seeds.getChildren().add(swatch);
        }
        Button fromImage = new Button("From image...");
        fromImage.getStyleClass().addAll("text", "icon-leading");
        fromImage.setGraphic(new Icon("image", 18));
        fromImage.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
            File file = chooser.showOpenDialog(fromImage.getScene().getWindow());
            if (file != null) {
                Color seed = SeedExtractor.extract(new Image(file.toURI().toString()));
                theme.setSeedColor(seed);
                Snackbar.show(fromImage, "Seed extracted from " + file.getName());
            }
        });

        SegmentedButton mode = new SegmentedButton("Light", "Dark", "System");
        mode.getSegments().get(theme.getMode().ordinal()).setSelected(true);
        mode.getToggleGroup().selectedToggleProperty().addListener((obs, old, toggle) -> {
            if (toggle != null) {
                theme.setMode(ThemeMode.values()[mode.getSegments().indexOf((ToggleButton) toggle)]);
            }
        });
        theme.modeProperty().addListener((obs, old, value) -> mode.getSegments().get(value.ordinal()).setSelected(true));

        FlowPane variants = new FlowPane(8, 8);
        for (Variant variant : Variant.values()) {
            String label = variant.name().charAt(0) + variant.name().substring(1).toLowerCase(Locale.ROOT).replace('_', ' ');
            Chip chip = new Chip(label, Chip.Variant.FILTER);
            chip.setSelected(theme.getVariant() == variant);
            chip.setOnAction(e -> theme.setVariant(variant));
            theme.variantProperty().addListener((obs, old, value) -> chip.setSelected(value == variant));
            variants.getChildren().add(chip);
        }

        SegmentedButton contrast = new SegmentedButton("Standard", "Medium", "High");
        contrast.getSegments().getFirst().setSelected(true);
        contrast.getToggleGroup().selectedToggleProperty().addListener((obs, old, toggle) -> {
            if (toggle != null) {
                theme.setContrast(contrast.getSegments().indexOf((ToggleButton) toggle) * 0.5);
            }
        });

        SegmentedButton density = new SegmentedButton("0", "-1", "-2", "-3");
        density.getSegments().get(theme.getDensity().ordinal()).setSelected(true);
        density.getToggleGroup().selectedToggleProperty().addListener((obs, old, toggle) -> {
            if (toggle != null) {
                theme.setDensity(Density.values()[density.getSegments().indexOf((ToggleButton) toggle)]);
            }
        });

        Switch harmonize = new Switch("Harmonize custom colors");
        harmonize.selectedProperty().bindBidirectional(theme.harmonizeCustomColorsProperty());

        VBox content = new VBox(8,
                heading("Seed color"), seeds, fromImage,
                heading("Mode"), mode,
                heading("Scheme variant"), variants,
                heading("Contrast"), contrast,
                heading("Density"), density,
                heading("Custom colors"), harmonize);
        content.setPadding(new Insets(0, 24, 24, 24));
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return new SideSheet("Theme", scroll);
    }

    private static Node heading(String text) {
        Label label = Typography.TITLE_SMALL.apply(new Label(text));
        label.setPadding(new Insets(16, 0, 4, 0));
        return label;
    }
}
