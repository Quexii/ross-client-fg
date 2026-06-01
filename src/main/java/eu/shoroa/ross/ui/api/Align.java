package eu.shoroa.ross.ui.api;

import org.lwjgl.util.yoga.Yoga;

public enum Align {
    AUTO(Yoga.YGAlignAuto),
    FLEX_START(Yoga.YGAlignFlexStart),
    CENTER(Yoga.YGAlignCenter),
    FLEX_END(Yoga.YGAlignFlexEnd),
    STRETCH(Yoga.YGAlignStretch),
    BASELINE(Yoga.YGAlignBaseline),
    SPACE_BETWEEN(Yoga.YGAlignSpaceBetween),
    SPACE_AROUND(Yoga.YGAlignSpaceAround);

    public final int value;

    Align(int value) {
        this.value = value;
    }
}
