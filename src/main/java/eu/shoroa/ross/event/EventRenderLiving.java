package eu.shoroa.ross.event;

import eu.shoroa.ross.event.api.Cancelable;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;

public abstract class EventRenderLiving<T extends EntityLivingBase> {
    public final EntityLivingBase entity;
    public final RendererLivingEntity<T> renderer;
    public final double x;
    public final double y;
    public final double z;

    public EventRenderLiving(EntityLivingBase entity, RendererLivingEntity<T> renderer, double x, double y, double z) {
        this.entity = entity;
        this.renderer = renderer;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static class Pre<T extends EntityLivingBase> extends EventRenderLiving<T> implements Cancelable {
        private boolean cancelled;

        public Pre(EntityLivingBase entity, RendererLivingEntity<T> renderer, double x, double y, double z) {
            super(entity, renderer, x, y, z);
        }

        @Override
        public boolean isCanceled() {
            return cancelled;
        }

        @Override
        public void setCanceled(boolean cancel) {
            this.cancelled = cancel;
        }
    }

    public static class Post<T extends EntityLivingBase> extends EventRenderLiving<T> {
        public Post(EntityLivingBase entity, RendererLivingEntity<T> renderer, double x, double y, double z) {
            super(entity, renderer, x, y, z);
        }
    }

    public abstract static class Specials<T extends EntityLivingBase> extends EventRenderLiving<T> {
        public Specials(EntityLivingBase entity, RendererLivingEntity<T> renderer, double x, double y, double z) {
            super(entity, renderer, x, y, z);
        }

        public static class Pre<T extends EntityLivingBase> extends Specials<T> implements Cancelable {
            private boolean cancelled;

            public Pre(EntityLivingBase entity, RendererLivingEntity<T> renderer, double x, double y, double z) {
                super(entity, renderer, x, y, z);
            }

            @Override
            public boolean isCanceled() {
                return cancelled;
            }

            @Override
            public void setCanceled(boolean cancel) {
                this.cancelled = cancel;
            }
        }

        public static class Post<T extends EntityLivingBase> extends Specials<T> {
            public Post(EntityLivingBase entity, RendererLivingEntity<T> renderer, double x, double y, double z) {
                super(entity, renderer, x, y, z);
            }
        }
    }
}