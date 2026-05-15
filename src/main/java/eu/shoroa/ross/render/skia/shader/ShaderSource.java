package eu.shoroa.ross.render.skia.shader;

import io.github.humbleui.skija.RuntimeEffect;
import io.github.humbleui.skija.RuntimeEffectBuilder;
import io.github.humbleui.skija.RuntimeEffectOptions;
import io.github.humbleui.skija.Shader;

public abstract class ShaderSource {
    protected RuntimeEffect runtimeEffect;
    protected RuntimeEffectBuilder builder;
    private long startTime;
    private long time;
    private float timef;

    public ShaderSource(String source) {
        runtimeEffect = RuntimeEffect.makeForShader(source, RuntimeEffectOptions.DEFAULT);
        builder = new RuntimeEffectBuilder(runtimeEffect);
    }

    public void init() {
        startTime = System.currentTimeMillis();
    }

    public void update() {
        time = System.currentTimeMillis() - startTime;
        timef = time / 1000f;
    }

    public Shader getShader() {
        return builder.makeShader();
    }

    public long getTime() {
        return time;
    }

    public float getTimeF() {
        return timef;
    }
}
