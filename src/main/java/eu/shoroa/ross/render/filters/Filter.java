package eu.shoroa.ross.render.filters;

import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.gl.Shader;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.Path;
import io.github.humbleui.skija.PathBuilder;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Filter {
    protected final Shader shader;
    private final Map<Integer, Image> textures = new HashMap<>();

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
        createFbos();
        textures.clear();
    }

    public void putImage(int target, int texture) {
        textures.computeIfAbsent(target, t -> Renderer.adoptGLTexture(texture));
    }

    public Image imageFor(int texture) {
        return textures.get(texture);
    }

    public static FilterKawase kawase() {
        if (kawase == null) kawase = new FilterKawase();
        return kawase;
    }
}
