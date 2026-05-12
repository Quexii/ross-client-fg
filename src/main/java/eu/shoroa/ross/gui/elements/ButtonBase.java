package eu.shoroa.ross.gui.elements;

import eu.shoroa.ross.animate.Animate;
import eu.shoroa.ross.animate.Easing;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.gui.GuiElement;

public abstract class ButtonBase extends GuiElement {
    private boolean hovered = false;
    private boolean pressed = false;

    protected int actionValue = 0;
    protected EventInput.Type actionType = EventInput.Type.MOUSE;

    protected Animate hoverAnimation = new Animate(180L, Easing.LINEAR);
    protected Animate pressAnimation = new Animate(180L, Easing.LINEAR);

    protected Runnable onAction = null;

    public ButtonBase(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    protected abstract void renderButton(float mouseX, float mouseY, float partialTicks);

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        hovered = getBounds().contains(mouseX, mouseY);

        hoverAnimation.doEase(hovered);
        pressAnimation.doEase(pressed);

        renderButton(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        EventInput.Action action = event.action;
        EventInput.Type type = event.type;
        int value = event.value;

        if (value == actionValue && type == actionType) {
            if (hovered) {
                if (action == EventInput.Action.PRESS) {
                    pressed = true;
                    return true;
                }

                if (action == EventInput.Action.RELEASE && pressed) {
                    pressed = false;
                    if (onAction != null) {
                        onAction.run();
                    }
                    return true;
                }
            } else {
                if (action == EventInput.Action.RELEASE && pressed) {
                    pressed = false;
                    return true;
                }
            }
        }
        return false;
    }

    public ButtonBase Action(Runnable onAction) {
        this.onAction = onAction;
        return this;
    }

    public ButtonBase Source(EventInput.Type actionSource) {
        this.actionType = actionSource;
        return this;
    }

    public ButtonBase Button(int sourceValue) {
        this.actionValue = sourceValue;
        return this;
    }
}
