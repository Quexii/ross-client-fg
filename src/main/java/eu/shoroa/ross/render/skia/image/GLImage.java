package eu.shoroa.ross.render.skia.image;

import eu.shoroa.ross.render.Renderer;
import org.lwjgl.opengl.Display;

public class GLImage extends ImageSource {
    private int texture;
    private int width;
    private int height;

    public GLImage(int texture) {
        this(texture, Display.getWidth(), Display.getHeight());
    }

    public GLImage(int texture, int width, int height) {
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

    public void setTexture(int texture) {
        this.texture = texture;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void init() {
        setImage(Renderer.adoptGLTexture(texture, width, height));
    }

    public void refresh() {
        init();
    }

    @Override
    public String getId() {
        return "gl:" + texture;
    }
}

