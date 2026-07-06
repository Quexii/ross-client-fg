package eu.shoroa.ross.render.ui;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.render.filters.Filter;
import eu.shoroa.ross.render.skia.Skia;
import eu.shoroa.ross.render.skia.font.FontSource;
import eu.shoroa.ross.type.Size;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.Point;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import org.lwjgl.opengl.Display;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UI {
    private static final Map<FontCache, io.github.humbleui.skija.Font> fontCache = new HashMap<>();
    private static Canvas currentCanvas;

    public static void use(Skia skia) {
        currentCanvas = skia.getCanvas();
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

    public static void drawRect(float x, float y, float w, float h, Paint p) {
        currentCanvas.drawRect(Rect.makeXYWH(x, y, w, h), p);
    }

    public static void drawRRect(float x, float y, float w, float h, float r, Paint p) {
        currentCanvas.drawRRect(RRect.makeXYWH(x, y, w, h, r), p);
    }

    public static void drawRRect(float x, float y, float w, float h, float tl, float tr, float br, float bl, Paint p) {
        currentCanvas.drawRRect(RRect.makeComplexXYWH(x, y, w, h, new float[]{tl, tr, br, bl}), p);
    }

    public static void drawCircle(float x, float y, float r, Paint p) {
        currentCanvas.drawCircle(x, y, r, p);
    }

    public static void drawLine(float x1, float y1, float x2, float y2, Paint p) {
        currentCanvas.drawLine(x1, y1, x2, y2, p);
    }

    public static void drawArc(float x, float y, float w, float h, float startAngle, float sweepAngle, Paint p) {
        currentCanvas.drawArc(x, y, x + w, y + h, startAngle, sweepAngle, false, p);
    }

    public static void drawPath(Path path, Paint p) {
        currentCanvas.drawPath(path, p);
    }

    public static void clipPath(Path path) {
        currentCanvas.clipPath(path, ClipMode.INTERSECT, true);
    }

    public static void clipRect(float x, float y, float w, float h) {
        currentCanvas.clipRect(Rect.makeXYWH(x, y, w, h), ClipMode.INTERSECT, true);
    }

    public static void clipRRect(float x, float y, float w, float h, float r) {
        currentCanvas.clipRRect(RRect.makeXYWH(x, y, w, h, r), ClipMode.INTERSECT, true);
    }

    public static void saveLayerAlpha(float x, float y, float w, float h, float alpha) {
        currentCanvas.saveLayerAlpha(Rect.makeXYWH(x, y, w, h), (int) (Math.max(0f, Math.min(1f, alpha)) * 255));
    }

    public static void drawText(String text, float x, float y, FontSource font, float size, Align align, Paint paint) {
        drawText(text, x, y, font, size, align, paint, false);
    }

    public static void drawText(String text, float x, float y, FontSource font, float size, Align align, Paint paint, boolean useSubpixel) {
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

    public static Size getTextBounds(String text, FontSource font, float size) {
        io.github.humbleui.skija.Font skFont = fontCache.computeIfAbsent(new FontCache(font.getTypeface(), size), fc -> new io.github.humbleui.skija.Font(fc.typeface, fc.size));
        try (TextLine line = TextLine.make(text, skFont)) {
            FontMetrics metrics = skFont.getMetrics();
            return new Size(line.getWidth(), metrics.getDescent() - metrics.getAscent());
        }
    }

    ///////////////////////////////
    // TRANSFORM
    ///////////////////////////////

    public static void translate(float x, float y) {
        currentCanvas.translate(x, y);
    }

    public static void rotate(float degrees) {
        currentCanvas.rotate(degrees);
    }

    public static void scale(float sx, float sy) {
        currentCanvas.scale(sx, sy);
    }

    public static void skew(float sx, float sy) {
        currentCanvas.skew(sx, sy);
    }

    public static void skewFrom(float x, float y, float sx, float sy) {
        translate(x, y);
        skew(sx, sy);
        translate(-x, -y);
    }

    public static void save() {
        currentCanvas.save();
    }

    public static void restore() {
        currentCanvas.restore();
    }

    public static void scaleFrom(float x, float y, float sx, float sy) {
        translate(x, y);
        scale(sx, sy);
        translate(-x, -y);
    }

    public static void rotateAround(float x, float y, float degrees) {
        translate(x, y);
        rotate(degrees);
        translate(-x, -y);
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
