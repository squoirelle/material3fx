package io.github.squoirelle.material3fx.skins;

import io.github.squoirelle.material3fx.controls.StateLayer;
import javafx.collections.ObservableList;
import javafx.scene.Node;

/** Helpers shared by the Material3FX skins. */
final class SkinSupport {

    private SkinSupport() {
    }

    /**
     * Keeps the state layer as the bottom-most child. JavaFX skins rebuild their children list
     * (e.g. when a labeled's graphic changes), so this is re-checked on every layout pass.
     */
    static void keepAtBottom(ObservableList<Node> children, StateLayer layer) {
        if (children.isEmpty() || children.getFirst() != layer) {
            children.remove(layer);
            children.addFirst(layer);
        }
    }

    static Node findChild(ObservableList<Node> children, String styleClass) {
        for (Node child : children) {
            if (child.getStyleClass().contains(styleClass)) {
                return child;
            }
        }
        return null;
    }
}
