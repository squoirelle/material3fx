package dev.m3fx.controls;

import javafx.css.PseudoClass;

/** Pseudo-classes shared by the m3fx controls. */
final class PseudoClasses {

    static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    static final PseudoClass FILLED = PseudoClass.getPseudoClass("filled");
    static final PseudoClass STANDALONE = PseudoClass.getPseudoClass("standalone");
    static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");
    static final PseudoClass FOCUSED = PseudoClass.getPseudoClass("focused");
    static final PseudoClass FLOATING = PseudoClass.getPseudoClass("floating");
    static final PseudoClass EMPTY = PseudoClass.getPseudoClass("empty");
    static final PseudoClass DISABLED = PseudoClass.getPseudoClass("disabled");
    static final PseudoClass SCROLLED = PseudoClass.getPseudoClass("scrolled");
    static final PseudoClass CLICKABLE = PseudoClass.getPseudoClass("clickable");
    static final PseudoClass EXPANDED = PseudoClass.getPseudoClass("expanded");

    private PseudoClasses() {
    }
}
