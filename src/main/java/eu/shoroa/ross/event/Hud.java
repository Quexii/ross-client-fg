package eu.shoroa.ross.event;

public class Hud {
    public final String name;

    Hud(String name) {
        this.name = name;
    }

    public boolean is(String name) {
        return this.name.equals(name);
    }

    public static class Layer extends Hud {
        public static final String NAME_SKIA_BOTTOM = "0";
        public static final String NAME_VANILLA_BOTTOM = "1";
        public static final String NAME_SKIA_TOP = "2";
        public static final String NAME_VANILLA_TOP = "3";

        public Layer(String name) {
            super(name);
        }
    }
}
