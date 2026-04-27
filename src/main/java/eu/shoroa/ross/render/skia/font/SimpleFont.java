package eu.shoroa.ross.render.skia.font;

import io.github.humbleui.skija.*;

import java.nio.ByteBuffer;

public class SimpleFont implements Font {
    private ByteBuffer buffer;
    private Typeface typeface;
    private io.github.humbleui.skija.Data data;
    private Data fontData;

    public void init(ByteBuffer buffer) {
        this.buffer = buffer;

        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        data = io.github.humbleui.skija.Data.makeFromBytes(bytes);
        typeface = FontMgr.getDefault().makeFromData(data);
        assert typeface != null;
        fontData = new Font.Data(
                typeface.getVariationAxes(),
                typeface.getVariations(),
                typeface.getFamilyName(),
                typeface.getFamilyNames()
        );

        System.out.println("Loaded simple font: " + fontData.familyName);
        System.out.println("data:");
            System.out.println(" - " + fontData.axes);
            System.out.println(" - " + fontData.variations);
            System.out.println(" - " + fontData.familyName);
        System.out.println();
    }

    @Override
    public Typeface getTypeface() {
        return typeface;
    }
}
