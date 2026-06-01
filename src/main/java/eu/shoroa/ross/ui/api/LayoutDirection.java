package eu.shoroa.ross.ui.api;

import org.lwjgl.util.yoga.Yoga;

public enum LayoutDirection {
    LTR(Yoga.YGDirectionLTR),
    RTL(Yoga.YGDirectionRTL),
    INHERIT(Yoga.YGDirectionInherit);

    public final int value;

    LayoutDirection(int value) {
        this.value = value;
    }
}
