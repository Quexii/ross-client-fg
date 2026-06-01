package eu.shoroa.ross.ui.handlers;

import eu.shoroa.ross.ui.api.Node;

public interface ScrollHandler {
    boolean nodeOnScroll(Node node, float mouseX, float mouseY, float scroll, float partialTicks);
}

