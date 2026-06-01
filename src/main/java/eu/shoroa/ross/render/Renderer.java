package eu.shoroa.ross.render;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.render.filters.Filter;
import eu.shoroa.ross.render.skia.SkiaSource;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.image.ImageSource;
import eu.shoroa.ross.types.Size;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.Point;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Renderer {
    private static Canvas currentCanvas;
    private static final Map<FontCache, io.github.humbleui.skija.Font> fontCache = new HashMap<>();
    private static final Map<Integer, Image> glImageCache = new HashMap<>();

    public static void use(SkiaSource source) {
        currentCanvas = source.getCanvas();
    }

    public static void drawRect(float x, float y, float width, float height, Paint paint) {
        currentCanvas.drawRect(Rect.makeXYWH(x, y, width, height), paint);
    }

    public static void drawRRect(float x, float y, float width, float height, float radius, Paint paint) {
        currentCanvas.drawRRect(RRect.makeXYWH(x, y, width, height, radius), paint);
    }

    public static void clipRect(float x, float y, float width, float height) {
        currentCanvas.clipRect(Rect.makeXYWH(x, y, width, height));
    }

    public static void clipRRect(float x, float y, float width, float height, float radius) {
        currentCanvas.clipRRect(RRect.makeXYWH(x, y, width, height, radius), true);
    }

    public static void drawFilter(Filter filter, int filterTexture, float x, float y, float width, float height) {
        Image image = filter.imageFor(filterTexture);
        if (image == null) return;

        currentCanvas.save();
        currentCanvas.clipRect(Rect.makeXYWH(x, y, width, height));
        currentCanvas.drawImageRect(image, Rect.makeWH(Display.getWidth(), Display.getHeight()));
        currentCanvas.restore();
    }

    public static void drawFilter(Filter filter, int filterTexture, float x, float y, float width, float height, float radius) {
        Image image = filter.imageFor(filterTexture);
        if (image == null) return;

        currentCanvas.save();
        currentCanvas.clipRRect(RRect.makeXYWH(x, y, width, height, radius), true);
        currentCanvas.drawImageRect(image, Rect.makeWH(Display.getWidth(), Display.getHeight()));
        currentCanvas.restore();
    }

    public static void drawImage(ImageSource source, float x, float y) {
        Image image = source.getImage();
        if (image == null) return;

        currentCanvas.drawImageRect(image, Rect.makeXYWH(x, y, source.getWidth(), source.getHeight()));
    }

    public static void drawImage(ImageSource source, float x, float y, Paint paint) {
        Image image = source.getImage();
        if (image == null) return;

        currentCanvas.drawImageRect(image, Rect.makeWH(image.getWidth(), image.getHeight()), Rect.makeXYWH(x, y, source.getWidth(), source.getHeight()), SamplingMode.LINEAR, paint, true);
    }

    public static void drawImage(ImageSource source, float x, float y, float width, float height) {
        Image image = source.getImage();
        if (image == null) return;

        currentCanvas.drawImageRect(image, Rect.makeXYWH(x, y, width, height));
    }

    public static void drawImage(ImageSource source, float x, float y, float width, float height, Paint paint) {
        Image image = source.getImage();
        if (image == null) return;

        currentCanvas.drawImageRect(image, Rect.makeXYWH(x, y, width, height), paint);
    }

    public static void drawImageRegion(ImageSource source,
                                       float srcX, float srcY, float srcWidth, float srcHeight,
                                       float dstX, float dstY, float dstWidth, float dstHeight) {
        Image image = source.getImage();
        if (image == null) return;

        currentCanvas.drawImageRect(
                image,
                Rect.makeXYWH(srcX, srcY, srcWidth, srcHeight),
                Rect.makeXYWH(dstX, dstY, dstWidth, dstHeight)
        );
    }

    public static void drawText(String text, float x, float y, Font font, float size, Font.Align align, Paint paint) {
        drawText(text, x, y, font, size, align, paint, false);
    }

    public static void drawText(String text, float x, float y, Font font, float size, Font.Align align, Paint paint, boolean useSubpixel) {
        io.github.humbleui.skija.Font skFont = fontCache.computeIfAbsent(new FontCache(font.getTypeface(), size), fc -> new io.github.humbleui.skija.Font(fc.typeface, fc.size));

        try (TextLine line = TextLine.make(text, skFont)) {
            paint.setAntiAlias(true);

            skFont.setMetricsLinear(true);
            skFont.setSubpixel(useSubpixel);
            skFont.setEdging(useSubpixel ? FontEdging.SUBPIXEL_ANTI_ALIAS : FontEdging.ANTI_ALIAS);
            skFont.setHinting(FontHinting.NONE);

            Point[] array = new Point[line.getGlyphs().length];
            Arrays.fill(array, new Point(0, 0));

            int i = 0;
            int j = 0;
            for (float position : line.getPositions()) {
                i++;
                if (i % 2 == 0) {
                    array[j].withY(position);
                    j++;
                } else {
                    array[j].withX(position);
                }
            }

            FontMetrics metrics = skFont.getMetrics();
            float height = metrics.getDescent() - metrics.getAscent();

            float drawX = 0f, drawY = 0f;
            switch (align) {
                case TOP_LEFT:
                case CENTER_LEFT:
                case BASELINE_LEFT:
                case BOTTOM_LEFT:
                    drawX = x;
                    break;
                case TOP_CENTER:
                case CENTER:
                case BASELINE_CENTER:
                case BOTTOM_CENTER:
                    drawX = x - line.getWidth() / 2f;
                    break;
                case TOP_RIGHT:
                case CENTER_RIGHT:
                case BASELINE_RIGHT:
                case BOTTOM_RIGHT:
                    drawX = x - line.getWidth();
                    break;
            }

            switch (align) {
                case TOP_LEFT:
                case TOP_CENTER:
                case TOP_RIGHT:
                    drawY = y + height - metrics.getDescent();
                    break;
                case CENTER_LEFT:
                case CENTER:
                case CENTER_RIGHT:
                    drawY = y + height / 2f - metrics.getDescent();
                    break;
                case BASELINE_LEFT:
                case BASELINE_CENTER:
                case BASELINE_RIGHT:
                    drawY = y;
                    break;
                case BOTTOM_LEFT:
                case BOTTOM_CENTER:
                case BOTTOM_RIGHT:
                    drawY = y - metrics.getDescent();
                    break;
            }

            currentCanvas.drawTextLine(line, drawX, drawY, paint);
        }
    }

    public static Size getTextBounds(String text, Font font, float size) {
        io.github.humbleui.skija.Font skFont = fontCache.computeIfAbsent(new FontCache(font.getTypeface(), size), fc -> new io.github.humbleui.skija.Font(fc.typeface, fc.size));
        try (TextLine line = TextLine.make(text, skFont)) {
            FontMetrics metrics = skFont.getMetrics();
            return new Size(line.getWidth(), metrics.getDescent() - metrics.getAscent());
        }
    }

    public static Image fromGL(int texture) {
        return glImageCache.computeIfAbsent(texture, t -> adoptGLTexture(t, Display.getWidth(), Display.getHeight()));
    }

    public static Image adoptGLTexture(int texture, int width, int height) {
        return Image.adoptGLTextureFrom(
                Client.INSTANCE.skia.getContext(),
                texture,
                GL11.GL_TEXTURE_2D,
                width,
                height,
                GL11.GL_RGBA8,
                SurfaceOrigin.BOTTOM_LEFT,
                ColorType.RGBA_8888
        );
    }

    public static void recomputeGLTexture(int texture) {
        if (glImageCache.containsKey(texture)) {
            Image old = glImageCache.remove(texture);
            if (old != null) {
                old.close();
            }
            fromGL(texture);
        }
    }

    public static void save() {
        currentCanvas.save();
    }

    public static void saveAlpha(float alpha) {
        currentCanvas.saveLayerAlpha(null, (int) (alpha * 255));
    }

    public static void restore() {
        currentCanvas.restore();
    }

    public static void translate(float x, float y) {
        currentCanvas.translate(x, y);
    }

    public static void rotate(float degrees) {
        currentCanvas.rotate(degrees);
    }

    public static void scale(float x, float y) {
        currentCanvas.scale(x, y);
    }

    public static void scale(float x, float y, float scale) {
        currentCanvas.translate(x, y);
        currentCanvas.scale(scale, scale);
        currentCanvas.translate(-x, -y);
    }

    private static class FontCache {
        private final Typeface typeface;
        private final float size;

        private FontCache(Typeface typeface, float size) {
            this.typeface = typeface;
            this.size = size;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FontCache fontCache = (FontCache) o;
            return Float.compare(fontCache.size, size) == 0 && java.util.Objects.equals(typeface, fontCache.typeface);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(typeface, size);
        }
    }
}
