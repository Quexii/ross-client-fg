package eu.shoroa.ross.event;

public class EventResize {
    public final int width;
    public final int height;

    public EventResize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
