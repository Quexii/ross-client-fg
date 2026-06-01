package eu.shoroa.ross.ui.handlers;

import eu.shoroa.ross.ui.api.Node;

public interface RenderHandler {
    void nodeOnRender(Node node, float mouseX, float mouseY, float partialTicks);
}

