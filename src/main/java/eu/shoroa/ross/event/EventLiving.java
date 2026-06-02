package eu.shoroa.ross.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

public class EventLiving extends EventEntity {
    public final EntityLivingBase entity;

    public EventLiving(EntityLivingBase entity) {
        super(entity);
        this.entity = entity;
    }

    public static class Update extends EventLiving {
        public Update(EntityLivingBase e) {
            super(e);
        }
    }

    public static class Jump extends EventLiving {
        public Jump(EntityLivingBase e) {
            super(e);
        }
    }

    public static class Attack extends EventLiving {
        public Attack(EntityLivingBase e) {
            super(e);
        }
    }

    public static class Damage extends EventLiving {
        public final float damage;
        public final DamageSource source;

        public Damage(EntityLivingBase entity, float damage, DamageSource source) {
            super(entity);
            this.damage = damage;
            this.source = source;
        }
    }
}
