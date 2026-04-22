package eu.shoroa.ross.event;

public class EventInput {
    public final int value;
    public final Type type;
    public final Action action;

    public EventInput(int value, Type type, Action action) {
        this.value = value;
        this.type = type;
        this.action = action;
    }

    public enum Type {
        KEYBOARD, MOUSE
    }

    public enum Action {
        PRESS, RELEASE, HOLD
    }
}
