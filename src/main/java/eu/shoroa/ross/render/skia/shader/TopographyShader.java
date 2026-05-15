package eu.shoroa.ross.render.skia.shader;

import eu.shoroa.ross.util.IO;
import io.github.humbleui.skija.Color;
import org.lwjgl.MemoryUtil;
import org.lwjgl.opengl.Display;

public class TopographyShader extends ShaderSource {
    private int seed = 1468280749;
    private float timeScale = 0.01f;
    private int freq = 5;
    private int octave = 2;
    private float persistence = 0.5f;
    private float lacunarity = 2.0f;
    private int color = -1;
    private float alphaMult = 1f;
    private float threshold = 0.01f;
    private float lineWidth = 0.01f;

    public TopographyShader() {
        super(MemoryUtil.decodeUTF8(IO.resourceToBufferUnsafe("/assets/rossclient/shaders/topography.sksl", 512)));
    }

    @Override
    public void update() {
        super.update();
        float r = Color.getR(color) / 255f;
        float g = Color.getG(color) / 255f;
        float b = Color.getB(color) / 255f;

        builder.setUniform("u_resolution", (float) Display.getWidth(), (float) Display.getHeight());
        builder.setUniform("u_time", getTimeF());
        builder.setUniform("u_seed", (float) seed);
        builder.setUniform("u_time_scale", timeScale);
        builder.setUniform("u_freq", freq);
        builder.setUniform("u_octave", octave);
        builder.setUniform("u_persistence", persistence);
        builder.setUniform("u_lacunarity", lacunarity);
        builder.setUniform("u_color", r, g, b);
        builder.setUniform("u_alpha_mult", alphaMult);
        builder.setUniform("u_threshold", threshold);
        builder.setUniform("u_line_width", lineWidth);
    }

    public void reset() {
        seed = 1468280749;
        timeScale = 0.01f;
        freq = 5;
        octave = 2;
        persistence = 0.5f;
        lacunarity = 2.0f;
        color = -1;
        alphaMult = 1f;
        threshold = 0.01f;
        lineWidth = 0.01f;
    }

    public TopographyShader color(int color) {
        this.color = color;
        return this;
    }

    public TopographyShader alphaMult(float alphaMult) {
        this.alphaMult = alphaMult;
        return this;
    }

    public TopographyShader threshold(float threshold) {
        this.threshold = threshold;
        return this;
    }

    public TopographyShader lineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
        return this;
    }

    public TopographyShader seed(int seed) {
        this.seed = seed;
        return this;
    }

    public TopographyShader timeScale(float timeScale) {
        this.timeScale = timeScale;
        return this;
    }

    public TopographyShader freq(int freq) {
        this.freq = freq;
        return this;
    }

    public TopographyShader octave(int octave) {
        this.octave = octave;
        return this;
    }

    public TopographyShader persistence(float persistence) {
        this.persistence = persistence;
        return this;
    }

    public TopographyShader lacunarity(float lacunarity) {
        this.lacunarity = lacunarity;
        return this;
    }
}
