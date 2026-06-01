package eu.shoroa.ross.ui.api;

import eu.shoroa.ross.ui.handlers.ScrollHandler;

public class ScrollNode<T extends ScrollNode> extends Node<T> implements ScrollHandler {
    public ScrollNode() {
        this.scrollHandler = this;
    }

    public ScrollNode(ScrollHandler handler) {
        this.scrollHandler = handler;
    }

    @Override
    public boolean nodeOnScroll(Node node, float mouseX, float mouseY, float scroll, float partialTicks) {
        return false;
    }
}

