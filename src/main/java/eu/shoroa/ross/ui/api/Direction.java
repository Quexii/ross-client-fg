package eu.shoroa.ross.ui.api;

import org.lwjgl.util.yoga.Yoga;

public enum Direction {
    COLUMN(Yoga.YGFlexDirectionColumn),
    COLUMN_REVERSE(Yoga.YGFlexDirectionColumnReverse),
    ROW(Yoga.YGFlexDirectionRow),
    ROW_REVERSE(Yoga.YGFlexDirectionRowReverse);

    public final int value;

    Direction(int value) {
        this.value = value;
    }
}
