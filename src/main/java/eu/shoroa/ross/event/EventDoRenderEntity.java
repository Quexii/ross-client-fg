package eu.shoroa.ross.event;

import net.minecraft.entity.Entity;

public class EventDoRenderEntity {
    public final Entity entity;
    public final double x, y, z;
    public final float entityYaw, partialTicks;

    private EventDoRenderEntity(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.entityYaw = entityYaw;
        this.partialTicks = partialTicks;
    }

    public static final class Pre extends EventDoRenderEntity {
        public Pre(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
            super(entity, x, y, z, entityYaw, partialTicks);
        }
    }

    public static final class Post extends EventDoRenderEntity {
        public Post(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
            super(entity, x, y, z, entityYaw, partialTicks);
        }
    }
}
