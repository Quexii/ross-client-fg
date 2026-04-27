package eu.shoroa.ross.animate;

import org.lwjgl.Sys;

import java.util.function.Supplier;

public class Animate {
    private long durationMs;
    private Easing ease;
    private Supplier<Boolean> condition;
    private double value;
    
    private double lastUpdateTime;
    private boolean lastCondition;

    public Animate(long durationMs, Easing ease) {
        this.durationMs = durationMs;
        this.ease = ease;
        this.condition = () -> false;
        this.value = 0f;
        this.lastUpdateTime = -1;
        this.lastCondition = false;
    }

    public Animate easeIf(Supplier<Boolean> condition) {
        this.condition = condition;
        return this;
    }

    public double getValue() {
        update();
        return ease.ease(value);
    }

    public double getLinearValue() {
        update();
        return value;
    }

    private void update() {
        double now = Sys.getTime() * 1000.0 / Sys.getTimerResolution();
        if (lastUpdateTime == -1) lastUpdateTime = now;
        
        double deltaMs = now - lastUpdateTime;
        lastUpdateTime = now;
        
        boolean cond = condition.get();
        double progress = deltaMs / durationMs;

        if (cond) value += progress;
        else value -= progress;

        value = Math.max(0f, Math.min(1f, value));
        lastCondition = cond;

        if (Double.isNaN(value) || Double.isInfinite(value)) forceFinish();
    }

    public void forceFinish() {
        value = condition.get() ? 1f : 0f;
    }

    public boolean canEase() {
        return condition.get();
    }

    public void setDuration(long durationMs) {
        this.durationMs = durationMs;
    }

    public void setEase(Easing ease) {
        this.ease = ease;
    }

    public void doEase(boolean condition) {
        this.condition = () -> condition;
    }
}