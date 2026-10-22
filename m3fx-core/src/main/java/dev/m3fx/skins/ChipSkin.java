package dev.m3fx.skins;

import dev.m3fx.controls.Chip;
import dev.m3fx.controls.Icon;
import dev.m3fx.controls.StateLayer;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.skin.ToggleButtonSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/**
 * Skin of {@link Chip}: state layer, label and graphic (from {@link ToggleButtonSkin}), plus a
 * check mark for selected filter chips and a close button for input chips.
 */
public class ChipSkin extends ToggleButtonSkin {

    private static final PseudoClass LEADING = PseudoClass.getPseudoClass("leading");
    private static final PseudoClass TRAILING = PseudoClass.getPseudoClass("trailing");
    private static final PseudoClass AVATAR = PseudoClass.getPseudoClass("avatar");
    private static final double GAP = 8;

    private final StateLayer stateLayer;
    private final Icon check = new Icon("check", 18);
    private final Icon closeIcon = new Icon("close", 18);
    private final StackPane closeButton = new StackPane(closeIcon);
    private final EventHandler<KeyEvent> deleteHandler = this::onKeyPressed;

    public ChipSkin(Chip chip) {
        super(chip);
        stateLayer = new StateLayer(chip);
        check.getStyleClass().add("chip-check");
        closeIcon.getStyleClass().add("chip-close-icon");
        closeIcon.setFollowTextFill(false);
        closeButton.getStyleClass().add("chip-close");
        check.setManaged(false);
        closeButton.setManaged(false);
        // Clicks on the close button must not reach the chip's button behavior.
        for (var type : java.util.List.of(MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED, MouseEvent.MOUSE_CLICKED)) {
            closeButton.addEventHandler(type, e -> {
                if (type == MouseEvent.MOUSE_CLICKED && !chip.isDisabled()) {
                    chip.close();
                }
                e.consume();
            });
        }
        chip.addEventHandler(KeyEvent.KEY_PRESSED, deleteHandler);
        registerChangeListener(chip.selectedProperty(), o -> update());
        registerChangeListener(chip.variantProperty(), o -> update());
        registerChangeListener(chip.graphicProperty(), o -> update());
        update();
    }

    private void onKeyPressed(KeyEvent e) {
        Chip chip = (Chip) getSkinnable();
        if ((e.getCode() == KeyCode.DELETE || e.getCode() == KeyCode.BACK_SPACE) && chip.getVariant() == Chip.Variant.INPUT) {
            chip.close();
            e.consume();
        }
    }

    private boolean showsCheck() {
        Chip chip = (Chip) getSkinnable();
        return chip.getVariant() == Chip.Variant.FILTER && chip.isSelected();
    }

    private boolean showsClose() {
        return ((Chip) getSkinnable()).getVariant() == Chip.Variant.INPUT;
    }

    private void update() {
        Chip chip = (Chip) getSkinnable();
        Node graphic = chip.getGraphic();
        boolean check = showsCheck();
        if (graphic != null) {
            // The check mark replaces the leading graphic of selected filter chips.
            graphic.setVisible(!check);
        }
        chip.pseudoClassStateChanged(LEADING, check || graphic != null);
        chip.pseudoClassStateChanged(TRAILING, showsClose());
        chip.pseudoClassStateChanged(AVATAR, !check && graphic instanceof dev.m3fx.controls.Avatar);
        updateChildren();
        chip.requestLayout();
    }

    @Override
    protected void updateChildren() {
        super.updateChildren();
        if (stateLayer == null) {
            return;
        }
        getChildren().addFirst(stateLayer);
        if (showsCheck()) {
            getChildren().add(check);
        }
        if (showsClose()) {
            getChildren().add(closeButton);
        }
    }

    private double leadingExtra() {
        // Without a graphic, the check mark needs its own slot before the text.
        return showsCheck() && getSkinnable().getGraphic() == null ? 18 + GAP : 0;
    }

    private double trailingExtra() {
        return showsClose() ? GAP + 18 : 0;
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        SkinSupport.keepAtBottom(getChildren(), stateLayer);
        double lead = leadingExtra();
        double trail = trailingExtra();
        super.layoutChildren(x + lead, y, Math.max(0, w - lead - trail), h);
        stateLayer.resizeRelocate(0, 0, getSkinnable().getWidth(), getSkinnable().getHeight());
        if (getChildren().contains(check)) {
            Node graphic = getSkinnable().getGraphic();
            double cx;
            if (graphic != null) {
                Bounds g = graphic.getBoundsInParent();
                cx = g.getMinX() + g.getWidth() / 2;
            } else {
                cx = x + 9;
            }
            Bounds b = check.getLayoutBounds();
            check.relocate(cx - b.getWidth() / 2, y + (h - b.getHeight()) / 2);
        }
        if (getChildren().contains(closeButton)) {
            closeButton.resizeRelocate(x + w - 18, y + (h - 18) / 2, 18, 18);
        }
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return super.computePrefWidth(height, top, right, bottom, left) + leadingExtra() + trailingExtra();
    }

    @Override
    protected double computeMinWidth(double height, double top, double right, double bottom, double left) {
        return computePrefWidth(height, top, right, bottom, left);
    }

    @Override
    public void dispose() {
        getSkinnable().removeEventHandler(KeyEvent.KEY_PRESSED, deleteHandler);
        getChildren().removeAll(stateLayer, check, closeButton);
        super.dispose();
    }
}
