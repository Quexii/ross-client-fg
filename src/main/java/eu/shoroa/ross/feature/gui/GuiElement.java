package eu.shoroa.ross.feature.gui;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.type.Rect;

public abstract class GuiElement {
    private float x, y, width, height;
    private Rect bounds;

    public GuiElement(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.bounds = new Rect(x, y, width, height);
    }

    public abstract void render(float mouseX, float mouseY, float partialTicks);

    public abstract boolean input(float mouseX, float mouseY, EventInput event);

    public void scroll(float value, float partialTicks) {
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
        bounds = new Rect(x, y, width, height);
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
        bounds = new Rect(x, y, width, height);
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
        bounds = new Rect(x, y, width, height);
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
        bounds = new Rect(x, y, width, height);
    }

    public Rect getBounds() {
        return bounds;
    }

    public boolean contains(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}