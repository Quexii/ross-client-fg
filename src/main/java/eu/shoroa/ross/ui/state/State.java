package eu.shoroa.ross.ui.state;

import eu.shoroa.ross.ui.api.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class State<T> {
    private T value;
    private final List<Consumer<T>> listeners = new ArrayList<>();

    public State(T initialValue) {
        this.value = initialValue;
    }

    public T get() {
        return value;
    }

    public void set(T nextValue) {
        if (Objects.equals(value, nextValue)) {
            return;
        }
        value = nextValue;
        for (Consumer<T> listener : new ArrayList<>(listeners)) {
            listener.accept(value);
        }
    }

    public State<T> onChange(Consumer<T> listener) {
        listeners.add(listener);
        return this;
    }

    public State<T> invalidateOnChange(Node node) {
        return onChange(value -> node.markDirty());
    }
}

