package eu.shoroa.ross.ui.nodes;

import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.ui.api.Node;

public class ClipNode extends Node<ClipNode> {
    private float radius;

    public ClipNode Radius(float radius) {
        this.radius = radius;
        return this;
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        if (renderHandler != null) {
            renderHandler.nodeOnRender(this, mouseX, mouseY, partialTicks);
        }

        Renderer.save();
        if (radius > 0f) {
            Renderer.clipRRect(getX(), getY(), getWidth(), getHeight(), radius);
        } else {
            Renderer.clipRect(getX(), getY(), getWidth(), getHeight());
        }

        for (Node child : children) {
            child.render(mouseX, mouseY, partialTicks);
        }

        Renderer.restore();
    }
}

