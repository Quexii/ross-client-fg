package eu.shoroa.ross.render.skia.image;

import eu.shoroa.ross.Client;
import io.github.humbleui.skija.*;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class GLImage extends ImageSource {
    private int texture;
    private int width;
    private int height;
    private boolean mipmaps;

    private GLTextureInfo textureInfo;
    private BackendTexture backendTexture;

    public GLImage(int texture, boolean mipmaps) {
        this(texture, Display.getWidth(), Display.getHeight(), mipmaps);
    }

    public GLImage(int texture, int width, int height, boolean mipmaps) {
        this.texture = texture;
        this.width = width;
        this.height = height;
        this.mipmaps = mipmaps;

        int binding = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        int format = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_INTERNAL_FORMAT);
        this.textureInfo = new GLTextureInfo(GL11.GL_TEXTURE_2D, texture, format);
        this.backendTexture = BackendTexture.makeGL(width, height, mipmaps, textureInfo);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, binding);
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
        setImage(Image.adoptTextureFrom(
                Client.INSTANCE.getSkia().getContext(),
                backendTexture,
                SurfaceOrigin.BOTTOM_LEFT,
                ColorType.RGBA_8888
        ));
    }

    public void refresh() {
        init();
    }

    @Override
    public String getId() {
        return "gl:" + texture;
    }
}
