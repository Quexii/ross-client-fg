package eu.shoroa.ross.notification;

import eu.shoroa.ross.animate.Animate;
import eu.shoroa.ross.animate.Easing;

public class Notification {
    public final String title;
    public final String message;
    public final Type type;

    private long startTime;
    private long duration;

    private final long exitAnimTime = 200;

    public final Animate entryAnim = new Animate(200, Easing.QUART_OUT).easeIf(() -> true);
    public final Animate exitAnim = new Animate(exitAnimTime, Easing.QUART_IN).easeIf(() -> getTime() >= duration - exitAnimTime);

    public Notification(String title, String message, Type type, long duration) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return getTime() > duration;
    }

    public long getTime() {
        return System.currentTimeMillis() - startTime;
    }

    public float getProgress() {
        return Math.min(1f, getTime() / (float) duration);
    }

    public enum Type {
        INFO, SUCCESS, WARNING, ERROR
    }
}