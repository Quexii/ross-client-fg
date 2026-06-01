package eu.shoroa.ross.ui.api;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.ui.handlers.InputHandler;

public class PointerNode<T extends PointerNode> extends Node<T> implements InputHandler {
    @FunctionalInterface
    public interface MouseButtonHandler {
        boolean onMouseButton(Node node, float mouseX, float mouseY, int button);
    }

    @FunctionalInterface
    public interface MouseDragHandler {
        void onMouseDrag(Node node, float mouseX, float mouseY, float deltaX, float deltaY, int button);
    }

    private MouseButtonHandler mouseDownHandler;
    private MouseButtonHandler mouseUpHandler;
    private MouseButtonHandler clickHandler;
    private MouseDragHandler dragHandler;

    private int pressedButton = -1;
    private boolean dragging;
    private float lastDragX;
    private float lastDragY;

    public PointerNode() {
        this.inputHandler = this;
    }

    public T onMouseDown(MouseButtonHandler handler) {
        this.mouseDownHandler = handler;
        return (T) this;
    }

    public T onMouseUp(MouseButtonHandler handler) {
        this.mouseUpHandler = handler;
        return (T) this;
    }

    public T onClick(MouseButtonHandler handler) {
        this.clickHandler = handler;
        return (T) this;
    }

    public T onDrag(MouseDragHandler handler) {
        this.dragHandler = handler;
        return (T) this;
    }

    public boolean isDragging() {
        return dragging;
    }

    public int getPressedButton() {
        return pressedButton;
    }

    @Override
    public boolean nodeOnInput(Node node, float mouseX, float mouseY, EventInput event) {
        if (event.type != EventInput.Type.MOUSE) {
            return false;
        }

        boolean hasHandlers = mouseDownHandler != null || mouseUpHandler != null || clickHandler != null || dragHandler != null;

        if (event.action == EventInput.Action.PRESS) {
            if (!contains(mouseX, mouseY)) {
                return false;
            }
            pressedButton = event.value;
            dragging = dragHandler != null;
            lastDragX = mouseX;
            lastDragY = mouseY;

            boolean consumed = false;
            if (mouseDownHandler != null) {
                consumed = mouseDownHandler.onMouseButton(this, mouseX, mouseY, event.value);
            }
            return consumed || hasHandlers;
        }

        if (event.action == EventInput.Action.RELEASE) {
            if (event.value != pressedButton) {
                return false;
            }

            dragging = false;
            pressedButton = -1;

            boolean consumed = false;
            if (mouseUpHandler != null) {
                consumed = mouseUpHandler.onMouseButton(this, mouseX, mouseY, event.value);
            }
            if (contains(mouseX, mouseY) && clickHandler != null) {
                consumed = clickHandler.onMouseButton(this, mouseX, mouseY, event.value) || consumed;
            }
            return consumed || hasHandlers;
        }

        return false;
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        if (dragging && dragHandler != null) {
            float deltaX = mouseX - lastDragX;
            float deltaY = mouseY - lastDragY;
            if (deltaX != 0f || deltaY != 0f) {
                dragHandler.onMouseDrag(this, mouseX, mouseY, deltaX, deltaY, pressedButton);
            }
            lastDragX = mouseX;
            lastDragY = mouseY;
        }

        super.render(mouseX, mouseY, partialTicks);
    }
}

