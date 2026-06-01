package eu.shoroa.ross.ui.api;

import org.lwjgl.util.yoga.Yoga;

public enum Overflow {
    VISIBLE(Yoga.YGOverflowVisible),
    HIDDEN(Yoga.YGOverflowHidden),
    SCROLL(Yoga.YGOverflowScroll);

    public final int value;

    Overflow(int value) {
        this.value = value;
    }
}
