package eu.shoroa.ross.event;

public interface Cancelable {
    boolean isCanceled();

    void setCanceled(boolean canceled);
}