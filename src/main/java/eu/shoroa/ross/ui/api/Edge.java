package eu.shoroa.ross.ui.api;

import org.lwjgl.util.yoga.Yoga;

public enum Edge {
    LEFT(Yoga.YGEdgeLeft),
    TOP(Yoga.YGEdgeTop),
    RIGHT(Yoga.YGEdgeRight),
    BOTTOM(Yoga.YGEdgeBottom),
    START(Yoga.YGEdgeStart),
    END(Yoga.YGEdgeEnd),
    HORIZONTAL(Yoga.YGEdgeHorizontal),
    VERTICAL(Yoga.YGEdgeVertical),
    ALL(Yoga.YGEdgeAll);

    public final int value;

    Edge(int value) {
        this.value = value;
    }
}
