package io.github.squoirelle.material3fx.demo;

import io.github.squoirelle.material3fx.controls.AdaptiveScaffold;
import io.github.squoirelle.material3fx.controls.Avatar;
import io.github.squoirelle.material3fx.controls.ListItem;
import io.github.squoirelle.material3fx.controls.NavigationItem;
import io.github.squoirelle.material3fx.controls.SegmentedButton;
import io.github.squoirelle.material3fx.controls.TopAppBar;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** An AdaptiveScaffold in a box whose width can be switched between size classes. */
final class AdaptivePage {

    private AdaptivePage() {
    }

    static Node page() {
        AdaptiveScaffold scaffold = new AdaptiveScaffold();
        for (String[] d : new String[][] {{"Inbox", "inbox"}, {"Units", "groups"}, {"Map", "map"}, {"Reports", "description"}}) {
            scaffold.getNavigationItems().add(new NavigationItem(d[0], d[1]));
        }
        scaffold.setNavigationTitle("Operations");
        TopAppBar bar = new TopAppBar("Messages");
        bar.setNavigationIcon(scaffold.getNavigationButton());
        scaffold.setTopBar(bar);

        VBox list = new VBox();
        list.getStyleClass().add("md-list");
        Label detail = new Label("Select a message");
        detail.getStyleClass().add("title-large");
        StackPane detailPane = new StackPane(detail);
        detailPane.setPadding(new Insets(24));
        String[][] messages = {{"Ada Lovelace", "Analysis of route B attached"}, {"Grace Hopper", "Compiler ready for review"},
                {"Alan Turing", "Decrypted the evening traffic"}, {"Hedy Lamarr", "Frequency hopping test passed"}};
        for (String[] m : messages) {
            ListItem item = new ListItem(m[0], m[1]);
            item.setLeading(new Avatar(m[0]));
            item.setOnAction(e -> {
                detail.setText(m[1]);
                scaffold.setDetailShown(true);
            });
            list.getChildren().add(item);
        }
        scaffold.setListPane(list);
        scaffold.setDetailPane(detailPane);

        StackPane frame = new StackPane(scaffold);
        frame.getStyleClass().add("adaptive-demo-frame");
        frame.setStyle("-fx-border-color: -md-sys-color-outline-variant; -fx-border-radius: 12; -fx-background-radius: 12;");
        frame.setPrefHeight(440);

        SegmentedButton widths = new SegmentedButton("Compact 480", "Medium 720", "Expanded 1000", "Large 1240");
        double[] values = {480, 720, 1000, 1240};
        widths.getToggleGroup().selectedToggleProperty().addListener((obs, old, toggle) -> {
            if (toggle != null) {
                double w = values[widths.getButtons().indexOf((ToggleButton) toggle)];
                frame.setMinWidth(w);
                frame.setPrefWidth(w);
                frame.setMaxWidth(w);
                scaffold.setDetailShown(false);
            }
        });
        widths.getButtons().get(2).setSelected(true);

        Label sizeClass = new Label();
        sizeClass.getStyleClass().add("on-surface-variant");
        sizeClass.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(
                () -> "Size class: " + scaffold.getSizeClass() + "  -  navigation: " + scaffold.getEffectiveNavigationMode()
                        + (scaffold.isTwoPane() ? "  -  two panes" : "  -  one pane"),
                scaffold.sizeClassProperty(), scaffold.effectiveNavigationModeProperty(), scaffold.widthProperty()));

        return Gallery.page("Adaptive layout", "AdaptiveScaffold picks the navigation component and the number of panes from "
                        + "the window width, following Material's size classes. This whole demo app uses one too: resize the window.",
                Gallery.section("Choose a width", widths, sizeClass, frame));
    }
}
