package eu.shoroa.ross.animate;

import java.util.function.Supplier;

public class Animate {
    private long durationMs;
    private Easing ease;
    private Supplier<Boolean> condition;
    private float value;
    
    private long lastUpdateTime;
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

    public float getValue() {
        update();
        return ease.ease(value);
    }

    public float getLinearValue() {
        update();
        return value;
    }

    private void update() {
        long now = System.currentTimeMillis();
        if (lastUpdateTime == -1) lastUpdateTime = now;
        
        long deltaMs = now - lastUpdateTime;
        lastUpdateTime = now;
        
        boolean cond = condition.get();
        float progress = deltaMs / (float) durationMs;
        
        if (cond) value += progress;
        else value -= progress;

        value = Math.max(0f, Math.min(1f, value));
        lastCondition = cond;

        if (Float.isNaN(value) || Float.isInfinite(value)) forceFinish();
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