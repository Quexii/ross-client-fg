package eu.shoroa.ross.event;

public interface LifeCycle {
    final class Start implements LifeCycle {
    }

    final class Stop implements LifeCycle {
    }

    final class Resize implements LifeCycle {
        public final int width, height;

        public Resize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}
