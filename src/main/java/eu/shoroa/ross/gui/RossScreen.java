package eu.shoroa.ross.gui;

import eu.shoroa.ross.event.EventInput;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import static eu.shoroa.ross.Client.EVENT_BUS;

public abstract class RossScreen extends GuiScreen {
    protected abstract void init();

    protected abstract void render(float mouseX, float mouseY, float partialTicks);

    protected abstract void input(float mouseX, float mouseY, EventInput event);

    protected abstract void scroll(float value, float partialTicks);

    @Override
    public void initGui() {
        EVENT_BUS.register(this);
        init();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        render(Mouse.getX(), Display.getHeight() - Mouse.getY(), partialTicks);
    }

    @Override
    public void onGuiClosed() {
        EVENT_BUS.unregister(this);
    }

    protected boolean cancelEscape() {
        return false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        input(Mouse.getX(), Display.getHeight() - Mouse.getY(), new EventInput(typedChar, EventInput.Type.CHARACTER, Keyboard.getEventKeyState() ? EventInput.Action.PRESS : EventInput.Action.RELEASE));
    }

    @Override
    public void handleMouseInput() {
        if (Mouse.getEventButton() != -1) {
            input(Mouse.getX(), Display.getHeight() - Mouse.getY(), new EventInput(Mouse.getEventButton(), EventInput.Type.MOUSE, Mouse.getEventButtonState() ? EventInput.Action.PRESS : EventInput.Action.RELEASE));
        }

        float rawScroll = Mouse.getEventDWheel();
        float scroll = rawScroll > 0 ? 1 : (rawScroll < 0 ? -1 : 0);
        if (scroll != 0) {
            scroll(scroll, 0);
        }

        super.handleMouseInput();
    }

    @Override
    public void handleKeyboardInput() {
        boolean cancelled = false;
        if (cancelEscape() && Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
            cancelled = true;
        }
        if (Keyboard.getEventKey() != 0) {
            input(Mouse.getX(), Display.getHeight() - Mouse.getY(), new EventInput(Keyboard.getEventKey(), EventInput.Type.KEYBOARD, Keyboard.getEventKeyState() ? EventInput.Action.PRESS : EventInput.Action.RELEASE));
        }
        if (cancelled) return;
        super.handleKeyboardInput();
    }
}
