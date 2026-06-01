package eu.shoroa.ross.ui.api;

import org.lwjgl.util.yoga.Yoga;

public enum PositionType {
    RELATIVE(Yoga.YGPositionTypeRelative),
    STATIC(Yoga.YGPositionTypeStatic),
    ABSOLUTE(Yoga.YGPositionTypeAbsolute);

    public final int value;

    PositionType(int value) {
        this.value = value;
    }
}

