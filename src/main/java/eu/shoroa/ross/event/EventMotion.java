package eu.shoroa.ross.event;

import eu.shoroa.ross.event.api.Cancelable;
import net.minecraft.entity.player.EntityPlayer;

public class EventMotion extends EventLiving {
    public final EntityPlayer entity;
    public final float partialTicks;

    public EventMotion(EntityPlayer entity, float partialTicks) {
        super(entity);
        this.entity = entity;
        this.partialTicks = partialTicks;
    }

    public static class Pre extends EventMotion implements Cancelable {
        public double x, y, z;
        public float yaw, pitch;
        public boolean onGround;

        private boolean cancelled;

        public Pre(EntityPlayer entity, double x, double y, double z, float yaw, float pitch, boolean onGround, float partialTicks) {
            super(entity, partialTicks);
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.onGround = onGround;
        }

        @Override
        public boolean isCanceled() {
            return cancelled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.cancelled = canceled;
        }
    }

    public static class Post extends EventMotion {
        public Post(EntityPlayer entity, float partialTicks) {
            super(entity, partialTicks);
        }
    }
}