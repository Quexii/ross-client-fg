package eu.shoroa.ross.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Setting<T> {

    private final String name;
    private final String id;
    private T value;

    private final List<Consumer<T>> listeners = new ArrayList<>();
    private Supplier<Boolean> visibility = () -> true;
    private final Type type;

    protected Setting(String name, String id, T defaultValue, Type type) {
        this.name = name;
        this.id = id;
        this.value = defaultValue;
        this.type = type;
    }

    public T get() {
        return value;
    }

    public void set(T newValue) {
        if (!Objects.equals(this.value, newValue)) {
            this.value = newValue;
            listeners.forEach(l -> l.accept(newValue));
        }
    }

    public void onChange(Consumer<T> listener) {
        listeners.add(listener);
    }

    public boolean isVisible() {
        return visibility.get();
    }

    public Setting<T> visibleWhen(Supplier<Boolean> condition) {
        this.visibility = condition;
        return this;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public enum Type {
        BOOLEAN,
        NUMBER,
        COLOR,
        MODE
    }
}