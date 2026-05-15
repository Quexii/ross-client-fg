package eu.shoroa.ross.render.skia.shader;

import io.github.humbleui.skija.RuntimeEffect;
import io.github.humbleui.skija.RuntimeEffectBuilder;
import io.github.humbleui.skija.RuntimeEffectOptions;

public class Shaders {
    public static final TopographyShader TOPOGRAPHY = new TopographyShader();

    public static void init() {
        System.out.println("Initializing shaders...");
        System.out.println("Loading topography shader...");
        TOPOGRAPHY.init();

        System.out.println("Finished initializing shaders.");
    }

    public static void update() {
    }
}
