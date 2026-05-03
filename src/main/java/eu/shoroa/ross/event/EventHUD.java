package eu.shoroa.ross.event;

public abstract class EventHUD {
    public final float partialTicks;

    protected EventHUD(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public static class PreHud extends EventHUD {
        public PreHud(float partialTicks) {
            super(partialTicks);
        }

        public static class Vanilla extends EventHUD {
            public Vanilla(float partialTicks) {
                super(partialTicks);
            }
        }

        public static class Skia extends EventHUD {
            public Skia(float partialTicks) {
                super(partialTicks);
            }
        }
    }

    public static class BottomVanilla extends EventHUD {
        public BottomVanilla(float partialTicks) {
            super(partialTicks);
        }
    }

    public static class BottomSkia extends EventHUD {
        public BottomSkia(float partialTicks) {
            super(partialTicks);
        }
    }

    public static class TopVanilla extends EventHUD {
        public TopVanilla(float partialTicks) {
            super(partialTicks);
        }
    }

    public static class TopSkia extends EventHUD {
        public TopSkia(float partialTicks) {
            super(partialTicks);
        }
    }
}
