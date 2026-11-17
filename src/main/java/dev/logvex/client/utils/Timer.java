package dev.logvex.client.utils;

/**
 * A monotonic millisecond stopwatch. One instance per timer; nothing is
 * static, so two features never share a clock by accident.
 */
public final class Timer {
    private long last = System.currentTimeMillis();

    public void reset() {
        last = System.currentTimeMillis();
    }

    public boolean hasElapsed(long millis) {
        return System.currentTimeMillis() - last >= millis;
    }

    public long elapsed() {
        return System.currentTimeMillis() - last;
    }

    /** True once per {@code millis} window; resets itself when it fires. */
    public boolean tick(long millis) {
        if (hasElapsed(millis)) {
            reset();
            return true;
        }
        return false;
    }

    public void setLast(long last) {
        this.last = last;
    }
}
