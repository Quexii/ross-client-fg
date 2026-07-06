package eu.shoroa.ross.render.skia.font;

import eu.shoroa.ross.Client;
import io.github.humbleui.skija.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;

public class SimpleFont implements FontSource {
    private ByteBuffer buffer;
    private Typeface typeface;
    private io.github.humbleui.skija.Data data;
    private Data fontData;

    private static final Logger logger = LogManager.getLogger();

    public void init(ByteBuffer buffer) {
        this.buffer = buffer;

        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        data = io.github.humbleui.skija.Data.makeFromBytes(bytes);
        typeface = FontMgr.getDefault().makeFromData(data);
        assert typeface != null;
        fontData = new FontSource.Data(
                typeface.getVariationAxes(),
                typeface.getVariations(),
                typeface.getFamilyName(),
                typeface.getFamilyNames()
        );

        if (Client.INSTANCE.getConfig().bootstrap().verbose) {
            logger.info("Loaded stella font: {}", fontData.familyName);
            logger.info("data:");
            logger.info(" - {}", (Object[]) fontData.axes);
            logger.info(" - {}", (Object[]) fontData.variations);
            logger.info(" - {}", fontData.familyName);
            logger.info("\n");
        }
    }

    @Override
    public Typeface getTypeface() {
        return typeface;
    }
}