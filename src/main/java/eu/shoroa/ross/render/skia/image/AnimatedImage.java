package eu.shoroa.ross.render.skia.image;

public class AnimatedImage extends ImageSource {
    private final String id;
    private final byte[] bytes;

    public AnimatedImage(byte[] bytes) {
        this("animated:" + System.identityHashCode(bytes), bytes);
    }

    public AnimatedImage(String id, byte[] bytes) {
        this.id = id;
        this.bytes = bytes;
    }

    @Override
    public void init() {
        decodeEncoded(bytes);
    }

    @Override
    public String getId() {
        return id;
    }
}