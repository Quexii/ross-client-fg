package eu.shoroa.ross.util;

public class Timer {
    private long last = System.currentTimeMillis();

    public void reset() {
        last = System.currentTimeMillis();
    }

    public boolean elapsed(long ms, boolean reset) {
        if (System.currentTimeMillis() - last >= ms) {
            if (reset) last = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public long getTime() {
        return System.currentTimeMillis() - last;
    }

    public void setTime(long time) {
        last = time;
    }
}
