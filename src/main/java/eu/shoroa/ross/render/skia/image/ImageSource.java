package eu.shoroa.ross.render.skia.image;

import eu.shoroa.ross.type.Size;
import io.github.humbleui.skija.Bitmap;
import io.github.humbleui.skija.Codec;
import io.github.humbleui.skija.Data;
import io.github.humbleui.skija.Image;
import org.jetbrains.annotations.Nullable;

public abstract class ImageSource implements AutoCloseable {
    private volatile Image image;
    private volatile int width;
    private volatile int height;

    private Codec codec;
    private Bitmap animationBitmap;
    private int[] frameDurationsMs;

    private int frameCount;
    private int currentFrame;

    private int lastDecodedFrame;

    private int repetitionCount;
    private int completedLoops;

    private long nextFrameTimeNanos;
    private boolean animationFinished;

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

    public final boolean isAnimated() {
        return codec != null && frameCount > 1;
    }

    public final int getFrameCount() {
        return frameCount;
    }

    public final int getCurrentFrame() {
        return currentFrame;
    }

    public void tick() {
        synchronized (this) {
            if (codec == null || animationBitmap == null || frameDurationsMs == null || frameCount <= 1 || animationFinished) {
                return;
            }

            long now = System.nanoTime();

            if (now < nextFrameTimeNanos) {
                return;
            }

            while (now >= nextFrameTimeNanos) {
                currentFrame++;

                if (currentFrame >= frameCount) {
                    currentFrame = 0;
                    completedLoops++;

                    if (repetitionCount >= 0 && completedLoops > repetitionCount) {
                        animationFinished = true;
                        currentFrame = frameCount - 1;
                        decodeAnimationFrameLocked(currentFrame);
                        return;
                    }
                }

                nextFrameTimeNanos += frameDurationsMs[currentFrame] * 1_000_000L;

                if (now < nextFrameTimeNanos) {
                    decodeAnimationFrameLocked(currentFrame);
                    return;
                }
            }
        }
    }

    protected final synchronized void decodeEncoded(byte[] bytes) {
        clearAnimation();

        Data data = null;
        Codec detectedCodec = null;

        try {
            data = Data.makeFromBytes(bytes);
            detectedCodec = Codec.makeFromData(data);

            if (detectedCodec != null && detectedCodec.getFrameCount() > 1) {
                setupAnimation(detectedCodec);
                detectedCodec = null;
                return;
            }

            if (detectedCodec != null) {
                detectedCodec.close();
                detectedCodec = null;
            }

            setImage(decode(bytes));
        } finally {
            if (detectedCodec != null) {
                detectedCodec.close();
            }

            if (data != null) {
                data.close();
            }
        }
    }

    private void setupAnimation(Codec newCodec) {
        codec = newCodec;

        frameCount = codec.getFrameCount();
        frameDurationsMs = new int[frameCount];

        for (int i = 0; i < frameCount; i++) {
            int duration = codec.getFrameInfo(i).getDuration();
            frameDurationsMs[i] = Math.max(duration, 10);
        }

        repetitionCount = codec.getRepetitionCount();

        animationBitmap = new Bitmap();
        animationBitmap.allocPixels(codec.getImageInfo());

        currentFrame = 0;
        lastDecodedFrame = -1;
        completedLoops = 0;
        animationFinished = false;

        decodeAnimationFrameLocked(0);

        nextFrameTimeNanos = System.nanoTime() + frameDurationsMs[0] * 1_000_000L;
    }

    private void decodeAnimationFrameLocked(int frame) {
        codec.readPixels(animationBitmap, frame, lastDecodedFrame);

        Image next = Image.makeFromBitmap(animationBitmap);

        if (next == null) {
            throw new IllegalStateException("Failed to decode animation frame " + frame + " for " + getId());
        }

        setImage(next);

        lastDecodedFrame = frame;
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

    private void clearAnimation() {
        if (codec != null) {
            codec.close();
            codec = null;
        }

        if (animationBitmap != null) {
            animationBitmap.close();
            animationBitmap = null;
        }

        frameDurationsMs = null;

        frameCount = 0;
        currentFrame = 0;
        lastDecodedFrame = -1;

        repetitionCount = 0;
        completedLoops = 0;

        nextFrameTimeNanos = 0L;
        animationFinished = false;
    }

    @Override
    public synchronized void close() {
        clearAnimation();

        if (image != null) {
            image.close();
            image = null;
            width = 0;
            height = 0;
        }
    }
}