package eu.shoroa.ross.render.filters;

import eu.shoroa.ross.render.opengl.Shader;
import eu.shoroa.ross.render.skia.image.GLImage;
import io.github.humbleui.skija.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class Filter {
    protected final Shader shader;
    private final Map<Integer, Image> textures = new HashMap<>();
    private final Map<Integer, Integer> textureIds = new HashMap<>();

    private static FilterKawase kawase;

    private static final Logger LOGGER = LogManager.getLogger(Filter.class);

    public Filter(Shader shader) {
        this.shader = shader;
    }

    public void init() {
        try {
            shader.init();
            createFbos();
        } catch (IOException e) {
            LOGGER.error("Failed to load shader", e);
        }
    }

    protected abstract void createFbos();

    public abstract void capture(int texture, Object... args);

    public void resize() {
        for (Image img : textures.values()) {
            img.close();
        }
        textures.clear();
        textureIds.clear();
        createFbos();
    }

    public void putImage(int target, int texture, int width, int height) {
        Integer oldTex = textureIds.get(target);
        if (oldTex == null || oldTex != texture) {
            GLImage gl = new GLImage(texture, width, height, true);
            gl.init();
            Image oldImg = textures.put(target, gl.getImage());
            if (oldImg != null) oldImg.close();
            textureIds.put(target, texture);
        }
    }

    public Integer getTargetFor(int target) {
        return textureIds.get(target);
    }

    public Image imageFor(int texture) {
        return textures.get(texture);
    }

    public static FilterKawase kawase() {
        if (kawase == null) kawase = new FilterKawase();
        return kawase;
    }
}