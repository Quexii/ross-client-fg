package eu.shoroa.ross.ui.handlers;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.ui.api.Node;

public interface InputHandler {
    boolean nodeOnInput(Node node, float mouseX, float mouseY, EventInput event);
}
