package eu.shoroa.ross.event;

import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;

import java.util.List;

public class EventRenderEntities {
    public static class Post extends EventRenderEntities {
        public final float partialTicks;
        public final ICamera camera;
        public final Entity renderViewEntity;
        public final List<Entity> entities;

        public Post(float partialTicks, ICamera camera, Entity renderViewEntity, List<Entity> entities) {
            this.partialTicks = partialTicks;
            this.camera = camera;
            this.renderViewEntity = renderViewEntity;
            this.entities = entities;
        }
    }
}
