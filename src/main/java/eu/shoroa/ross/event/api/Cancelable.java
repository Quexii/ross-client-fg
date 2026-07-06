package eu.shoroa.ross.event.api;

public interface Cancelable {
    boolean isCanceled();

    void setCanceled(boolean canceled);
}