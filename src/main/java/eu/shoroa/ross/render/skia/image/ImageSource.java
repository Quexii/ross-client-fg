package eu.shoroa.ross.render.skia.image;

import eu.shoroa.ross.types.Size;
import io.github.humbleui.skija.Image;
import org.jetbrains.annotations.Nullable;

public abstract class ImageSource implements AutoCloseable {
    private volatile Image image;
    private volatile int width;
    private volatile int height;

    public abstract void init();

    public abstract String getId();

    @Nullable
    public final Image getImage() {
        return image;
    }

    public final boolean isLoaded() {
        return image != null;
    }

    public final int getWidth() {
        return width;
    }

    public final int getHeight() {
        return height;
    }

    public final Size getSize() {
        return new Size(width, height);
    }

    public synchronized void reload() {
        close();
        init();
    }

    protected final synchronized void setImage(@Nullable Image next) {
        Image previous = image;
        image = next;

        if (next == null) {
            width = 0;
            height = 0;
        } else {
            width = next.getWidth();
            height = next.getHeight();
        }

        if (previous != null && previous != next) {
            previous.close();
        }
    }

    protected final Image decode(byte[] bytes) {
        Image decoded = Image.makeDeferredFromEncodedBytes(bytes);
        if (decoded == null) {
            throw new IllegalArgumentException("Failed to decode encoded image bytes for " + getId());
        }
        return decoded;
    }

    @Override
    public synchronized void close() {
        if (image != null) {
            image.close();
            image = null;
            width = 0;
            height = 0;
        }
    }
}

