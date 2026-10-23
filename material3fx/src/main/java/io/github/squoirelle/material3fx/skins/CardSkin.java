package io.github.squoirelle.material3fx.skins;

import io.github.squoirelle.material3fx.controls.Card;
import io.github.squoirelle.material3fx.controls.StateLayer;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/** Skin of {@link Card}: content inside the padding, clipped to the corners, plus a state layer when clickable. */
public class CardSkin extends SkinBase<Card> {

    private final Region clip = new Region();
    private final StateLayer stateLayer;
    private final ActionBehavior behavior;

    public CardSkin(Card card) {
        super(card);
        stateLayer = new StateLayer(card);
        stateLayer.getStyleClass().add("card-state-layer");
        card.setClip(clip);
        behavior = new ActionBehavior(card, () -> card.getOnAction() != null, card::fire);
        registerChangeListener(card.contentProperty(), o -> updateChildren());
        registerChangeListener(card.onActionProperty(), o -> updateChildren());
        updateChildren();
    }

    private void updateChildren() {
        Card card = getSkinnable();
        getChildren().clear();
        if (card.getContent() != null) {
            getChildren().add(card.getContent());
        }
        if (card.getOnAction() != null) {
            // Above the content: Material overlays the state layer on the whole card.
            getChildren().add(stateLayer);
        }
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        Card card = getSkinnable();
        Node content = card.getContent();
        if (content != null) {
            layoutInArea(content, x, y, w, h, -1, javafx.geometry.HPos.LEFT, javafx.geometry.VPos.TOP);
        }
        stateLayer.resizeRelocate(0, 0, card.getWidth(), card.getHeight());
        // The clip follows the card's own corner radii.
        CornerRadii radii = card.getBackground() == null || card.getBackground().getFills().isEmpty()
                ? CornerRadii.EMPTY : card.getBackground().getFills().getFirst().getRadii();
        clip.setBackground(new Background(new BackgroundFill(Color.BLACK, radii, null)));
        clip.resize(card.getWidth(), card.getHeight());
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        Node content = getSkinnable().getContent();
        return left + (content == null ? 0 : content.prefWidth(-1)) + right;
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        Node content = getSkinnable().getContent();
        double w = width < 0 ? -1 : Math.max(0, width - left - right);
        return top + (content == null ? 0 : content.prefHeight(w)) + bottom;
    }

    @Override
    protected double computeMinHeight(double width, double top, double right, double bottom, double left) {
        Node content = getSkinnable().getContent();
        double w = width < 0 ? -1 : Math.max(0, width - left - right);
        return top + (content == null ? 0 : content.minHeight(w)) + bottom;
    }

    @Override
    public void dispose() {
        behavior.dispose();
        getSkinnable().setClip(null);
        super.dispose();
    }
}
