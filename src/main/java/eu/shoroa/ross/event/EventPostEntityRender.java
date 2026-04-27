package eu.shoroa.ross.event;

import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;

public class EventPostEntityRender extends Event {
    private final List<Entity> entities;
    private final float partialTicks;
    private final Entity renderViewEntity;
    private final ICamera camera;

    public EventPostEntityRender(List<Entity> entities, float partialTicks, Entity renderViewEntity, ICamera camera) {
        this.entities = entities;
        this.partialTicks = partialTicks;
        this.renderViewEntity = renderViewEntity;
        this.camera = camera;
    }

    public List<Entity> entities() {
        return entities;
    }

    public float partialTicks() {
        return partialTicks;
    }

    public Entity renderViewEntity() {
        return renderViewEntity;
    }

    public ICamera camera() {
        return camera;
    }
}