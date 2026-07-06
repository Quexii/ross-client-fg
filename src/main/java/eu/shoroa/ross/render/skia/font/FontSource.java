package eu.shoroa.ross.render.skia.font;

import io.github.humbleui.skija.FontFamilyName;
import io.github.humbleui.skija.FontVariation;
import io.github.humbleui.skija.FontVariationAxis;
import io.github.humbleui.skija.Typeface;

import java.nio.ByteBuffer;

public interface FontSource {
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
}