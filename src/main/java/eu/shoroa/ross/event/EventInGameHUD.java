package eu.shoroa.ross.event;

public abstract class EventInGameHUD implements Cancelable {
    private boolean canceled;

    public static class Stats extends EventInGameHUD {}
    public static class Hotbar extends EventInGameHUD {}
    public static class XP extends EventInGameHUD {}

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
