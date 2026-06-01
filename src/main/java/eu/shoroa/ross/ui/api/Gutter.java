package eu.shoroa.ross.ui.api;

import org.lwjgl.util.yoga.Yoga;

public enum Gutter {
    COLUMN(Yoga.YGGutterColumn),
    ROW(Yoga.YGGutterRow),
    ALL(Yoga.YGGutterAll);

    public final int value;

    Gutter(int value) {
        this.value = value;
    }
}
