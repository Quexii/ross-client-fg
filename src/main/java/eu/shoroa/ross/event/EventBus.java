package eu.shoroa.ross.event;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EventBus {
    private final ConcurrentHashMap<Class<?>, Listener[]> listenersByType = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Class<?>, List<Class<?>>> hierarchyCache = new ConcurrentHashMap<>();

    private static final Listener[] EMPTY = new Listener[0];

    private volatile Logger logger = Logger.getLogger("EventBus");

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void register(Object target) {
        Class<?> clazz = target.getClass();
        for (Method method : getAllMethods(clazz)) {
            Subscribe annotation = method.getAnnotation(Subscribe.class);
            if (annotation == null) continue;
            validateMethod(method);
            Consumer<Object> invoker = createInvoker(method, target);
            Listener listener = new Listener(
                target,
                invoker,
                annotation.priority(),
                annotation.receiveCanceled()
            );
            addListener(method.getParameterTypes()[0], listener);
        }
    }

    public void unregister(Object target) {
        for (Class<?> eventType : listenersByType.keySet()) {
            listenersByType.computeIfPresent(eventType, (key, current) -> {
                List<Listener> filtered = new ArrayList<>();
                boolean changed = false;
                for (Listener l : current) {
                    if (l.owner == target) {
                        changed = true;
                    } else {
                        filtered.add(l);
                    }
                }
                if (!changed) return current;
                if (filtered.isEmpty()) return EMPTY;
                return sortedArray(filtered);
            });
        }
    }

    public boolean post(Object event) {
        Objects.requireNonNull(event, "event");
        boolean isCancelable = event instanceof Cancelable;
        boolean cancelled = false;

        List<Class<?>> hierarchy = getHierarchy(event.getClass());
        for (Class<?> type : hierarchy) {
            Listener[] listeners = listenersByType.get(type);
            if (listeners == null) continue;

            for (Listener listener : listeners) {
                if (cancelled && !listener.receiveCanceled) continue;

                try {
                    listener.invoker.accept(event);
                } catch (Throwable t) {
                    if (logger != null) {
                        logger.log(Level.WARNING, "Exception while dispatching event " + event + " to listener " + listener.owner, t);
                    }
                }

                if (isCancelable && !cancelled) {
                    cancelled = ((Cancelable) event).isCanceled();
                }
            }
        }
        return cancelled;
    }

    private static class Listener {
        final Object owner;
        final Consumer<Object> invoker;
        final EventPriority priority;
        final boolean receiveCanceled;

        Listener(Object owner, Consumer<Object> invoker, EventPriority priority, boolean receiveCanceled) {
            this.owner = owner;
            this.invoker = invoker;
            this.priority = priority;
            this.receiveCanceled = receiveCanceled;
        }
    }

    private void addListener(Class<?> eventType, Listener listener) {
        listenersByType.compute(eventType, (key, current) -> {
            List<Listener> list = new ArrayList<>();
            if (current != null) Collections.addAll(list, current);
            list.add(listener);
            return sortedArray(list);
        });
        hierarchyCache.computeIfAbsent(eventType, EventBus::computeHierarchy);
    }

    private static Listener[] sortedArray(List<Listener> list) {
        list.sort(Comparator.comparingInt(l -> l.priority.ordinal()));
        return list.toArray(new Listener[0]);
    }

    private static List<Method> getAllMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Method m : current.getDeclaredMethods()) {
                if (Modifier.isPublic(m.getModifiers())) {
                    methods.add(m);
                }
            }
            current = current.getSuperclass();
        }
        return methods;
    }

    private static void validateMethod(Method method) {
        if (method.getParameterCount() != 1) {
            throw new IllegalArgumentException("Event handler must have exactly one parameter: " + method);
        }
        if (method.getReturnType() != void.class) {
            throw new IllegalArgumentException("Event handler must return void: " + method);
        }
    }

    private Consumer<Object> createInvoker(Method method, Object target) {
        try {
            Object receiver = Modifier.isStatic(method.getModifiers()) ? null : target;
            return event -> {
                try {
                    method.invoke(receiver, event);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Failed to invoke listener for " + method, e);
                }
            };
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException("Failed to create invoker for " + method, t);
        }
    }

    private List<Class<?>> getHierarchy(Class<?> eventClass) {
        return hierarchyCache.computeIfAbsent(eventClass, EventBus::computeHierarchy);
    }

    private static List<Class<?>> computeHierarchy(Class<?> eventClass) {
        List<Class<?>> hierarchy = new ArrayList<>();
        Set<Class<?>> visited = new HashSet<>();
        Deque<Class<?>> queue = new ArrayDeque<>();
        queue.add(eventClass);
        while (!queue.isEmpty()) {
            Class<?> clazz = queue.poll();
            if (!visited.add(clazz)) continue;
            hierarchy.add(clazz);
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                queue.add(superClass);
            }
            for (Class<?> iface : clazz.getInterfaces()) {
                queue.add(iface);
            }
        }
        return Collections.unmodifiableList(hierarchy);
    }

    public int getListenerCount() {
        int count = 0;
        for (Listener[] arr : listenersByType.values()) {
            count += arr.length;
        }
        return count;
    }
}