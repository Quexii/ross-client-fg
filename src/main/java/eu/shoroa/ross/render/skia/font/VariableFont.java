package eu.shoroa.ross.render.skia.font;

import io.github.humbleui.skija.FontMgr;
import io.github.humbleui.skija.FontVariation;
import io.github.humbleui.skija.FontVariationAxis;
import io.github.humbleui.skija.Typeface;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VariableFont implements Font {
    private ByteBuffer buffer;
    private Typeface baseFace;
    private Map<Tags, DerivedFont> typefaces = new HashMap<>();
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

        System.out.println("Loaded variable font: " + fontData.familyName);
        System.out.println("Available axes:");
        for (FontVariationAxis axis : fontData.axes) {
            System.out.println(" - " + axis.getTag() + ": " + axis.getMinValue() + " to " + axis.getMaxValue() + " (default: " + axis.getDefaultValue() + ")");
        }
        System.out.println();
    }

    private Typeface createTypeface(Tags tags) {
        FontVariation[] variations = tags.values.entrySet().stream().map(entry -> {
            String tag = entry.getKey();
            int value = entry.getValue();
            FontVariationAxis axis = Arrays.stream(fontData.axes)
                    .filter(a -> a.getTag().equals(tag))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Axis " + tag + " not found"));

            float clamped = Math.min(Math.max(value, axis.getMinValue()), axis.getMaxValue());
            return new FontVariation(axis.getTag(), clamped);
        }).toArray(FontVariation[]::new);

        return baseFace.makeClone(variations);
    }

    public DerivedFont derive(String axis, int value) {
        return derive(new Tags(Collections.singletonMap(axis, value)));
    }

    private DerivedFont derive(Tags tags) {
        return typefaces.computeIfAbsent(tags, DerivedFont::new);
    }

    public DerivedFont weight(Weight weight) {
        return derive("wght", weight.value);
    }

    public DerivedFont weight(int value) {
        return derive("wght", value);
    }

    public DerivedFont italic(int value) {
        return derive("ital", value);
    }

    public DerivedFont opticSize(OpticSize opsz) {
        return derive("opsz", opsz.value);
    }

    public DerivedFont opticSize(int value) {
        return derive("opsz", value);
    }

    public DerivedFont width(int value) {
        return derive("wdth", value);
    }

    public DerivedFont fill(boolean value) {
        return derive("FILL", value ? 1 : 0);
    }

    public DerivedFont grade(int value) {
        return derive("GRAD", value);
    }

    public DerivedFont slant(int value) {
        return derive("slnt", value);
    }

    public DerivedFont roundness(int value) {
        return derive("ROND", value);
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

    public enum OpticSize {
        P20(20),
        P24(24),
        P40(40),
        P48(48);

        private final int value;

        OpticSize(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static class Tags {
        public final Map<String, Integer> values;

        private Tags(Map<String, Integer> values) {
            this.values = Collections.unmodifiableMap(new HashMap<>(values));
        }

        public Tags with(String axis, int value) {
            Map<String, Integer> newValues = new HashMap<>(this.values);
            newValues.put(axis, value);
            return new Tags(newValues);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Tags tags = (Tags) o;
            return Objects.equals(values, tags.values);
        }

        @Override
        public int hashCode() {
            return Objects.hash(values);
        }
    }

    public final class DerivedFont implements Font {
        private final Typeface typeface;
        private final Tags tags;

        private DerivedFont(Tags tags) {
            this.tags = tags;
            typeface = createTypeface(tags);
        }

        public DerivedFont derive(String axis, int value) {
            return VariableFont.this.derive(tags.with(axis, value));
        }

        public DerivedFont weight(Weight weight) {
            return derive("wght", weight.value);
        }

        public DerivedFont weight(int value) {
            return derive("wght", value);
        }

        public DerivedFont italic(int value) {
            return derive("ital", value);
        }

        public DerivedFont opticSize(OpticSize opsz) {
            return derive("opsz", opsz.value);
        }

        public DerivedFont opticSize(int value) {
            return derive("opsz", value);
        }

        public DerivedFont width(int value) {
            return derive("wdth", value);
        }

        public DerivedFont fill(boolean value) {
            return derive("FILL", value ? 1 : 0);
        }

        public DerivedFont grade(int value) {
            return derive("GRAD", value);
        }

        public DerivedFont slant(int value) {
            return derive("slnt", value);
        }

        public DerivedFont roundness(int value) {
            return derive("ROND", value);
        }

        @Override
        public Typeface getTypeface() {
            return typeface;
        }
    }
}
