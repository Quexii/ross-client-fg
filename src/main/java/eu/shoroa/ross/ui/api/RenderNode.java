package eu.shoroa.ross.ui.api;

import eu.shoroa.ross.ui.handlers.RenderHandler;

public class RenderNode<T extends RenderNode> extends Node<T> implements RenderHandler {
    public RenderNode() {
        this.renderHandler = this;
    }

    public RenderNode(RenderHandler handler) {
        this.renderHandler = handler;
    }

    @Override
    public void nodeOnRender(Node node, float mouseX, float mouseY, float partialTicks) {}
}
