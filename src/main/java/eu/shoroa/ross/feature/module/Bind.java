package eu.shoroa.ross.feature.module;

import eu.shoroa.ross.event.EventInput;

public class Bind {
    public final int key;
    public final EventInput.Type type;
    public final EventInput.Action action;

    public Bind(int key, EventInput.Type type, EventInput.Action action) {
        this.key = key;
        this.type = type;
        this.action = action;
    }
}