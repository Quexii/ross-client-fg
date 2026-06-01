package eu.shoroa.ross.ui.api;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.ui.handlers.InputHandler;

public class InputNode extends Node implements InputHandler {
    public InputNode() {
        super();
        this.inputHandler = this;
    }

    @Override
    public boolean nodeOnInput(Node node, float mouseX, float mouseY, EventInput event) {
        return false;
    }
}
