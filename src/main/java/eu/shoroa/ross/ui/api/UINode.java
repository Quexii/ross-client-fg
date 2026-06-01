package eu.shoroa.ross.ui.api;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.ui.handlers.InputHandler;
import eu.shoroa.ross.ui.handlers.RenderHandler;

public abstract class UINode extends Node implements InputHandler, RenderHandler {
    public UINode() {
        super();
        this.inputHandler = this;
        this.renderHandler = this;
    }
}
