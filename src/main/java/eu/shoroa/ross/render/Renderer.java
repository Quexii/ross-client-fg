package eu.shoroa.ross.render;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.render.filters.Filter;
import eu.shoroa.ross.render.skia.SkiaSource;
import eu.shoroa.ross.render.skia.font.Font;
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
import java.util.function.Consumer;

public class Renderer {
    private static Canvas currentCanvas;
    private static final Map<FontCache, io.github.humbleui.skija.Font> fontCache = new HashMap<>();
    private static final Map<Integer, Image> glImageCache = new HashMap<>();

    public static void use(SkiaSource source) {
        currentCanvas = source.getCanvas();
    }

    public static void drawRect(float x, float y, float width, float height, Consumer<Paint> paint) {
        try (Paint p = new Paint()) {
            paint.accept(p);
            currentCanvas.drawRect(Rect.makeXYWH(x, y, width, height), p);
        }
    }

    public static void drawRRect(float x, float y, float width, float height, float radius, Consumer<Paint> paint) {
        try (Paint p = new Paint()) {
            paint.accept(p);
            currentCanvas.drawRRect(RRect.makeXYWH(x, y, width, height, radius), p);
        }
    }


    public static void drawFilter(Filter filter, int filterTexture, float x, float y, float width, float height) {
        Image image = filter.imageFor(filterTexture);

        currentCanvas.save();
        currentCanvas.clipRect(Rect.makeXYWH(x, y, width, height));
        currentCanvas.drawImageRect(image, Rect.makeWH(Display.getWidth(), Display.getHeight()));
        currentCanvas.restore();
    }

    public static void drawFilter(Filter filter, int filterTexture, float x, float y, float width, float height, float radius) {
        Image image = filter.imageFor(filterTexture);

        currentCanvas.save();
        currentCanvas.clipRRect(RRect.makeXYWH(x, y, width, height, radius));
        currentCanvas.drawImageRect(image, Rect.makeWH(Display.getWidth(), Display.getHeight()));
        currentCanvas.restore();
    }

    public static void drawText(String text, float x, float y, Font font, float size, Font.Align align, Consumer<Paint> paint) {
        io.github.humbleui.skija.Font skFont = fontCache.computeIfAbsent(new FontCache(font.getTypeface(), size), fc -> new io.github.humbleui.skija.Font(fc.typeface, fc.size));
        TextLine line = TextLine.make(text, skFont);

        try (Paint p = new Paint()) {
            p.setAntiAlias(true);
            paint.accept(p);

            skFont.setMetricsLinear(true);
            skFont.setSubpixel(true);
            skFont.setEdging(FontEdging.SUBPIXEL_ANTI_ALIAS);
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

            currentCanvas.drawTextLine(line, drawX, drawY, p);
        }
    }

    public static Size getTextBounds(String text, Font font, float size) {
        io.github.humbleui.skija.Font skFont = fontCache.computeIfAbsent(new FontCache(font.getTypeface(), size), fc -> new io.github.humbleui.skija.Font(fc.typeface, fc.size));
        TextLine line = TextLine.make(text, skFont);
        FontMetrics metrics = skFont.getMetrics();

        return new Size(line.getWidth(), metrics.getDescent() - metrics.getAscent());
    }

    public static Image fromGL(int texture) {
        return glImageCache.computeIfAbsent(texture, Renderer::adoptGLTexture);
    }

    public static Image adoptGLTexture(int texture) {
        int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);

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
            glImageCache.remove(texture);
            fromGL(texture);
        }
    }

    private static class FontCache {
        private final Typeface typeface;
        private final float size;

        private FontCache(Typeface typeface, float size) {
            this.typeface = typeface;
            this.size = size;
        }
    }
}
