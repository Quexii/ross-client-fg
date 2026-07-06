package eu.shoroa.ross.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class EventEntity {
    public final Entity entity;

    public EventEntity(Entity entity) {
        this.entity = entity;
    }
}