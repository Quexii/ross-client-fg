package eu.shoroa.ross.event;

import net.minecraftforge.fml.common.eventhandler.Event;

public abstract class EventHUD extends Event {
    public final float partialTicks;

    protected EventHUD(float partialTicks) {
        this.partialTicks = partialTicks;
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
