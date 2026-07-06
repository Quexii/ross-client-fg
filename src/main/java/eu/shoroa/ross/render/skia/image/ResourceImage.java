package eu.shoroa.ross.render.skia.image;

import eu.shoroa.ross.utils.io.Res;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ResourceImage extends ImageSource {
    private final String path;

    public ResourceImage(String path) {
        this.path = path;
    }

    @Override
    public void init() {
        try {
            ByteBuffer buffer = Res.resourceToBuffer(path, 8192);
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            setImage(decode(bytes));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load classpath image: " + path, e);
        }
    }

    @Override
    public String getId() {
        return path;
    }
}
