package eu.shoroa.ross.render.skia.font;

import io.github.humbleui.skija.FontFamilyName;
import io.github.humbleui.skija.FontVariation;
import io.github.humbleui.skija.FontVariationAxis;
import io.github.humbleui.skija.Typeface;

public interface Font {
    Typeface getTypeface();

    class Data {
        public final FontVariationAxis[] axes;
        public final FontVariation[] variations;
        public final String familyName;
        public final FontFamilyName[] names;

        public Data(FontVariationAxis[] axes, FontVariation[] variations, String familyName, FontFamilyName[] names) {
            this.axes = axes;
            this.variations = variations;
            this.familyName = familyName;
            this.names = names;
        }
    }

    enum Align {
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT,
        CENTER_LEFT,
        CENTER,
        CENTER_RIGHT,
        BASELINE_LEFT,
        BASELINE_CENTER,
        BASELINE_RIGHT,
    }
}
