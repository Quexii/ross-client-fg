package eu.shoroa.ross.render.skia.font;

import io.github.humbleui.skija.FontMgr;
import io.github.humbleui.skija.FontVariation;
import io.github.humbleui.skija.FontVariationAxis;
import io.github.humbleui.skija.Typeface;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class VariableFont implements Font {
    private ByteBuffer buffer;
    private Typeface baseFace;
    private Map<Tag, DerivedFont> typefaces = new HashMap<>();
    private io.github.humbleui.skija.Data data;
    private Font.Data fontData;

    public void init(ByteBuffer buffer) {
        this.buffer = buffer;

        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        data = io.github.humbleui.skija.Data.makeFromBytes(bytes);
        baseFace = FontMgr.getDefault().makeFromData(data);
        assert baseFace != null;
        fontData = new Font.Data(
                baseFace.getVariationAxes(),
                baseFace.getVariations(),
                baseFace.getFamilyName(),
                baseFace.getFamilyNames()
        );
    }

    private Typeface axis(String tag, int value) {
        FontVariationAxis axis = Arrays.stream(fontData.axes)
                .filter(a -> a.getTag().equals(tag))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Axis " + tag + " not found"));

        float clamped = Math.min(Math.max(value, axis.getMinValue()), axis.getMaxValue());
        FontVariation variation = new FontVariation(axis.getTag(), clamped);
        return baseFace.makeClone(variation);
    }

    public DerivedFont derive(String axis, int value) {
        return typefaces.computeIfAbsent(new Tag(axis, value), DerivedFont::new);
    }

    public DerivedFont weight(Weight weight) {
        return derive("wght", weight.value);
    }

    public DerivedFont italic(int value) {
        return derive("ital", value);
    }

    public DerivedFont opticSize(int value) {
        return derive("opsz", value);
    }

    public DerivedFont width(int value) {
        return derive("wdth", value);
    }

    @Override
    public Typeface getTypeface() {
        return baseFace;
    }

    public enum Weight {
        THIN(100),
        EXTRA_LIGHT(200),
        LIGHT(300),
        NORMAL(400),
        MEDIUM(500),
        SEMIBOLD(600),
        BOLD(700),
        HEAVY(800),
        BLACK(900);

        private final int value;

        Weight(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private class Tag {
        public final String axis;
        public final int value;

        private Tag(String axis, int value) {
            this.axis = axis;
            this.value = value;
        }
    }

    public final class DerivedFont implements Font {
        private final Typeface typeface;

        private DerivedFont(Tag tag) {
            typeface = axis(tag.axis, tag.value);
        }

        @Override
        public Typeface getTypeface() {
            return typeface;
        }
    }
}
